package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.io.IOException;
import java.util.Map;

public class MergeDefinitionConverter {

  private static final Gson gson;

  static {
    gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(
                new TypeAdapterFactory() {
                  @Override
                  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                    if (type.getRawType() != FieldDefinition.class) {
                      return null;
                    }

                    final TypeAdapter<JsonElement> elementAdapter =
                        gson.getAdapter(JsonElement.class);

                    return (TypeAdapter<T>)
                        new TypeAdapter<FieldDefinition<?>>() {
                          @Override
                          public void write(JsonWriter out, FieldDefinition<?> value) {
                            throw new UnsupportedOperationException(
                                "Serialization of abstract FieldDefinition not supported via this factory");
                          }

                          @Override
                          public FieldDefinition<?> read(JsonReader in) throws IOException {
                            JsonElement jsonElement = elementAdapter.read(in);
                            JsonObject jsonObject = jsonElement.getAsJsonObject();

                            String strategy =
                                jsonObject.has("strategy")
                                    ? jsonObject.get("strategy").getAsString()
                                    : "standard";

                            Class<? extends FieldDefinition> targetClass;

                            MergeStrategy<?, ?> mergeStrategy =
                                StrategyRegistry.getInstance().getStrategy(strategy);

                            if (mergeStrategy != null) {
                              targetClass = mergeStrategy.getConfigurationClass();
                              if (targetClass == FieldDefinition.class) {
                                targetClass = StandardFieldDefinition.class;
                              }
                            } else {
                              targetClass = StandardFieldDefinition.class;
                            }

                            return gson.getAdapter(targetClass).fromJsonTree(jsonElement);
                          }
                        }.nullSafe();
                  }
                })
            .create();
  }

  public static MergeDefinition fromMap(Map<String, Map<String, Object>> definitionMap) {
    try {
      String json = gson.toJson(Map.of("definitions", definitionMap));
      return gson.fromJson(json, MergeDefinition.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid merge definition: " + e.getMessage(), e);
    }
  }
}
