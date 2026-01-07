package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import de.x132.ObjectMerger;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MinimumValueStrategy implements MergeStrategy<Object> {

    @Override
    public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
        return sources.stream()
                .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(value -> (Integer) value))
                .orElse(fieldDef.getDefaultValue());
    }
}
