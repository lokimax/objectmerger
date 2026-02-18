package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition;
import de.x132.objectmerger.strategy.list.ListFieldDefinition;
import de.x132.objectmerger.strategy.map.MapFieldDefinition;
import de.x132.objectmerger.strategy.nested.NestedFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
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
                  try {
                    targetClass =
                        (Class<? extends FieldDefinition>)
                            Class.forName("de.x132.objectmerger.strategy.mvel.MvelFieldDefinition");
                  } catch (ClassNotFoundException e) {
                    // Fallback or throw? If user requested 'mvel', throw.
                    throw new IllegalArgumentException(
                        "MVEL strategy requested but 'objectmerger-mvel' dependency is missing.");
                  }
                  break;
                case "nested":
                  targetClass = NestedFieldDefinition.class;
                  break;
                case "conditional":
                  targetClass = ConditionalFieldDefinition.class;
                  break;
                default:
                  targetClass = StandardFieldDefinition.class;
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
