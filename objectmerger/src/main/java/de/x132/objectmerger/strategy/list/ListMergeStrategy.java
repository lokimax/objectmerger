package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.exception.ConfigurationException;
import de.x132.objectmerger.exception.MergeExecutionException;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.config.ListConfig;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListMergeStrategy
        implements MergeStrategy<List<Object>, ListFieldDefinition<List<Object>>> {

    public static final String NAME = "mergeList";

    @Override
    public String getName() {
        return NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<ListFieldDefinition<List<Object>>> getConfigurationClass() {
        return (Class) ListFieldDefinition.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> merge(
            List<LabeledSource<?>> sources,
            ListFieldDefinition<List<Object>> fieldDef,
            String fieldName) {
        return (List<Object>) mergeInternal(sources, fieldDef, fieldName);
    }

    private Object mergeInternal(
            List<LabeledSource<?>> sources, ListConfig fieldDef, String fieldName) {

        Map<Object, List<LabeledSource<?>>> groupedBy =
                sources.stream()
                        .flatMap(
                                source -> {
                                    Object val =
                                            ObjectMerger.getFieldValue(
                                                    source.getSource(), fieldName);
                                    if (!(val instanceof Collection)) {
                                        return Stream.empty();
                                    }
                                    Collection<?> collection = (Collection<?>) val;
                                    return collection.stream()
                                            .map(
                                                    item ->
                                                            new LabeledSource<Object>(
                                                                    source.getLabel(), item));
                                })
                        .collect(
                                Collectors.groupingBy(
                                        labeledSource ->
                                                ObjectMerger.getFieldValue(
                                                        labeledSource.getSource(),
                                                        fieldDef.getIdentifyBy()),
                                        Collectors.toList()));

        if (fieldDef.getKeyOriginLabels() != null && !fieldDef.getKeyOriginLabels().isEmpty()) {
            groupedBy
                    .entrySet()
                    .removeIf(
                            entry -> {
                                List<LabeledSource<?>> sourcesForId = entry.getValue();

                                if (fieldDef.isRequirePresenceInAllKeyOrigins()) {
                                    return !fieldDef.getKeyOriginLabels().stream()
                                            .allMatch(
                                                    requiredLabel ->
                                                            sourcesForId.stream()
                                                                    .anyMatch(
                                                                            s ->
                                                                                    requiredLabel
                                                                                            .equals(
                                                                                                    s
                                                                                                            .getLabel())));
                                } else {
                                    return sourcesForId.stream()
                                            .noneMatch(
                                                    s ->
                                                            fieldDef.getKeyOriginLabels()
                                                                    .contains(s.getLabel()));
                                }
                            });
        }

        return groupedBy.values().stream()
                .map(
                        items -> {
                            try {
                                if (fieldDef.getItemMergeDefinition() != null
                                        && fieldDef.getItemMergeDefinition().getTargetClass()
                                                != null) {
                                    Class<?> itemClass =
                                            Class.forName(
                                                    fieldDef.getItemMergeDefinition()
                                                            .getTargetClass());
                                    return doMerge(itemClass, fieldDef, items);
                                } else if (items.stream()
                                        .allMatch(i -> i.getSource() instanceof Map)) {
                                    return doMapMerge(fieldDef, items);
                                } else {
                                    throw new ConfigurationException(
                                            "Cannot merge list items: missing target class in definition and items are not Maps.");
                                }
                            } catch (ClassNotFoundException e) {
                                throw new MergeExecutionException("Failed to merge list items", e);
                            }
                        })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> T doMerge(Class<T> itemClass, ListConfig fieldDef, List<LabeledSource<?>> items) {
        LabeledSource<T>[] sources =
                items.stream()
                        .map(item -> new LabeledSource<T>(item.getLabel(), (T) item.getSource()))
                        .toArray(LabeledSource[]::new);
        return ObjectMerger.merge(
                itemClass,
                ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition()),
                sources);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doMapMerge(ListConfig fieldDef, List<LabeledSource<?>> items) {

        MergeDefinition def =
                (fieldDef.getItemMergeDefinition() != null)
                        ? ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition())
                        : new MergeDefinition();
        if (def.getDefinitions() == null) {
            def.setDefinitions(new HashMap<>());
        }

        LabeledSource<Map<String, Object>>[] sources =
                items.stream()
                        .map(
                                item ->
                                        new LabeledSource<Map<String, Object>>(
                                                item.getLabel(),
                                                (Map<String, Object>) item.getSource()))
                        .toArray(LabeledSource[]::new);

        return ObjectMerger.merge(def, sources);
    }
}
