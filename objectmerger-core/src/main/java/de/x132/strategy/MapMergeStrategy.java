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
    // Group all map entries by key across all sources
    Map<Object, List<LabeledSource<?>>> groupedByKey = new HashMap<>();

    for (LabeledSource<?> source : sources) {
      @SuppressWarnings("unchecked")
      Map<Object, Object> sourceMap =
          (Map<Object, Object>) ObjectMerger.getFieldValue(source.getSource(), fieldName);

      if (sourceMap != null) {
        for (Map.Entry<Object, Object> entry : sourceMap.entrySet()) {
          Object key = entry.getKey();
          Object value = entry.getValue();

          // Create a labeled source for each value with its key
          LabeledSource<Object> labeledValue = new LabeledSource<>(source.getLabel(), value);

          groupedByKey.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(labeledValue);
        }
      }
    }

    // Merge values for each key if itemMergeDefinition is provided
    Map<Object, Object> mergedMap = new HashMap<>();

    for (Map.Entry<Object, List<LabeledSource<?>>> entry : groupedByKey.entrySet()) {
      Object key = entry.getKey();
      List<LabeledSource<?>> valuesForKey = entry.getValue();

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
}
