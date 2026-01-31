package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.x132.objectmerger.strategy.config.ListConfig;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListMergeStrategy implements MergeStrategy<Object, ListFieldDefinition> {

  public static final String NAME = "mergeList";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Class<ListFieldDefinition> getConfigurationClass() {
    return ListFieldDefinition.class;
  }

  @Override
  public Object merge(
      List<LabeledSource<?>> sources, ListFieldDefinition fieldDef, String fieldName) {
    return mergeInternal(sources, fieldDef, fieldName);
  }

  private Object mergeInternal(
      List<LabeledSource<?>> sources, ListConfig fieldDef, String fieldName) {

    Map<Object, List<LabeledSource<?>>> groupedBy = sources.stream()
        .flatMap(
            source -> {
              Collection<?> collection = (Collection<?>) ObjectMerger.getFieldValue(source.getSource(), fieldName);
              if (collection == null) {
                return Stream.empty();
              }
              return collection.stream()
                  .map(item -> new LabeledSource<>(source.getLabel(), item));
            })
        .collect(
            Collectors.groupingBy(
                labeledSource -> ObjectMerger.getFieldValue(
                    labeledSource.getSource(), fieldDef.getIdentifyBy()),
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
                          requiredLabel -> sourcesForId.stream()
                              .anyMatch(s -> requiredLabel.equals(s.getLabel())));
                } else {
                  return sourcesForId.stream()
                      .noneMatch(s -> fieldDef.getKeyOriginLabels().contains(s.getLabel()));
                }
              });
    }

    return groupedBy.values().stream()
        .map(
            items -> {
              try {
                if (fieldDef.getItemMergeDefinition() != null
                    && fieldDef.getItemMergeDefinition().getTargetClass() != null) {
                  Class<?> itemClass = Class.forName(fieldDef.getItemMergeDefinition().getTargetClass());
                  return doMerge(itemClass, fieldDef, items);
                } else if (items.stream().allMatch(i -> i.getSource() instanceof Map)) {
                  return doMapMerge(fieldDef, items);
                } else {
                  throw new IllegalArgumentException(
                      "Cannot merge list items: missing target class in definition and items are not Maps.");
                }
              } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to merge list items", e);
              }
            })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private <T> T doMerge(
      Class<T> itemClass, ListConfig fieldDef, List<LabeledSource<?>> items) {
    LabeledSource<T>[] sources = items.stream()
        .map(item -> new LabeledSource<T>(item.getLabel(), (T) item.getSource()))
        .toArray(LabeledSource[]::new);
    return ObjectMerger.merge(
        itemClass, ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition()), sources);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> doMapMerge(
      ListConfig fieldDef, List<LabeledSource<?>> items) {

    MergeDefinition def = (fieldDef.getItemMergeDefinition() != null)
        ? ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition())
        : new MergeDefinition();
    if (def.getDefinitions() == null) {
      def.setDefinitions(new HashMap<>());
    }

    LabeledSource<Map<String, Object>>[] sources = items.stream()
        .map(
            item -> new LabeledSource<Map<String, Object>>(
                item.getLabel(), (Map<String, Object>) item.getSource()))
        .toArray(LabeledSource[]::new);

    return ObjectMerger.merge(def, sources);
  }
}
