package de.x132.objectmerger.strategy;

import de.x132.objectmerger.LabeledSource;
import java.util.List;

/**
 * Strategy for merging values from multiple sources into a single value.
 *
 * <h2>Extension via SPI</h2>
 *
 * <p>Values are merged by strategies loaded via Java's {@link java.util.ServiceLoader} mechanism.
 * To implement a custom strategy:
 *
 * <ol>
 *   <li>Implement this interface.
 *   <li>Create a specific {@link FieldDefinition} subclass if custom configuration is needed.
 *   <li>Register your implementation in {@code
 *       META-INF/services/de.x132.objectmerger.strategy.MergeStrategy}.
 * </ol>
 *
 * @param <T> The type of the merged value.
 * @param <C> The specific type of {@link FieldDefinition} required by this strategy.
 */
public interface MergeStrategy<T, C extends FieldDefinition<T>> {

  /**
   * Merges the values for a specific field from the given sources.
   *
   * @param sources The list of sources containing the values.
   * @param fieldDef The definition of the field to merge.
   * @param fieldName The name of the field being merged.
   * @return The merged value.
   */
  T merge(List<LabeledSource<?>> sources, C fieldDef, String fieldName);

  /**
   * Returns the unique name of this strategy. This name is used in the configuration to refer to
   * this strategy.
   *
   * @return The unique name of the strategy (e.g. "priority").
   */
  String getName();

  /**
   * Returns the expected configuration class for this strategy.
   *
   * <p><strong>Contract:</strong> This method must NOT return {@code null}. It must return the
   * specific subclass of {@link FieldDefinition} that this strategy expects. This is used by the
   * runtime to validate that the configuration provided in the merge definition matches the
   * strategy's requirements.
   *
   * @return The expected FieldDefinition subclass.
   */
  Class<C> getConfigurationClass();
}
