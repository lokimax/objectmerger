package de.x132.objectmerger.strategy.graaljs;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.config.GraalJsConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * MergeStrategy implementation using GraalJS.
 *
 * <p>Securely evaluates JavaScript expressions to merge values from multiple sources. Uses a
 * sandboxed GraalJS context via {@link GraalJsHelper} to prevent unauthorized access.
 */
@Slf4j
public class GraalJsMergeStrategy implements MergeStrategy<Object, GraalJsFieldDefinition> {

    public static final String NAME = "graaljs";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<GraalJsFieldDefinition> getConfigurationClass() {
        return GraalJsFieldDefinition.class;
    }

    @Override
    public Object merge(
            List<LabeledSource<?>> sources, GraalJsFieldDefinition fieldDef, String fieldName) {
        return merge(sources, (GraalJsConfig) fieldDef, fieldName, fieldDef.getDefaultValue());
    }

    private Object merge(
            List<LabeledSource<?>> sources,
            GraalJsConfig fieldDef,
            String fieldName,
            Object defaultValue) {
        if (fieldDef == null
                || fieldDef.getExpression() == null
                || fieldDef.getExpression().isEmpty()) {
            log.warn(
                    "GraalJS strategy invoked for field '{}' but no expression provided.",
                    fieldName);
            return defaultValue;
        }

        try (Context context = GraalJsHelper.createSecureContext()) {

            Map<String, Object> simpleSources = new HashMap<>();
            for (LabeledSource<?> source : sources) {
                simpleSources.put(source.getLabel(), source.getSource());
            }

            Value bindings = context.getBindings("js");
            bindings.putMember("sources", simpleSources);
            bindings.putMember("labeledSources", sources);

            Value result = context.eval("js", fieldDef.getExpression());

            return result.as(Object.class);

        } catch (Exception e) {
            log.error(
                    "Error executing GraalJS expression for field '{}': {}",
                    fieldName,
                    e.getMessage());
            return defaultValue;
        }
    }
}
