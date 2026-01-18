package de.x132.objectmerger.strategy.map;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMergeStrategy implements MergeStrategy<Object> {

  @Override
  public Class<? extends FieldDefinition> getConfigurationClass() {
    return FieldDefinition.class;
  }

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    if (sources.isEmpty()) {
      return new HashMap<>();
    }

    MapFieldDefinition mapDef =
        (fieldDef instanceof MapFieldDefinition) ? (MapFieldDefinition) fieldDef : null;

    // 1. Determine the set of keys to include in the result
    java.util.Set<Object> targetKeys = new java.util.HashSet<>();

    if (mapDef != null
        && mapDef.getKeyTemplateSources() != null
        && !mapDef.getKeyTemplateSources().isEmpty()) {
      // Template Mode: Only use keys from specified sources
      for (String label : mapDef.getKeyTemplateSources()) {
        sources.stream()
            .filter(s -> s.getLabel().equals(label))
            .findFirst()
            .ifPresent(
                source -> {
                  @SuppressWarnings("unchecked")
                  Map<Object, Object> sourceMap =
                      (Map<Object, Object>)
                          ObjectMerger.getFieldValue(source.getSource(), fieldName);
                  if (sourceMap != null) {
                    targetKeys.addAll(sourceMap.keySet());
                  }
                });
      }
    } else {
      // Default Mode (Union): Use keys from ALL sources
      for (LabeledSource<?> source : sources) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> sourceMap =
            (Map<Object, Object>) ObjectMerger.getFieldValue(source.getSource(), fieldName);
        if (sourceMap != null) {
          targetKeys.addAll(sourceMap.keySet());
        }
      }
    }

    Map<Object, Object> mergedMap = new HashMap<>();

    // 2. Iterate over determined keys and match values from all sources
    for (Object key : targetKeys) {
      java.util.List<LabeledSource<?>> valuesForKey = new java.util.ArrayList<>();

      // Collect values for this key from ALL sources
      for (LabeledSource<?> source : sources) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> sourceMap =
            (Map<Object, Object>) ObjectMerger.getFieldValue(source.getSource(), fieldName);

        if (sourceMap != null && sourceMap.containsKey(key)) {
          Object value = sourceMap.get(key);
          valuesForKey.add(new LabeledSource<>(source.getLabel(), value));
        }
      }

      if (mapDef != null && mapDef.getItemMergeDefinition() != null) {
        // Merge the values using itemMergeDefinition
        try {
          Class<?> valueClass = Class.forName(mapDef.getItemMergeDefinition().getTargetClass());
          Object mergedValue = doMerge(valueClass, mapDef, valuesForKey);
          mergedMap.put(key, mergedValue);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Failed to merge map values", e);
        }
      } else {
        // If no itemMergeDefinition, just use the first value (priority)
        if (!valuesForKey.isEmpty()) {
          mergedMap.put(key, valuesForKey.get(0).getSource());
        }
      }
    }

    return mergedMap;
  }

  @SuppressWarnings("unchecked")
  private <T> T doMerge(
      Class<T> valueClass, MapFieldDefinition fieldDef, List<LabeledSource<?>> values) {
    LabeledSource<T>[] sources =
        values.stream()
            .map(item -> new LabeledSource<T>(item.getLabel(), (T) item.getSource()))
            .toArray(LabeledSource[]::new);

    return ObjectMerger.merge(
        valueClass, ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition()), sources);
  }

  @Override
  public String getName() {
    return "mergeMap";
  }
}
