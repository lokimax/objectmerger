package de.x132.objectmerger.strategy.standard;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.List;
import java.util.Objects;

public class StandardMergeStrategy implements MergeStrategy<Object, FieldDefinition> {

  @Override
  public Class<FieldDefinition> getConfigurationClass() {
    return FieldDefinition.class;
  }

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    if (sources.isEmpty()) {
      return fieldDef.getDefaultValue();
    }

    return sources.stream()
        .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(fieldDef.getDefaultValue());
  }

  public static final String NAME = "standard";

  @Override
  public String getName() {
    return NAME;
  }
}
