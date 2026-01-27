package de.x132.objectmerger.strategy.map;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapMergeStrategy implements MergeStrategy<Object, MapFieldDefinition> {

  public static final String NAME = "mergeMap";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Class<MapFieldDefinition> getConfigurationClass() {
    return MapFieldDefinition.class;
  }

  @Override
  public Object merge(
      List<LabeledSource<?>> sources, MapFieldDefinition fieldDef, String fieldName) {
    if (sources.isEmpty()) {
      return new HashMap<>();
    }

    // fieldDef is already MapFieldDefinition, no cast needed.

    // 1. Determine the set of keys to include in the result
    Set<Object> targetKeys = new HashSet<>();

    if (fieldDef.getKeyTemplateSources() != null && !fieldDef.getKeyTemplateSources().isEmpty()) {
      // Template Mode: Only use keys from specified sources
      for (String label : fieldDef.getKeyTemplateSources()) {
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
      List<LabeledSource<?>> valuesForKey = new ArrayList<>();

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

      if (fieldDef.getItemMergeDefinition() != null) {
        // Merge the values using itemMergeDefinition
        try {
          Class<?> valueClass = Class.forName(fieldDef.getItemMergeDefinition().getTargetClass());
          Object mergedValue = doMerge(valueClass, fieldDef, valuesForKey);
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
    if (Map.class.isAssignableFrom(valueClass)) {
      LabeledSource<Map<String, Object>>[] sources =
          values.stream()
              .map(
                  item ->
                      new LabeledSource<Map<String, Object>>(
                          item.getLabel(), (Map<String, Object>) item.getSource()))
              .toArray(LabeledSource[]::new);
      return (T)
          ObjectMerger.merge(
              ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition()), sources);
    }
    LabeledSource<T>[] sources =
        values.stream()
            .map(item -> new LabeledSource<T>(item.getLabel(), (T) item.getSource()))
            .toArray(LabeledSource[]::new);

    return ObjectMerger.merge(
        valueClass, ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition()), sources);
  }
}
