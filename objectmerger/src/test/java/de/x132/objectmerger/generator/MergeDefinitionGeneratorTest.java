package de.x132.objectmerger.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MergeDefinitionGeneratorTest {

  @SuppressWarnings("unused")
  private static class TestClass {
    private String name;
    private int age;
  }

  @Test
  void generateFromClass() {
    MergeDefinition def = MergeDefinitionGenerator.generate(TestClass.class);

    assertNotNull(def);
    assertNotNull(def.getDefinitions());
    assertEquals(2, def.getDefinitions().size());

    assertTrue(def.getDefinitions().containsKey("name"));
    assertTrue(def.getDefinitions().containsKey("age"));

    assertTrue(def.getDefinitions().get("name") instanceof StandardFieldDefinition);
  }

  @Test
  void generateFromMap() {
    Map<String, Object> data = Map.of("key1", "value1", "key2", 123);

    MergeDefinition def = MergeDefinitionGenerator.generate(data);

    assertNotNull(def);
    assertNotNull(def.getDefinitions());
    assertEquals(2, def.getDefinitions().size());

    assertTrue(def.getDefinitions().containsKey("key1"));
    assertTrue(def.getDefinitions().containsKey("key2"));
  }
}
