package de.x132.objectmerger.strategy.nashorn;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.exception.ConfigurationException;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.conditional.ConditionCase;
import de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * Conditional MergeStrategy using Nashorn expressions.
 *
 * @param <T> the type of the result
 */
@Slf4j
public class NashornConditionalStrategy<T>
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
            List<LabeledSource<?>> sources,
            ConditionalFieldDefinition<T> fieldDef,
            String fieldName) {
        return (T) mergeInternal(sources, fieldDef, fieldName);
    }

    private Object mergeInternal(
            List<LabeledSource<?>> sources,
            ConditionalFieldDefinition<T> fieldDef,
            String fieldName) {

        Map<String, Object> values = new HashMap<>();
        for (LabeledSource<?> s : sources) {
            values.put(s.getLabel(), ObjectMerger.getFieldValue(s.getSource(), fieldName));
        }

        try {
            ScriptEngine engine = NashornHelper.createSecureEngine();

            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("values", values);
            bindings.put("sources", sources);

            if (fieldDef.getCases() != null) {
                for (ConditionCase<?> c : fieldDef.getCases()) {
                    try {
                        Object result = engine.eval(c.getCondition());

                        if (result instanceof Boolean && (Boolean) result) {
                            log.debug(
                                    "Condition '{}' matched for field '{}'",
                                    c.getCondition(),
                                    fieldName);
                            return executeSubStrategy(c.getUseStrategy(), sources, fieldName);
                        }
                    } catch (Exception e) {
                        log.warn(
                                "Failed to evaluate Nashorn condition '{}' for field '{}': {}",
                                c.getCondition(),
                                fieldName,
                                e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error(
                    "Error setting up Nashorn context for conditional strategy: {}",
                    e.getMessage());
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
            throw new ConfigurationException(
                    "Unknown strategy in conditional case: " + strategyName);
        }

        return strategy.merge(sources, def, fieldName);
    }
}
