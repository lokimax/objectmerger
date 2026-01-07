package de.x132.objectmerger.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.x132.FieldDefinition;
import de.x132.MergeDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts JSON/Map-based merge definitions to proper MergeDefinition objects
 * using Gson for serialization.
 */
public class MergeDefinitionConverter {

    private static final Gson gson = new GsonBuilder().create();

    /**
     * Convert a Map<String, Map<String, Object>> to a proper MergeDefinition.
     * Uses Gson to handle the conversion via JSON serialization.
     */
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
