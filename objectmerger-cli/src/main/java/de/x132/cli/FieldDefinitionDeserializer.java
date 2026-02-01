package de.x132.cli;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.lang.reflect.Type;

public class FieldDefinitionDeserializer implements JsonDeserializer<FieldDefinition<?>> {

  @Override
  public FieldDefinition<?> deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

    JsonObject jsonObject = json.getAsJsonObject();
    String strategyName =
        jsonObject.has("strategy") ? jsonObject.get("strategy").getAsString() : "standard";

    MergeStrategy<?, ?> strategy = StrategyRegistry.getInstance().getStrategy(strategyName);

    // The configuration class for the strategy
    Class<? extends FieldDefinition> configClass = strategy.getConfigurationClass();

    // Prevent infinite recursion if the strategy returns the abstract base class
    if (configClass == FieldDefinition.class) {
      configClass = StandardFieldDefinition.class;
    }

    return context.deserialize(json, configClass);
  }
}
