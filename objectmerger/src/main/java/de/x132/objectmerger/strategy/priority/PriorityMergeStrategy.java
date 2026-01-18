package de.x132.objectmerger.strategy.priority;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PriorityMergeStrategy implements MergeStrategy<Object> {

  @Override
  public Class<? extends FieldDefinition> getConfigurationClass() {
    return PriorityFieldDefinition.class;
  }

  @Override
  public Object merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    if (!(fieldDef instanceof PriorityFieldDefinition priorityDef)) {
      throw new IllegalArgumentException("PriorityMergeStrategy requires PriorityFieldDefinition");
    }

    Map<String, Integer> priorityMap = priorityDef.getPriority();

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

  @Override
  public String getName() {
    return "priority";
  }
}
