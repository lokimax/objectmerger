package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.list.ListFieldDefinition;
import de.x132.objectmerger.strategy.map.MapFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
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
        de.x132.objectmerger.strategy.FieldDefinition.class,
        (com.google.gson.JsonDeserializer<de.x132.objectmerger.strategy.FieldDefinition<?>>)
            (json, typeOfT, context) -> {
              com.google.gson.JsonObject jsonObject = json.getAsJsonObject();
              String strategy =
                  jsonObject.has("strategy")
                      ? jsonObject.get("strategy").getAsString()
                      : "standard";

              Class<? extends de.x132.objectmerger.strategy.FieldDefinition> targetClass;

              switch (strategy) {
                case "priority":
                  targetClass = PriorityFieldDefinition.class;
                  break;
                case "mergeMap":
                  targetClass = MapFieldDefinition.class;
                  break;
                case "mergeList":
                  targetClass = ListFieldDefinition.class;
                  break;
                case "mvel":
                  targetClass = de.x132.objectmerger.strategy.mvel.MvelFieldDefinition.class;
                  break;
                case "nested":
                  targetClass = de.x132.objectmerger.strategy.nested.NestedFieldDefinition.class;
                  break;
                case "conditional":
                  targetClass =
                      de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition.class;
                  break;
                default:
                  targetClass =
                      de.x132.objectmerger.strategy.standard.StandardFieldDefinition.class;
                  break;
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
