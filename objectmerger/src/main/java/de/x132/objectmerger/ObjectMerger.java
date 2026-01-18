package de.x132.objectmerger;

import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for merging objects based on a definition and strategies.
 *
 * <p>This class uses reflection to iterate over fields and apply specific merge strategies. It
 * supports different strategies for resolving conflicts or combining values from multiple sources.
 *
 * <h2>Strategy Loading</h2>
 *
 * Strategies are loaded via Java SPI (Service Provider Interface). The system looks for
 * implementations of {@link MergeStrategy} registered in {@code
 * META-INF/services/de.x132.objectmerger.strategy.MergeStrategy}. If a strategy specified in the
 * {@link MergeDefinition} is not found, the "standard" strategy is used as a fallback.
 */
@Slf4j
public class ObjectMerger {

  /**
   * Merges multiple sources into a target object based on the provided definition.
   *
   * @param targetClass The class of the result object.
   * @param mergeDefinition The definition of how fields should be merged.
   * @param sources The sources to merge.
   * @param <T> The type of the result object.
   * @return A new instance of T with merged values.
   * @throws RuntimeException If merging fails (e.g. instantiation or field access errors).
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

  /**
   * Merges multiple sources into a target map based on the provided definition.
   *
   * @param mergeDefinition The definition of how fields should be merged.
   * @param sources The sources to merge (Maps).
   * @return A new Map with merged values.
   */
  @SafeVarargs
  public static Map<String, Object> merge(
      MergeDefinition mergeDefinition, LabeledSource<Map<String, Object>>... sources) {
    Map<String, Object> result = new java.util.HashMap<>();
    Map<String, FieldDefinition> definitions = mergeDefinition.getDefinitions();
    List<LabeledSource<Map<String, Object>>> sourceList = Arrays.asList(sources);

    for (Map.Entry<String, FieldDefinition> entry : definitions.entrySet()) {
      String fieldName = entry.getKey();
      FieldDefinition fieldDef = entry.getValue();

      MergeStrategy<?, ?> strategy = resolveStrategy(fieldDef);
      if (strategy != null) {
        // Cast to raw type to allow capture in helper
        @SuppressWarnings("rawtypes")
        MergeStrategy rawStrategy = strategy;
        Object mergedValue = executeStrategy(rawStrategy, sourceList, fieldDef, fieldName);
        result.put(fieldName, mergedValue);
      }
    }
    return result;
  }

  private static <T> void processField(
      MergeContext<T> context, String fieldName, FieldDefinition fieldDef) {
    try {
      Field field = context.targetClass().getDeclaredField(fieldName);
      field.setAccessible(true);

      MergeStrategy<?, ?> strategy = resolveStrategy(fieldDef);
      if (strategy != null) {
        // Cast to raw type to allow capture in helper
        @SuppressWarnings("rawtypes")
        MergeStrategy rawStrategy = strategy;
        applyStrategy(rawStrategy, context.sources(), fieldDef, fieldName, field, context.result());
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

  private static MergeStrategy<?, ?> resolveStrategy(FieldDefinition fieldDef) {
    String strategyName = fieldDef.getStrategy() != null ? fieldDef.getStrategy() : "standard";
    return StrategyRegistry.getInstance().getStrategy(strategyName);
  }

  private static <T, C extends FieldDefinition> void applyStrategy(
      MergeStrategy<T, C> strategy,
      List<? extends LabeledSource<?>> sources,
      FieldDefinition fieldDef,
      String fieldName,
      Field field,
      Object result)
      throws IllegalAccessException {

    T mergedValue = executeStrategy(strategy, sources, fieldDef, fieldName);
    field.set(result, mergedValue);
  }

  @SuppressWarnings("unchecked")
  private static <T, C extends FieldDefinition> T executeStrategy(
      MergeStrategy<T, C> strategy,
      List<? extends LabeledSource<?>> sources,
      FieldDefinition fieldDef,
      String fieldName) {

    if (!strategy.getConfigurationClass().isInstance(fieldDef)) {
      throw new IllegalArgumentException(
          String.format(
              "Field '%s' requires configuration of type '%s' but got '%s' for strategy '%s'",
              fieldName,
              strategy.getConfigurationClass().getSimpleName(),
              fieldDef.getClass().getSimpleName(),
              strategy.getName()));
    }

    C config = (C) fieldDef;
    return strategy.merge(new java.util.ArrayList<>(sources), config, fieldName);
  }

  public static Object getFieldValue(Object obj, String fieldName) {
    if (obj == null) return null;
    if (obj instanceof Map) {
      return ((Map<?, ?>) obj).get(fieldName);
    }

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
