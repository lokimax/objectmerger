package de.x132.objectmerger.util;

import static org.junit.jupiter.api.Assertions.*;

import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MergeDefinitionConverter Tests")
class MergeDefinitionConverterTest {

    @Test
    @DisplayName("Should convert simple priority definition")
    void testConvertSimplePriorityDefinition() {
        Map<String, Map<String, Object>> input = new LinkedHashMap<>();
        input.put(
                "name",
                Map.of("strategy", "priority", "priority", Map.of("source1", 1, "source2", 2)));

        MergeDefinition result = MergeDefinitionConverter.fromMap(input);

        assertNotNull(result);
        assertNotNull(result.getDefinitions());
        assertTrue(result.getDefinitions().containsKey("name"));

        PriorityFieldDefinition nameField =
                (PriorityFieldDefinition) result.getDefinitions().get("name");
        assertNotNull(nameField);
        assertNotNull(nameField.getPriority());
        assertEquals(1, nameField.getPriority().get("source1"));
        assertEquals(2, nameField.getPriority().get("source2"));
    }

    @Test
    @DisplayName("Should convert strategy definition")
    void testConvertStrategyDefinition() {
        Map<String, Map<String, Object>> input = new LinkedHashMap<>();
        input.put("age", Map.of("strategy", "maximum", "defaultValue", 0));

        MergeDefinition result = MergeDefinitionConverter.fromMap(input);

        assertNotNull(result);
        assertNotNull(result.getDefinitions());
        assertTrue(result.getDefinitions().containsKey("age"));

        FieldDefinition ageField = (FieldDefinition) result.getDefinitions().get("age");
        assertNotNull(ageField);
        assertEquals("maximum", ageField.getStrategy());
        assertNotNull(ageField.getDefaultValue());
    }

    @Test
    @DisplayName("Should convert multiple field definitions")
    void testConvertMultipleFields() {
        Map<String, Map<String, Object>> input = new LinkedHashMap<>();
        input.put(
                "name",
                Map.of(
                        "strategy",
                        "priority",
                        "priority",
                        Map.of("database", 1, "crm", 2, "analytics", 3)));
        input.put("age", Map.of("strategy", "maximum"));
        input.put("email", Map.of("strategy", "priority", "priority", Map.of("database", 1)));

        MergeDefinition result = MergeDefinitionConverter.fromMap(input);

        assertNotNull(result);
        assertNotNull(result.getDefinitions());
        assertEquals(3, result.getDefinitions().size());
        assertTrue(result.getDefinitions().containsKey("name"));
        assertTrue(result.getDefinitions().containsKey("age"));
        assertTrue(result.getDefinitions().containsKey("email"));
    }

    @Test
    @DisplayName("Should handle empty definition map")
    void testConvertEmptyDefinition() {
        Map<String, Map<String, Object>> input = new LinkedHashMap<>();

        MergeDefinition result = MergeDefinitionConverter.fromMap(input);

        assertNotNull(result);
        assertNotNull(result.getDefinitions());
        assertTrue(result.getDefinitions().isEmpty());
    }

    @Test
    @DisplayName("Should convert all merge strategies")
    void testConvertAllStrategies() {
        Map<String, Map<String, Object>> input = new LinkedHashMap<>();
        input.put("field1", Map.of("strategy", "priority"));
        input.put("field2", Map.of("strategy", "minimum"));
        input.put("field3", Map.of("strategy", "maximum"));
        input.put("field4", Map.of("strategy", "average"));
        input.put("field5", Map.of("strategy", "sum"));
        input.put("field6", Map.of("strategy", "concatenate"));

        MergeDefinition result = MergeDefinitionConverter.fromMap(input);

        assertNotNull(result);
        assertEquals(6, result.getDefinitions().size());

        FieldDefinition field1 = (FieldDefinition) result.getDefinitions().get("field1");
        assertEquals("priority", field1.getStrategy());

        FieldDefinition field2 = (FieldDefinition) result.getDefinitions().get("field2");
        assertEquals("minimum", field2.getStrategy());

        FieldDefinition field3 = (FieldDefinition) result.getDefinitions().get("field3");
        assertEquals("maximum", field3.getStrategy());

        FieldDefinition field4 = (FieldDefinition) result.getDefinitions().get("field4");
        assertEquals("average", field4.getStrategy());

        FieldDefinition field5 = (FieldDefinition) result.getDefinitions().get("field5");
        assertEquals("sum", field5.getStrategy());

        FieldDefinition field6 = (FieldDefinition) result.getDefinitions().get("field6");
        assertEquals("concatenate", field6.getStrategy());
    }

    @Test
    @DisplayName("Should convert definition with default value")
    void testConvertWithDefaultValue() {
        Map<String, Map<String, Object>> input = new LinkedHashMap<>();
        input.put("count", Map.of("strategy", "sum", "defaultValue", 0));

        MergeDefinition result = MergeDefinitionConverter.fromMap(input);

        assertNotNull(result);
        FieldDefinition countField = (FieldDefinition) result.getDefinitions().get("count");
        assertNotNull(countField);
        assertEquals("sum", countField.getStrategy());
        assertEquals(0.0, countField.getDefaultValue()); // Gson converts to Double
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null input")
    void testConvertNullInput() {
        assertThrows(IllegalArgumentException.class, () -> MergeDefinitionConverter.fromMap(null));
    }

    @Test
    @DisplayName("Should parse GraalJsFieldDefinition")
    void parseGraalJsFieldDefinition() {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("strategy", "graaljs");
        fieldMap.put("expression", "sources.a + sources.b");

        Map<String, Map<String, Object>> input = Map.of("testField", fieldMap);
        MergeDefinition definition = MergeDefinitionConverter.fromMap(input);

        // Use reflection to verify class, effectively testing that dynamic loading
        // works
        FieldDefinition fieldDef = definition.getDefinitions().get("testField");
        try {
            Class<?> graalClass =
                    Class.forName("de.x132.objectmerger.strategy.graaljs.GraalJsFieldDefinition");
            assertInstanceOf(graalClass, fieldDef);

            java.lang.reflect.Method getExpression = graalClass.getMethod("getExpression");
            String expression = (String) getExpression.invoke(fieldDef);
            assertEquals("sources.a + sources.b", expression);
        } catch (Exception e) {
            fail("Failed to verify GraalJsFieldDefinition via reflection: " + e.getMessage());
        }
    }
}
