package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import de.x132.ObjectMerger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PriorityMergeStrategy implements MergeStrategy<Object> {

    @Override
    public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
        Map<String, Integer> priority = fieldDef.getPriority();

        if (priority == null || priority.isEmpty()) {
            // If no priority is defined, return first non-null value
            return sources.stream()
                    .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(fieldDef.getDefaultValue());
        }

        // If priority is defined, return value from source with lowest priority number
        return sources.stream()
                .filter(s -> ObjectMerger.getFieldValue(s.getSource(), fieldName) != null)
                .min(Comparator.comparingInt(s -> priority.getOrDefault(s.getLabel(), Integer.MAX_VALUE)))
                .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
                .orElse(fieldDef.getDefaultValue());
    }
}
