package de.x132.objectmerger.strategy.conditional;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.exception.ConfigurationException;

import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.config.ConditionalConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalMergeStrategy<T>
    implements MergeStrategy<T, ConditionalFieldDefinition<T>> {

  public static final String NAME = "conditional";

  @Override
  public String getName() {
    return NAME;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<ConditionalFieldDefinition<T>> getConfigurationClass() {
    return (Class) ConditionalFieldDefinition.class;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T merge(
      List<LabeledSource<?>> sources, ConditionalFieldDefinition<T> fieldDef, String fieldName) {
    return (T) mergeInternal(sources, fieldDef, fieldName);
  }

  private Object mergeInternal(
      List<LabeledSource<?>> sources, ConditionalConfig<T> fieldDef, String fieldName) {

    Map<String, Object> context = new HashMap<>();
    context.put("sources", sources);
    Map<String, Object> values = new HashMap<>();
    for (LabeledSource<?> s : sources) {
      values.put(s.getLabel(), ObjectMerger.getFieldValue(s.getSource(), fieldName));
    }
    if (fieldDef.getCases() != null) {
      for (ConditionCase<?> c : fieldDef.getCases()) {
        try {
          // Add values to context for easier access (e.g. values['source1'])
          // MVEL expression example: "values['source1'] == 'admin'"
          context.put("values", values);

          Object result = de.x132.objectmerger.strategy.mvel.MvelSandbox.evaluate(c.getCondition(), context);

          if (result instanceof Boolean && (Boolean) result) {
            log.debug("Condition '{}' matched for field '{}'", c.getCondition(), fieldName);
            return executeSubStrategy(c.getUseStrategy(), sources, fieldName);
          }
        } catch (Exception e) {
          log.warn(
              "Failed to evaluate condition '{}' for field '{}': {}",
              c.getCondition(),
              fieldName,
              e.getMessage());
          // Continue to next case or default
        }
      }
    }

    if (fieldDef.getDefaultStrategy() != null) {
      log.debug("No condition matched for field '{}', using default strategy", fieldName);
      return executeSubStrategy(fieldDef.getDefaultStrategy(), sources, fieldName);
    }

    return null;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Object executeSubStrategy(
      FieldDefinition def, List<LabeledSource<?>> sources, String fieldName) {
    String strategyName = def.getStrategy() != null ? def.getStrategy() : "standard";
    MergeStrategy strategy = StrategyRegistry.getInstance().getStrategy(strategyName);

    if (strategy == null) {
      throw new ConfigurationException("Unknown strategy in conditional case: " + strategyName);
    }

    return strategy.merge(sources, def, fieldName);
  }
}
