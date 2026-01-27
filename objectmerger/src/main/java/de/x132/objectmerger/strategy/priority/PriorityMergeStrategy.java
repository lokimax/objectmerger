package de.x132.objectmerger.strategy.priority;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PriorityMergeStrategy implements MergeStrategy<Object, PriorityFieldDefinition> {

  public static final String NAME = "priority";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Class<PriorityFieldDefinition> getConfigurationClass() {
    return PriorityFieldDefinition.class;
  }

  @Override
  public Object merge(
      List<LabeledSource<?>> sources, PriorityFieldDefinition fieldDef, String fieldName) {
    Map<String, Integer> priorityMap = fieldDef.getPriority();

    if (priorityMap == null || priorityMap.isEmpty()) {
      throw new IllegalArgumentException("PriorityMergeStrategy requires a non-empty priority map");
    }

    // If priority is defined, consider ONLY sources explicitly listed in the
    // priority map.
    // This enforces "one source per property" semantics: unspecified sources are
    // ignored.
    return sources.stream()
        .filter(s -> priorityMap.containsKey(s.getLabel()))
        .filter(s -> ObjectMerger.getFieldValue(s.getSource(), fieldName) != null)
        .min(Comparator.comparingInt(s -> priorityMap.get(s.getLabel())))
        .map(source -> ObjectMerger.getFieldValue(source.getSource(), fieldName))
        .orElse(fieldDef.getDefaultValue());
  }
}
