package de.x132;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ObjectMerger {

    private static final Gson gson = new Gson();

    public static String merge(String json1, String label1, String json2, String label2, String mergeDefinition) {
        JsonElement node1 = JsonParser.parseString(json1);
        JsonElement node2 = JsonParser.parseString(json2);
        JsonElement definition = JsonParser.parseString(mergeDefinition);

        JsonElement resultNode = mergeNodes(node1.getAsJsonObject(), label1, node2.getAsJsonObject(), label2, definition.getAsJsonObject().get("definitions").getAsJsonObject());
        return gson.toJson(resultNode);
    }

    private static JsonObject mergeNodes(JsonObject node1, String label1, JsonObject node2, String label2, JsonObject definition) {
        JsonObject resultNode = new JsonObject();
        
        for (Entry<String, JsonElement> entry : definition.entrySet()) {
            String fieldName = entry.getKey();
            JsonObject fieldDefinition = entry.getValue().getAsJsonObject();

            if (fieldDefinition.has("strategy")) {
                String strategy = fieldDefinition.get("strategy").getAsString();
                if ("mergeList".equals(strategy)) {
                    resultNode.add(fieldName, mergeList(
                            node1.getAsJsonArray(fieldName), label1,
                            node2.getAsJsonArray(fieldName), label2,
                            fieldDefinition
                    ));
                } else if ("maximum".equals(strategy)) {
                    JsonObject priority = fieldDefinition.get("priority").getAsJsonObject();
                    String preferredLabel = getPreferredLabel(label1, label2, priority);
                    JsonElement value1 = node1.get(fieldName);
                    JsonElement value2 = node2.get(fieldName);

                    if(value1 == null || value1.isJsonNull()) {
                        resultNode.add(fieldName, value2);
                    } else if (value2 == null || value2.isJsonNull()) {
                        resultNode.add(fieldName, value1);
                    }
                    else if(value1.getAsDouble() > value2.getAsDouble()) {
                        resultNode.add(fieldName, value1);
                    }
                    else {
                        resultNode.add(fieldName, value2);
                    }
                }
            } else {
                String preferredLabel = getPreferredLabel(label1, label2, fieldDefinition);
                if (preferredLabel.equals(label1) && node1.has(fieldName)) {
                    resultNode.add(fieldName, node1.get(fieldName));
                } else if (node2.has(fieldName)) {
                    resultNode.add(fieldName, node2.get(fieldName));
                }
            }
        }
        return resultNode;
    }

    private static String getPreferredLabel(String label1, String label2, JsonObject priorityDefinition) {
        int priority1 = priorityDefinition.has(label1) ? priorityDefinition.get(label1).getAsInt() : Integer.MAX_VALUE;
        int priority2 = priorityDefinition.has(label2) ? priorityDefinition.get(label2).getAsInt() : Integer.MAX_VALUE;
        return priority1 <= priority2 ? label1 : label2;
    }

    private static JsonArray mergeList(JsonArray list1, String label1, JsonArray list2, String label2, JsonObject definition) {
        JsonArray mergedList = new JsonArray();
        String identifyBy = definition.get("identifyBy").getAsString();

        Map<String, JsonElement> map1 = new HashMap<>();
        if (list1 != null) {
            for (JsonElement item : list1) {
                map1.put(item.getAsJsonObject().get(identifyBy).getAsString(), item);
            }
        }

        Map<String, JsonElement> map2 = new HashMap<>();
        if (list2 != null) {
            for (JsonElement item : list2) {
                map2.put(item.getAsJsonObject().get(identifyBy).getAsString(), item);
            }
        }

        Set<String> allKeys = new HashSet<>(map1.keySet());
        allKeys.addAll(map2.keySet());

        String preferredLabel = getPreferredLabel(label1, label2, definition.get("priority").getAsJsonObject());

        for (String key : allKeys) {
            JsonElement item1 = map1.get(key);
            JsonElement item2 = map2.get(key);
            JsonObject itemDefinition = definition.get("itemMergeDefinition").getAsJsonObject();
            
            if (item1 != null && item2 != null) {
                mergedList.add(mergeNodes(item1.getAsJsonObject(), label1, item2.getAsJsonObject(), label2, itemDefinition.get("definitions").getAsJsonObject()));
            } else if (item1 != null) {
                 if (preferredLabel.equals(label1)) {
                    mergedList.add(item1);
                 }
            } else if (item2 != null) {
                if (preferredLabel.equals(label2)) {
                    mergedList.add(item2);
                }
            }
        }
        return mergedList;
    }
}
