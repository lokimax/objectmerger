package de.x132.objectmerger.strategy.mvel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.x132.objectmerger.LabeledSource;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MvelFallbackTest {

    private final MvelMergeStrategy strategy = new MvelMergeStrategy();

    @Test
    @DisplayName("Should return default value when expression is empty")
    void emptyExpressionReturnsDefault() {
        MvelFieldDefinition fieldDef = new MvelFieldDefinition();
        fieldDef.setDefaultValue("fallback");

        // Test null expression (default)
        assertEquals("fallback", strategy.merge(List.of(), fieldDef, "test"));

        // Test empty expression
        fieldDef.setExpression("");
        assertEquals("fallback", strategy.merge(List.of(), fieldDef, "test"));
    }

    @Test
    @DisplayName("Should return deafult value when expression fails")
    void exceptionReturnsDefault() {
        MvelFieldDefinition fieldDef = new MvelFieldDefinition();
        fieldDef.setExpression("undefinedVar.callMethod()"); // This will throw PropertyAccessException or similar
        fieldDef.setDefaultValue("fallback_on_error");

        assertEquals("fallback_on_error", strategy.merge(List.of(), fieldDef, "test"));
    }

    @Test
    @DisplayName("Should return null if no default value is set and error occurs")
    void noDefaultValueReturnsNullOnError() {
        MvelFieldDefinition fieldDef = new MvelFieldDefinition();
        fieldDef.setExpression("some_undefined_variable");
        // No defaultValue set (null by default)

        assertNull(strategy.merge(List.of(), fieldDef, "test"));
    }
}
