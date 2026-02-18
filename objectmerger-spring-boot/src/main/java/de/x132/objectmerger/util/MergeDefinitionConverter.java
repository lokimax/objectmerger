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
 * Converts JSON/Map-based merge definitions to proper MergeDefinition objects
 * using Gson for
 * serialization.
 */
public class MergeDefinitionConverter {

  private static final Gson gson;

  static {
    gson = new GsonBuilder()
        .registerTypeAdapterFactory(
            new com.google.gson.TypeAdapterFactory() {
              @Override
              public <T> com.google.gson.TypeAdapter<T> create(
                  Gson gson, com.google.gson.reflect.TypeToken<T> type) {
                // Only handle the exact FieldDefinition base class, not subclasses!
                if (type.getRawType() != FieldDefinition.class) {
                  return null;
                }

                final com.google.gson.TypeAdapter<com.google.gson.JsonElement> elementAdapter = gson
                    .getAdapter(com.google.gson.JsonElement.class);

                return (com.google.gson.TypeAdapter<T>) new com.google.gson.TypeAdapter<FieldDefinition<?>>() {
                  @Override
                  public void write(
                      com.google.gson.stream.JsonWriter out, FieldDefinition<?> value) {
                    // Serialization is not the focus here, delegate or throw
                    throw new UnsupportedOperationException(
                        "Serialization of abstract FieldDefinition not supported via this factory");
                  }

                  @Override
                  public FieldDefinition<?> read(com.google.gson.stream.JsonReader in)
                      throws java.io.IOException {
                    com.google.gson.JsonElement jsonElement = elementAdapter.read(in);
                    com.google.gson.JsonObject jsonObject = jsonElement.getAsJsonObject();

                    String strategy = jsonObject.has("strategy")
                        ? jsonObject.get("strategy").getAsString()
                        : "standard";

                    Class<? extends FieldDefinition> targetClass;

                    // Dynamic lookup via StrategyRegistry
                    de.x132.objectmerger.strategy.MergeStrategy<?, ?> mergeStrategy = de.x132.objectmerger.registry.StrategyRegistry
                        .getInstance()
                        .getStrategy(strategy);

                    if (mergeStrategy != null) {
                      targetClass = mergeStrategy.getConfigurationClass();
                      // FIX: If strategy returns raw FieldDefinition.class (like
                      // StandardMergeStrategy does),
                      // map it to StandardFieldDefinition.class to avoid recursion or abstract
                      // class instantiation issues.
                      if (targetClass == FieldDefinition.class) {
                        targetClass = StandardFieldDefinition.class;
                      }
                    } else {
                      targetClass = StandardFieldDefinition.class;
                    }

                    // Delegate to the adapter for the specific target class
                    // Since targetClass != FieldDefinition.class (due to check above),
                    // this factory will return null for it, allowing default Gson behavior.
                    return gson.getAdapter(targetClass).fromJsonTree(jsonElement);
                  }
                }.nullSafe();
              }
            })
        .create();
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
