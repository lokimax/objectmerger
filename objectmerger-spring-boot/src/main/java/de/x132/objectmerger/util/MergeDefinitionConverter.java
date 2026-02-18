package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.Map;

/**
 * Converts JSON/Map-based merge definitions to proper MergeDefinition objects using Gson for
 * serialization.
 */
public class MergeDefinitionConverter {

  private static final Gson gson;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(
        FieldDefinition.class,
        (JsonDeserializer<FieldDefinition<?>>)
            (json, typeOfT, context) -> {
              JsonObject jsonObject = json.getAsJsonObject();
              String strategy =
                  jsonObject.has("strategy")
                      ? jsonObject.get("strategy").getAsString()
                      : "standard";

              Class<? extends FieldDefinition> targetClass;

              // Dynamic lookup via StrategyRegistry (OCP compliant)
              MergeStrategy<?, ?> mergeStrategy =
                  StrategyRegistry.getInstance().getStrategy(strategy);

              if (mergeStrategy != null) {
                targetClass = mergeStrategy.getConfigurationClass();
              } else {
                targetClass = StandardFieldDefinition.class;
              }

              return context.deserialize(json, targetClass);
            });
    gson = builder.create();
  }

  public static MergeDefinition fromMap(Map<String, Map<String, Object>> definitionMap) {
    try {
      // Convert map to JSON string
      String json = gson.toJson(Map.of("definitions", definitionMap));
      // Parse back to MergeDefinition
      return gson.fromJson(json, MergeDefinition.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid merge definition: " + e.getMessage(), e);
    }
  }
}
