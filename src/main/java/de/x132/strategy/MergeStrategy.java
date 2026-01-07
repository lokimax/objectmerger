package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import java.util.List;

public interface MergeStrategy<T> {
  T merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName);
}
