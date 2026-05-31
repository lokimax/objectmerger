package de.x132.objectmerger.strategy.nashorn;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.config.NashornConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import lombok.extern.slf4j.Slf4j;

/** MergeStrategy implementation using Nashorn. */
@Slf4j
public class NashornMergeStrategy implements MergeStrategy<Object, NashornFieldDefinition> {

    public static final String NAME = "nashorn";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<NashornFieldDefinition> getConfigurationClass() {
        return NashornFieldDefinition.class;
    }

    @Override
    public Object merge(
            List<LabeledSource<?>> sources, NashornFieldDefinition fieldDef, String fieldName) {
        return merge(sources, (NashornConfig) fieldDef, fieldName, fieldDef.getDefaultValue());
    }

    private Object merge(
            List<LabeledSource<?>> sources,
            NashornConfig fieldDef,
            String fieldName,
            Object defaultValue) {
        if (fieldDef == null
                || fieldDef.getExpression() == null
                || fieldDef.getExpression().isEmpty()) {
            log.warn(
                    "Nashorn strategy invoked for field '{}' but no expression provided.",
                    fieldName);
            return defaultValue;
        }

        try {
            ScriptEngine engine = NashornHelper.createSecureEngine();

            Map<String, Object> simpleSources = new HashMap<>();
            for (LabeledSource<?> source : sources) {
                simpleSources.put(source.getLabel(), source.getSource());
            }

            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("sources", simpleSources);
            bindings.put("labeledSources", sources);

            Object result = engine.eval(fieldDef.getExpression());

            // Unpack Nashorn specific wrapper if needed, but Nashorn evaluates to standard Java
            // objects mapping mostly.

            return result;

        } catch (Exception e) {
            log.error(
                    "Error executing Nashorn expression for field '{}': {}",
                    fieldName,
                    e.getMessage());
            return defaultValue;
        }
    }
}
