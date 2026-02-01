package de.x132.objectmerger.engine;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeContext;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.helper.ReflectionHelper;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Handles merging of generic POJOs (Plain Old Java Objects). */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PojoMerger {

  @SafeVarargs
  public static <T> T merge(
      Class<T> targetClass, MergeDefinition mergeDefinition, LabeledSource<T>... sources) {
    try {
      T result = targetClass.getDeclaredConstructor().newInstance();
      Map<String, FieldDefinition<?>> definitions = mergeDefinition.getDefinitions();
      List<LabeledSource<T>> sourceList = Arrays.asList(sources);
      MergeContext<T> context = new MergeContext<>(targetClass, result, sourceList);

      for (Map.Entry<String, FieldDefinition<?>> entry : definitions.entrySet()) {
        processField(context, entry.getKey(), entry.getValue());
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to merge objects of type {}", targetClass.getName(), e);
      throw new RuntimeException("Failed to merge objects of type " + targetClass.getName(), e);
    }
  }

  private static <T> void processField(
      MergeContext<T> context, String fieldName, FieldDefinition<?> fieldDef) {

    Field field = ReflectionHelper.getField(context.targetClass(), fieldName);
    if (field == null) {
      // Field missing in class, already logged by helper
      return;
    }

    MergeStrategy<?, ?> strategy = resolveStrategy(fieldDef);
    if (strategy != null) {
      // Cast to raw type to allow capture in helper
      @SuppressWarnings("rawtypes")
      MergeStrategy rawStrategy = strategy;
      applyStrategy(rawStrategy, context.sources(), fieldDef, fieldName, field, context.result());
    }
  }

  private static MergeStrategy<?, ?> resolveStrategy(FieldDefinition<?> fieldDef) {
    String strategyName = fieldDef.getStrategy() != null ? fieldDef.getStrategy() : "standard";
    return StrategyRegistry.getInstance().getStrategy(strategyName);
  }

  private static <T, C extends FieldDefinition<T>> void applyStrategy(
      MergeStrategy<T, C> strategy,
      List<? extends LabeledSource<?>> sources,
      FieldDefinition<?> fieldDef,
      String fieldName,
      Field field,
      Object result) {

    T mergedValue = executeStrategy(strategy, sources, fieldDef, fieldName);
    ReflectionHelper.setFieldValue(field, result, mergedValue);
  }

  @SuppressWarnings("unchecked")
  private static <T, C extends FieldDefinition<T>> T executeStrategy(
      MergeStrategy<T, C> strategy,
      List<? extends LabeledSource<?>> sources,
      FieldDefinition<?> fieldDef,
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
}
