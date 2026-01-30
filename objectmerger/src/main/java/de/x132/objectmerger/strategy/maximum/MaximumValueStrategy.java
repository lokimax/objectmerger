package de.x132.objectmerger.strategy.maximum;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.List;
import java.util.Objects;

public class MaximumValueStrategy implements MergeStrategy<Object, FieldDefinition> {

  public static final String NAME = "maximum";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Class<FieldDefinition> getConfigurationClass() {
    return FieldDefinition.class;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    return sources.stream()
        .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
        .filter(Objects::nonNull)
        .max(
            (o1, o2) -> {
              if (o1 instanceof Number && o2 instanceof Number) {
                return Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue());
              }
              if (o1 instanceof Comparable && o2 instanceof Comparable) {
                return ((Comparable) o1).compareTo(o2);
              }
              throw new IllegalArgumentException(
                  "Field "
                      + fieldName
                      + " values must be Numbers or Comparable for maximum strategy, but found: "
                      + o1.getClass().getSimpleName()
                      + " and "
                      + o2.getClass().getSimpleName());
            })
        .orElse(fieldDef.getDefaultValue());
  }
}
