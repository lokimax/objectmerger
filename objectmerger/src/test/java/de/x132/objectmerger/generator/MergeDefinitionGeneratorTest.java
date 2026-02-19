package de.x132.objectmerger.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.list.ListFieldDefinition;
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

    @Test
    void generateFromMapWithList() {
        Map<String, Object> item1 = Map.of("id", 1, "name", "Item1");
        Map<String, Object> item2 = Map.of("id", 2, "name", "Item2");
        Map<String, Object> data = Map.of("items", java.util.List.of(item1, item2));

        MergeDefinition def = MergeDefinitionGenerator.generate(data);

        assertNotNull(def);
        assertTrue(def.getDefinitions().containsKey("items"));

        FieldDefinition fieldDef = def.getDefinitions().get("items");
        assertTrue(fieldDef instanceof ListFieldDefinition);

        ListFieldDefinition listDef = (ListFieldDefinition) fieldDef;

        assertEquals("mergeList", listDef.getStrategy());
        assertEquals("id", listDef.getIdentifyBy());
    }
}
