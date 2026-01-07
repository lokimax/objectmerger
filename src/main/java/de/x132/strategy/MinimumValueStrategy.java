package de.x132.strategy;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import de.x132.ObjectMerger;

public class MinimumValueStrategy implements MergeStrategy<Object> {

    @Override
    public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
        return sources.stream()
                .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
                .filter(Objects::nonNull)
                .min(
                        Comparator.comparingDouble(
                                value -> {
                                    if (value instanceof Number) {
                                        return ((Number) value).doubleValue();
                                    }
                                    throw new IllegalArgumentException(
                                            "Field "
                                                    + fieldName
                                                    + " must be a number for minimum strategy, but was: "
                                                    + value.getClass().getSimpleName());
                                }))
                .orElse(fieldDef.getDefaultValue());
    }
}
