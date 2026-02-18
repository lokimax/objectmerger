package de.x132.objectmerger.strategy.mvel;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.config.MvelConfig;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;

@Slf4j
public class MvelMergeStrategy implements MergeStrategy<Object, MvelFieldDefinition> {

  public static final String NAME = "mvel";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Class<MvelFieldDefinition> getConfigurationClass() {
    return MvelFieldDefinition.class;
  }

  @Override
  public Object merge(
      List<LabeledSource<?>> sources, MvelFieldDefinition fieldDef, String fieldName) {
    return merge(sources, (MvelConfig) fieldDef, fieldName, fieldDef.getDefaultValue());
  }

  private Object merge(
      List<LabeledSource<?>> sources, MvelConfig fieldDef, String fieldName, Object defaultValue) {
    if (fieldDef == null
        || fieldDef.getExpression() == null
        || fieldDef.getExpression().isEmpty()) {
      log.warn("MVEL strategy invoked for field '{}' but no expression provided.", fieldName);
      return defaultValue;
    }

    try {
      MvelSandbox.validateExpression(fieldDef.getExpression());

      Serializable compiledExpression =
          MVEL.compileExpression(
              fieldDef.getExpression(), MvelSandbox.createSandboxedParserContext());
      Map<String, Object> context = prepareContext(sources);
      context.put("labeledSources", sources);

      MvelSandbox.validateContextVariables(context);

      return MVEL.executeExpression(compiledExpression, context);
    } catch (SecurityException securityException) {
      log.error(
          "MVEL sandbox violation for field '{}': {}", fieldName, securityException.getMessage());
      throw securityException;
    } catch (Exception e) {
      log.error("Error executing MVEL expression for field '{}': {}", fieldName, e.getMessage());
      return defaultValue;
    }
  }

  private Map<String, Object> prepareContext(List<LabeledSource<?>> sources) {
    Map<String, Object> simpleSources = new HashMap<>();
    Map<String, Object> context = new HashMap<>();

    for (LabeledSource<?> source : sources) {
      simpleSources.put(source.getLabel(), source.getSource());
    }

    context.put("sources", simpleSources);
    return context;
  }
}
