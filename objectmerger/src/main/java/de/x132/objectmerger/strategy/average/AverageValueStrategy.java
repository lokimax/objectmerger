package de.x132.objectmerger.strategy.average;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.List;
import java.util.Objects;

public class AverageValueStrategy implements MergeStrategy<Number, FieldDefinition<Number>> {

  public static final String NAME = "average";

  @Override
  public String getName() {
    return NAME;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<FieldDefinition<Number>> getConfigurationClass() {
    return (Class) FieldDefinition.class;
  }

  @Override
  public Number merge(
      List<LabeledSource<?>> sources, FieldDefinition<Number> fieldDef, String fieldName) {
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
}
