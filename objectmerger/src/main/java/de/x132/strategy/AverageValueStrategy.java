package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import de.x132.ObjectMerger;
import java.util.List;
import java.util.Objects;

public class AverageValueStrategy implements MergeStrategy<Object> {

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    double sum =
        sources.stream()
            .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
            .filter(Objects::nonNull)
            .mapToDouble(
                value -> {
                  if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                  }
                  throw new IllegalArgumentException(
                      "Field " + fieldName + " must be a number for average strategy");
                })
            .sum();

    long count =
        sources.stream()
            .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
            .filter(Objects::nonNull)
            .count();

    if (count == 0) {
      return fieldDef.getDefaultValue();
    }

    double average = sum / count;

    // Return as Integer if result is a whole number, otherwise as Double
    if (average == Math.floor(average)
        && average <= Integer.MAX_VALUE
        && average >= Integer.MIN_VALUE) {
      return (int) average;
    }
    return average;
  }

  @Override
  public String getName() {
    return "average";
  }
}
