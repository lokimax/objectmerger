package de.x132.family;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.registry.StrategyRegistry;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Family Merge Scenario (JSON Integration)")
class ListMergeTest {

  private final Gson gson;

  public ListMergeTest() {
    this.gson =
        new GsonBuilder()
            .registerTypeAdapter(FieldDefinition.class, new TestFieldDefinitionDeserializer())
            .registerTypeAdapter(MergeDefinition.class, new TestMergeDefinitionDeserializer())
            .registerTypeAdapter(
                ItemMergeDefinition.class, new TestItemMergeDefinitionDeserializer())
            .create();
  }

  @Test
  @DisplayName("Should merge two families and apply API updates based on Template logic")
  void testFamilyMergeScenario() throws IOException {
    // 1. Load Data
    Map<String, Object> familyA = loadJson("scenarios/family_merging/family_a.json");
    Map<String, Object> familyB = loadJson("scenarios/family_merging/family_b.json");
    Map<String, Object> apiUpdate = loadJson("scenarios/family_merging/api_update.json");
    MergeDefinition definition = loadDefinition("scenarios/family_merging/definition.json");

    // 2. Wrap Sources
    LabeledSource<Map<String, Object>> sourceA = new LabeledSource<>("family_a", familyA);
    LabeledSource<Map<String, Object>> sourceB = new LabeledSource<>("family_b", familyB);
    LabeledSource<Map<String, Object>> sourceApi = new LabeledSource<>("api", apiUpdate);

    // 3. Merge
    // Note: We are merging Maps, so the result is a Map
    @SuppressWarnings("unchecked")
    Map<String, Object> result = ObjectMerger.merge(definition, sourceA, sourceB, sourceApi);

    // 4. Verify
    assertNotNull(result);
    assertTrue(result.containsKey("members"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> members = (List<Map<String, Object>>) result.get("members");

    // Expecting 4 members: Peter, Mary (from A), Klaus, Gabi (from B).
    // Grandpa (from API) should be filtered out because API is not in
    // keyOriginLabels.
    assertEquals(4, members.size(), "Should have 4 members (Grandpa filtered out)");

    // Verify Peter (p1) has updated birthdate from API
    Map<String, Object> peter = findById(members, "p1");
    assertEquals("Peter", peter.get("name"));
    assertEquals(
        "1980-01-02",
        peter.get("birthdate"),
        "Peter should have updated birthdate from API (Priority 10)");

    // Verify Klaus (p2) is present
    Map<String, Object> klaus = findById(members, "p2");
    assertNotNull(klaus);
    assertEquals("Klaus", klaus.get("name"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> loadJson(String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      if (is == null) throw new IOException("Resource not found: " + path);
      return gson.fromJson(
          new InputStreamReader(is), new TypeToken<Map<String, Object>>() {}.getType());
    }
  }

  private MergeDefinition loadDefinition(String path) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      if (is == null) throw new IOException("Resource not found: " + path);
      return gson.fromJson(new InputStreamReader(is), MergeDefinition.class);
    }
  }

  private Map<String, Object> findById(List<Map<String, Object>> list, String id) {
    return list.stream()
        .filter(m -> id.equals(m.get("id")))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Member with id " + id + " not found in: " + list));
  }

  // Inner Deserializer class
  public static class TestFieldDefinitionDeserializer
      implements JsonDeserializer<FieldDefinition<?>> {
    @Override
    public FieldDefinition<?> deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      JsonObject jsonObject = json.getAsJsonObject();
      String strategyName =
          jsonObject.has("strategy") ? jsonObject.get("strategy").getAsString() : "standard";

      MergeStrategy<?, ?> strategy = StrategyRegistry.getInstance().getStrategy(strategyName);
      if (strategy == null) {
        // Fallback or error? For test, let's use standard if unknown (should not happen
        // in valid test)
        // Actually, if registry is empty this fails. Registry is singleton, ensures
        // standard strategies loaded?
        // StrategyRegistry usually loads from ServiceLoader.
        // Since this is a unit test, we rely on SPI file being present in
        // src/main/resources/META-INF...
        // which it IS in objectmerger module.
        throw new JsonParseException("Unknown strategy: " + strategyName);
      }

      Class<? extends FieldDefinition> configClass = strategy.getConfigurationClass();

      if (configClass == FieldDefinition.class) {
        configClass = StandardFieldDefinition.class;
      }

      return context.deserialize(json, configClass);
    }
  }

  // Inner Deserializer class for MergeDefinition (Flat Map -> Object)
  public static class TestMergeDefinitionDeserializer implements JsonDeserializer<MergeDefinition> {
    @Override
    public MergeDefinition deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      JsonObject jsonObject = json.getAsJsonObject();
      Map<String, FieldDefinition<?>> map = new HashMap<>();

      for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        FieldDefinition<?> def = context.deserialize(entry.getValue(), FieldDefinition.class);
        map.put(entry.getKey(), def);
      }

      return new MergeDefinition(map);
    }
  }

  // Inner Deserializer class for ItemMergeDefinition (Flat Map -> Object)
  public static class TestItemMergeDefinitionDeserializer
      implements JsonDeserializer<ItemMergeDefinition> {
    @Override
    public ItemMergeDefinition deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      JsonObject jsonObject = json.getAsJsonObject();
      Map<String, FieldDefinition<?>> map = new HashMap<>();
      String targetClass = null;

      if (jsonObject.has("targetClass")) {
        targetClass = jsonObject.get("targetClass").getAsString();
        // Dont modify original object if possible, but iterating and skipping is safer
      }

      for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        if ("targetClass".equals(entry.getKey())) {
          continue; // Handled above
        }
        FieldDefinition<?> def = context.deserialize(entry.getValue(), FieldDefinition.class);
        map.put(entry.getKey(), def);
      }

      ItemMergeDefinition itemDef = new ItemMergeDefinition();
      itemDef.setDefinitions(map);
      itemDef.setTargetClass(targetClass);
      return itemDef;
    }
  }
}
