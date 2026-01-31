package de.x132.objectmerger.strategy.standard;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.List;
import java.util.Objects;

public class StandardMergeStrategy<T> implements MergeStrategy<T, FieldDefinition<T>> {

  public static final String NAME = "standard";

  @Override
  public String getName() {
    return NAME;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<FieldDefinition<T>> getConfigurationClass() {
    return (Class) FieldDefinition.class;
  }

  @Override
  public T merge(List<LabeledSource<?>> sources, FieldDefinition<T> fieldDef, String fieldName) {
    if (sources.isEmpty()) {
      return fieldDef.getDefaultValue();
    }

    return (T)
        sources.stream()
            .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(fieldDef.getDefaultValue());
  }
}
