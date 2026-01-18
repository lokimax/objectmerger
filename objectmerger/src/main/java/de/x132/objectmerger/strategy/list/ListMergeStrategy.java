package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListMergeStrategy implements MergeStrategy<Object, ListFieldDefinition> {

  @Override
  public Class<ListFieldDefinition> getConfigurationClass() {
    return ListFieldDefinition.class;
  }

  @Override
  public Object merge(
      List<LabeledSource<?>> sources, ListFieldDefinition fieldDef, String fieldName) {

    Map<Object, List<LabeledSource<?>>> groupedBy =
        sources.stream()
            .flatMap(
                source -> {
                  Collection<?> collection =
                      (Collection<?>) ObjectMerger.getFieldValue(source.getSource(), fieldName);
                  if (collection == null) {
                    return Stream.empty();
                  }
                  return collection.stream()
                      .map(item -> new LabeledSource<>(source.getLabel(), item));
                })
            .collect(
                Collectors.groupingBy(
                    labeledSource ->
                        ObjectMerger.getFieldValue(
                            labeledSource.getSource(), fieldDef.getIdentifyBy()),
                    Collectors.toList()));

    return groupedBy.values().stream()
        .map(
            items -> {
              try {
                Class<?> itemClass =
                    Class.forName(fieldDef.getItemMergeDefinition().getTargetClass());
                return doMerge(itemClass, fieldDef, items);
              } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to merge list items", e);
              }
            })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private <T> T doMerge(
      Class<T> itemClass, ListFieldDefinition fieldDef, List<LabeledSource<?>> items) {
    LabeledSource<T>[] sources =
        items.stream()
            .map(item -> new LabeledSource<T>(item.getLabel(), (T) item.getSource()))
            .toArray(LabeledSource[]::new);
    return ObjectMerger.merge(
        itemClass, ObjectMerger.toMergeDefinition(fieldDef.getItemMergeDefinition()), sources);
  }

  @Override
  public String getName() {
    return "mergeList";
  }
}
