package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.x132.objectmerger.MergeDefinition;
import java.util.Map;

public class MergeDefinitionConverter {

  private static final Gson gson;

  static {
    gson = new GsonBuilder().registerTypeAdapterFactory(new FieldDefinitionTypeAdapterFactory()).create();
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
