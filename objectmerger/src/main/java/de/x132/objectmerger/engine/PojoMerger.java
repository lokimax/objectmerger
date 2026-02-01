package de.x132.objectmerger.engine;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeContext;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.exception.ConfigurationException;
import de.x132.objectmerger.exception.MergeExecutionException;
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

      // Template Mode Logic
      Map<String, FieldDefinition<?>> definitions;

      if (mergeDefinition.getTemplateSourceLabel() != null) {
        // 1. Find template source
        LabeledSource<T> templateSource =
            findTemplateSource(mergeDefinition.getTemplateSourceLabel(), sources);
        if (templateSource == null) {
          throw new de.x132.objectmerger.exception.InvalidSourceException(
              "Template source '"
                  + mergeDefinition.getTemplateSourceLabel()
                  + "' not found among provided sources.");
        }

        // 2. Generate base definition
        // We need to support Pojo generation in Generator first, assuming it exists or
        // needs update
        MergeDefinition generatedDef =
            de.x132.objectmerger.generator.MergeDefinitionGenerator.generate(targetClass);
        // Note: Generator uses Class for POJOs currently.
        // If we want dynamic instance-based generation for POJOs, we might need to
        // enhance Generator.
        // For now, let's use the class-based generator which matches what we have.
        // Wait, the requirement was "dynamic schema from source".
        // If it's a POJO, the schema IS the class. If it's a Map, the schema IS the
        // keys.

        // Let's assume for POJO merging, we simply want to ensure we have definitions
        // for all fields
        // derived from the class (Standard behavior), but overlay with specific config.
        // Actually, PojoMerger already iterates over 'definitions.entrySet()'.
        // If 'definitions' is empty/partial, only those fields are merged.
        // Template Mode for POJO means: "Fill definitions with all fields from class"

        definitions = generatedDef.getDefinitions();

        // 3. Overlay explicit configuration
        if (mergeDefinition.getDefinitions() != null) {
          definitions.putAll(mergeDefinition.getDefinitions());
        }

      } else {
        definitions = mergeDefinition.getDefinitions();
      }

      List<LabeledSource<T>> sourceList = Arrays.asList(sources);
      MergeContext<T> context = new MergeContext<>(targetClass, result, sourceList);

      for (Map.Entry<String, FieldDefinition<?>> entry : definitions.entrySet()) {
        processField(context, entry.getKey(), entry.getValue());
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to merge objects of type {}", targetClass.getName(), e);
      if (e instanceof de.x132.objectmerger.exception.ObjectMergerException) {
        throw (de.x132.objectmerger.exception.ObjectMergerException) e;
      }
      throw new MergeExecutionException(
          "Failed to merge objects of type " + targetClass.getName(), e);
    }
  }

  private static <T> LabeledSource<T> findTemplateSource(String label, LabeledSource<T>[] sources) {
    for (LabeledSource<T> source : sources) {
      if (source.getLabel().equals(label)) {
        return source;
      }
    }
    return null;
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
      throw new ConfigurationException(
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
