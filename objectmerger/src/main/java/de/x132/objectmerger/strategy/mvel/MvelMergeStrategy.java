package de.x132.objectmerger.strategy.mvel;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;

@Slf4j
public class MvelMergeStrategy implements MergeStrategy<Object, MvelFieldDefinition> {

    private static final String NAME = "mvel";

    @Override
    public Object merge(
            List<LabeledSource<?>> sources, MvelFieldDefinition fieldDef, String fieldName) {
        if (fieldDef == null || fieldDef.getExpression() == null || fieldDef.getExpression().isEmpty()) {
            log.warn("MVEL strategy invoked for field '{}' but no expression provided.", fieldName);
            return null;
        }

        try {
            Serializable compiledExpression = MVEL.compileExpression(fieldDef.getExpression());
            Map<String, Object> context = prepareContext(sources);

            // Also provide raw sources if needed for advanced usage
            context.put("labeledSources", sources);

            return MVEL.executeExpression(compiledExpression, context);
        } catch (Exception e) {
            log.error("Error executing MVEL expression for field '{}': {}", fieldName, e.getMessage());
            // Depending on requirement, we might want to return null, throw, or return a
            // fallback
            return null;
        }
    }

    private Map<String, Object> prepareContext(List<LabeledSource<?>> sources) {
        Map<String, Object> simpleSources = new HashMap<>();
        Map<String, Object> context = new HashMap<>();

        for (LabeledSource<?> source : sources) {
            simpleSources.put(source.getLabel(), source.getSource());
        }

        // Provide 'sources' as a map for easy access in MVEL: sources['priority']
        context.put("sources", simpleSources);
        return context;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<MvelFieldDefinition> getConfigurationClass() {
        return MvelFieldDefinition.class;
    }
}
