package de.x132.testutil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.list.ListFieldDefinition;
import de.x132.objectmerger.strategy.map.MapFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.lang.reflect.Type;

public class TestGsonHelper {

  public static Gson createGson() {
    return new GsonBuilder()
        .registerTypeAdapter(FieldDefinition.class, new FieldDefinitionDeserializer())
        .create();
  }

  private static class FieldDefinitionDeserializer implements JsonDeserializer<FieldDefinition> {
    @Override
    public FieldDefinition deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();

      String strategy = "";
      if (jsonObject.has("strategy")) {
        strategy = jsonObject.get("strategy").getAsString();
      }

      if ("mergeMap".equals(strategy) || jsonObject.has("keyTemplateSources")) {
        return context.deserialize(json, MapFieldDefinition.class);
      }

      if ("mergeList".equals(strategy) || jsonObject.has("identifyBy")) {
        return context.deserialize(json, ListFieldDefinition.class);
      }

      if (jsonObject.has("priority")
          || "priority".equals(strategy)
          || "concatenate".equals(strategy)) {
        return context.deserialize(json, PriorityFieldDefinition.class);
      }

      if ("mvel".equals(strategy)) {
        return context.deserialize(json, de.x132.objectmerger.strategy.mvel.MvelFieldDefinition.class);
      }

      return context.deserialize(json, StandardFieldDefinition.class);
    }
  }
}
