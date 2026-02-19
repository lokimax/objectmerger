package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.io.IOException;

public class FieldDefinitionTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() != FieldDefinition.class) {
            return null;
        }
        return (TypeAdapter<T>) new FieldDefinitionAdapter(gson).nullSafe();
    }

    private static class FieldDefinitionAdapter extends TypeAdapter<FieldDefinition<?>> {
        private final Gson gson;
        private final TypeAdapter<JsonElement> elementAdapter;

        private FieldDefinitionAdapter(Gson gson) {
            this.gson = gson;
            this.elementAdapter = gson.getAdapter(JsonElement.class);
        }

        @Override
        public void write(JsonWriter out, FieldDefinition<?> value) {
            throw new UnsupportedOperationException(
                    "Serialization to FieldDefinition not supported");
        }

        @Override
        public FieldDefinition<?> read(JsonReader in) throws IOException {
            JsonElement jsonElement = elementAdapter.read(in);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String strategy =
                    jsonObject.has("strategy")
                            ? jsonObject.get("strategy").getAsString()
                            : "standard";

            Class<? extends FieldDefinition> targetClass = StandardFieldDefinition.class;

            MergeStrategy<?, ?> mergeStrategy =
                    StrategyRegistry.getInstance().getStrategy(strategy);

            if (mergeStrategy != null) {
                Class<? extends FieldDefinition> configClass =
                        mergeStrategy.getConfigurationClass();
                if (configClass != FieldDefinition.class) {
                    targetClass = configClass;
                }
            }

            return gson.getAdapter(targetClass).fromJsonTree(jsonElement);
        }
    }
}
