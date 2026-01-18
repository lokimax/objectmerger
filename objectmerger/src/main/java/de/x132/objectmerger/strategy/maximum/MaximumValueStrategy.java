package de.x132.objectmerger.strategy.maximum;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MaximumValueStrategy implements MergeStrategy<Object, FieldDefinition> {

  @Override
  public Class<FieldDefinition> getConfigurationClass() {
    return FieldDefinition.class;
  }

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    return sources.stream()
        .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
        .filter(Objects::nonNull)
        .max(
            Comparator.comparingDouble(
                value -> {
                  if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                  }
                  throw new IllegalArgumentException(
                      "Field "
                          + fieldName
                          + " must be a number for maximum strategy, but was: "
                          + value.getClass().getSimpleName());
                }))
        .orElse(fieldDef.getDefaultValue());
  }

  @Override
  public String getName() {
    return "maximum";
  }
}
