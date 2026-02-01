package de.x132.objectmerger.strategy.recursive;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.exception.MergeExecutionException;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.List;

public class RecursiveMergeStrategy<T> implements MergeStrategy<T, RecursiveFieldDefinition<T>> {

  public static final String NAME = "recursive";

  @Override
  public String getName() {
    return NAME;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<RecursiveFieldDefinition<T>> getConfigurationClass() {
    return (Class) RecursiveFieldDefinition.class;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T merge(
      List<LabeledSource<?>> sources, RecursiveFieldDefinition<T> fieldDef, String fieldName) {
    if (sources.isEmpty()) {
      return fieldDef.getDefaultValue();
    }

    // Extract field values from sources
    List<LabeledSource<Object>> nestedSources =
        sources.stream()
            .map(
                s -> {
                  Object val = ObjectMerger.getFieldValue(s.getSource(), fieldName);
                  return new LabeledSource<>(s.getLabel(), val);
                })
            .collect(java.util.stream.Collectors.toList());

    // Determine target class from the first non-null nested source
    Object firstNonNull =
        nestedSources.stream()
            .map(LabeledSource::getSource)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);

    if (firstNonNull == null) {
      return fieldDef.getDefaultValue();
    }

    Class<?> targetClass = firstNonNull.getClass();

    // Prepare nested definition
    MergeDefinition nestedDef = fieldDef.getNestedDefinition();

    // If nested definition is empty, we must rely on Template Mode for discovery!
    // We can synthesize a Template Source Label if we want to force one source to
    // be the template?
    // Or we just let ObjectMerger handle it.
    // Wait, if 'nestedDef' is empty, ObjectMerger will look for 'definitions'.
    // If that is empty, it returns empty object (unless template mode is ON).

    // Feature Idea:
    // If nestedDefinition has NO templateSource set, but we have sources here,
    // should we imply one?
    // Let's explicitly support Template Mode in Recursive Field Def?
    // Or just say: If you want auto-discovery, set 'templateSourceLabel' in
    // 'nestedDefinition'.
    // That's cleaner.

    // HOWEVER: We have sources interacting dynamically here.
    // The main ObjectMerger loop doesn't pass 'definitions' down automatically
    // unless we do it here.
    // But 'nestedDef' IS that definition.

    // Cast sources to LabeledSource<T>
    @SuppressWarnings("unchecked")
    LabeledSource<T>[] castSources =
        (LabeledSource<T>[])
            nestedSources.stream()
                .map(s -> new LabeledSource<>(s.getLabel(), (T) s.getSource()))
                .toArray(LabeledSource[]::new);

    try {
      @SuppressWarnings("unchecked")
      Class<T> typedClass = (Class<T>) targetClass;
      return ObjectMerger.merge(typedClass, nestedDef, castSources);
    } catch (Exception e) {
      throw new MergeExecutionException("Recursive merge failed for field " + fieldName, e);
    }
  }
}
