package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import java.util.List;

/**
 * Strategy for merging values from multiple sources into a single value.
 *
 * @param <T> The type of the merged value.
 */
public interface MergeStrategy<T> {

  /**
   * Merges the values for a specific field from the given sources.
   *
   * @param sources   The list of sources containing the values.
   * @param fieldDef  The definition of the field to merge.
   * @param fieldName The name of the field being merged.
   * @return The merged value.
   */
  T merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName);

  /**
   * Returns the unique name of this strategy.
   * This name is used in the configuration to refer to this strategy.
   *
   * @return The unique name of the strategy (e.g. "priority").
   */
  String getName();
}
