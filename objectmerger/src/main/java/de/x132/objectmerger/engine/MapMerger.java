package de.x132.objectmerger.engine;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.exception.ConfigurationException;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Handles merging of Map-based objects. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapMerger {

  @SafeVarargs
  public static Map<String, Object> merge(
      MergeDefinition mergeDefinition, LabeledSource<Map<String, Object>>... sources) {
    Map<String, Object> result = new HashMap<>();
    Map<String, FieldDefinition<?>> definitions = mergeDefinition.getDefinitions();
    List<LabeledSource<Map<String, Object>>> sourceList = Arrays.asList(sources);

    for (Map.Entry<String, FieldDefinition<?>> entry : definitions.entrySet()) {
      String fieldName = entry.getKey();
      FieldDefinition<?> fieldDef = entry.getValue();

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

  private static MergeStrategy<?, ?> resolveStrategy(FieldDefinition<?> fieldDef) {
    String strategyName = fieldDef.getStrategy() != null ? fieldDef.getStrategy() : "standard";
    return StrategyRegistry.getInstance().getStrategy(strategyName);
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
