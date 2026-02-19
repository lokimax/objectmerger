package de.x132.objectmerger.strategy.priority;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.exception.ConfigurationException;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.config.Prioritizable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PriorityMergeStrategy<T> implements MergeStrategy<T, PriorityFieldDefinition<T>> {

    public static final String NAME = "priority";

    @Override
    public String getName() {
        return NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<PriorityFieldDefinition<T>> getConfigurationClass() {
        return (Class) PriorityFieldDefinition.class;
    }

    @Override
    public T merge(
            List<LabeledSource<?>> sources, PriorityFieldDefinition<T> fieldDef, String fieldName) {
        return merge(sources, (Prioritizable) fieldDef, fieldName, fieldDef.getDefaultValue());
    }

    @SuppressWarnings("unchecked")
    private T merge(
            List<LabeledSource<?>> sources,
            Prioritizable config,
            String fieldName,
            T defaultValue) {
        Map<String, Integer> priorityMap = config.getPriority();

        if (priorityMap == null || priorityMap.isEmpty()) {
            throw new ConfigurationException(
                    "PriorityMergeStrategy requires a non-empty priority map");
        }

        // If priority is defined, consider ONLY sources explicitly listed in the
        // priority map.
        // This enforces "one source per property" semantics: unspecified sources are
        // ignored.
        return (T)
                sources.stream()
                        .filter(s -> priorityMap.containsKey(s.getLabel()))
                        .filter(s -> ObjectMerger.getFieldValue(s.getSource(), fieldName) != null)
                        .min(Comparator.comparingInt(s -> priorityMap.get(s.getLabel())))
                        .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
                        .orElse(defaultValue);
    }
}
