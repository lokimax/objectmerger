package de.x132.objectmerger.strategy.concatenate;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConcatenateStrategy implements MergeStrategy<Object> {

  private static final String DEFAULT_DELIMITER = ",";

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    String delimiter = getDelimiter(fieldDef);
    Map<String, Integer> priority = null;
    if (fieldDef instanceof PriorityFieldDefinition priorityDef) {
      priority = priorityDef.getPriority();
    }

    List<String> values =
        sources.stream()
            .sorted(getComparator(priority))
            .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
            .filter(Objects::nonNull)
            .map(this::validateAndConvertToString)
            .collect(Collectors.toList());

    if (values.isEmpty()) {
      return fieldDef.getDefaultValue();
    }

    return String.join(delimiter, values);
  }

  private String getDelimiter(FieldDefinition fieldDef) {
    // Try to get delimiter from a custom field or property
    // For now, return default - can be extended to read from metadata
    return DEFAULT_DELIMITER;
  }

  private Comparator<LabeledSource<?>> getComparator(Map<String, Integer> priority) {
    if (priority == null || priority.isEmpty()) {
      // If no priority, keep original order
      return (s1, s2) -> 0;
    }
    // Sort by priority (lower number = higher priority = comes first)
    return Comparator.comparingInt(s -> priority.getOrDefault(s.getLabel(), Integer.MAX_VALUE));
  }

  private String validateAndConvertToString(Object value) {
    if (value instanceof String) {
      return (String) value;
    }
    throw new IllegalArgumentException(
        "Field must be a String for concatenate strategy, but was: "
            + value.getClass().getSimpleName());
  }

  @Override
  public String getName() {
    return "concatenate";
  }
}
