package de.x132.objectmerger;

import de.x132.objectmerger.engine.MapMerger;
import de.x132.objectmerger.engine.PojoMerger;
import de.x132.objectmerger.helper.ReflectionHelper;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for merging objects based on a definition and strategies.
 *
 * <p>This class acts as a Facade, delegating the actual merging logic to specialized engines:
 *
 * <ul>
 *   <li>{@link PojoMerger} for generic POJO merging.
 *   <li>{@link MapMerger} for Map-based merging.
 * </ul>
 *
 * Field access and modification is handled by {@link ReflectionHelper}.
 *
 * <h2>Strategy Loading</h2>
 *
 * Strategies are loaded via Java SPI (Service Provider Interface).
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMerger {

  /**
   * Merges multiple sources into a target object based on the provided definition. Delegates to
   * {@link PojoMerger}.
   *
   * @param targetClass The class of the result object.
   * @param mergeDefinition The definition of how fields should be merged.
   * @param sources The sources to merge.
   * @param <T> The type of the result object.
   * @return A new instance of T with merged values.
   */
  @SafeVarargs
  public static <T> T merge(
      Class<T> targetClass, MergeDefinition mergeDefinition, LabeledSource<T>... sources) {
    return PojoMerger.merge(targetClass, mergeDefinition, sources);
  }

  /**
   * Merges multiple sources into a target map based on the provided definition. Delegates to {@link
   * MapMerger}.
   *
   * @param mergeDefinition The definition of how fields should be merged.
   * @param sources The sources to merge (Maps).
   * @return A new Map with merged values.
   */
  @SafeVarargs
  public static Map<String, Object> merge(
      MergeDefinition mergeDefinition, LabeledSource<Map<String, Object>>... sources) {
    return MapMerger.merge(mergeDefinition, sources);
  }

  /** Gets a field value safely from an object or Map. Delegates to {@link ReflectionHelper}. */
  public static Object getFieldValue(Object obj, String fieldName) {
    return ReflectionHelper.getFieldValue(obj, fieldName);
  }

  public static MergeDefinition toMergeDefinition(ItemMergeDefinition itemMergeDefinition) {
    MergeDefinition mergeDefinition = new MergeDefinition();
    mergeDefinition.setDefinitions(itemMergeDefinition.getDefinitions());
    return mergeDefinition;
  }
}
