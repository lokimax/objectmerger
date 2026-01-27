package de.x132.objectmerger.strategy.date;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.List;
import java.util.Objects;

public class LatestDateStrategy implements MergeStrategy<Object, FieldDefinition> {

  public static final String NAME = "latestDate";

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
        .filter(value -> value instanceof Comparable)
        .max((o1, o2) -> ((Comparable) o1).compareTo(o2))
        .orElseGet(
            () -> {
              // Log warning if non-comparable types were encountered?
              // For now, simplicity: if reduced to empty, return default.
              return fieldDef.getDefaultValue();
            });
  }
}
