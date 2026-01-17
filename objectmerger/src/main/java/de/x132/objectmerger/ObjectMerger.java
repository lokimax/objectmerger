package de.x132.objectmerger;

import de.x132.objectmerger.strategy.MergeStrategy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for merging objects based on a definition and strategies.
 *
 * <p>
 * This class uses reflection to iterate over fields and apply specific merge
 * strategies. It
 * supports different strategies for resolving conflicts or combining values
 * from multiple sources.
 * Strategies are loaded via Java SPI (Service Provider Interface).
 */
@Slf4j
public class ObjectMerger {

  private static final Map<String, MergeStrategy<?>> STRATEGIES;

  static {
    STRATEGIES = new java.util.HashMap<>();
    // ServiceLoader.load(Class<S>) returns ServiceLoader<S>
    ServiceLoader<MergeStrategy> loader = ServiceLoader.load(MergeStrategy.class);
    for (MergeStrategy<?> strategy : loader) {
      STRATEGIES.put(strategy.getName(), strategy);
    }
  }

  /**
   * Merges multiple sources into a target object based on the provided
   * definition.
   *
   * @param targetClass     The class of the result object.
   * @param mergeDefinition The definition of how fields should be merged.
   * @param sources         The sources to merge.
   * @param <T>             The type of the result object.
   * @return A new instance of T with merged values.
   * @throws RuntimeException If merging fails (e.g. instantiation or field access
   *                          errors).
   */
  @SafeVarargs
  public static <T> T merge(
      Class<T> targetClass, MergeDefinition mergeDefinition, LabeledSource<T>... sources) {
    try {
      T result = targetClass.getDeclaredConstructor().newInstance();
      Map<String, FieldDefinition> definitions = mergeDefinition.getDefinitions();
      List<LabeledSource<T>> sourceList = Arrays.asList(sources);
      MergeContext<T> context = new MergeContext<>(targetClass, result, sourceList);

      for (Map.Entry<String, FieldDefinition> entry : definitions.entrySet()) {
        processField(context, entry.getKey(), entry.getValue());
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to merge objects of type {}", targetClass.getName(), e);
      throw new RuntimeException("Failed to merge objects of type " + targetClass.getName(), e);
    }
  }

  private static <T> void processField(
      MergeContext<T> context, String fieldName, FieldDefinition fieldDef) {
    try {
      Field field = context.targetClass().getDeclaredField(fieldName);
      field.setAccessible(true);

      MergeStrategy<?> strategy = resolveStrategy(fieldDef);
      if (strategy != null) {
        if (!strategy.getConfigurationClass().isInstance(fieldDef)) {
          throw new IllegalArgumentException(
              String.format(
                  "Field '%s' requires configuration of type '%s' but got '%s' for strategy '%s'",
                  fieldName,
                  strategy.getConfigurationClass().getSimpleName(),
                  fieldDef.getClass().getSimpleName(),
                  strategy.getName()));
        }
        Object mergedValue = strategy.merge(new java.util.ArrayList<>(context.sources()), fieldDef, fieldName);
        field.set(context.result(), mergedValue);
      }
    } catch (NoSuchFieldException e) {
      log.warn(
          "Field '{}' defined in mapping but missing in class '{}'",
          fieldName,
          context.targetClass().getName());
    } catch (IllegalAccessException e) {
      log.error("Access denied for field '{}'", fieldName, e);
      throw new RuntimeException("Access denied for field: " + fieldName, e);
    }
  }

  private static MergeStrategy<?> resolveStrategy(FieldDefinition fieldDef) {
    String strategyName = fieldDef.getStrategy() != null ? fieldDef.getStrategy() : "standard";
    MergeStrategy<?> strategy = STRATEGIES.get(strategyName);
    if (strategy == null) {
      log.warn("Unknown strategy '{}' for field. Using default (standard).", strategyName);
      return STRATEGIES.get("standard");
    }
    return strategy;
  }

  public static Object getFieldValue(Object obj, String fieldName) {
    if (obj == null)
      return null;
    try {
      Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(obj);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      log.debug(
          "Failed to get field value '{}' from object of type '{}'",
          fieldName,
          obj.getClass().getName());
      throw new RuntimeException(
          "Failed to get field value: "
              + fieldName
              + " from object of type "
              + obj.getClass().getName(),
          e);
    }
  }

  public static MergeDefinition toMergeDefinition(ItemMergeDefinition itemMergeDefinition) {
    MergeDefinition mergeDefinition = new MergeDefinition();
    mergeDefinition.setDefinitions(itemMergeDefinition.getDefinitions());
    return mergeDefinition;
  }
}
