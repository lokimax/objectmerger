package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import de.x132.ObjectMerger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMergeStrategy implements MergeStrategy<Object> {

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    if (sources.isEmpty()) {
      return new HashMap<>();
    }

    // The first source acts as the template (master) for keys
    LabeledSource<?> templateSource = sources.get(0);
    @SuppressWarnings("unchecked")
    Map<Object, Object> templateMap =
        (Map<Object, Object>) ObjectMerger.getFieldValue(templateSource.getSource(), fieldName);

    if (templateMap == null) {
      return new HashMap<>();
    }

    Map<Object, Object> mergedMap = new HashMap<>();

    // Iterate ONLY over the keys of the template map
    for (Object key : templateMap.keySet()) {
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

      if (fieldDef != null && fieldDef.getItemMergeDefinition() != null) {
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
      Class<T> valueClass, FieldDefinition fieldDef, List<LabeledSource<?>> values) {
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
