package de.x132.objectmerger.strategy.mvel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.x132.objectmerger.LabeledSource;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MvelMergeStrategyTest {

    private final MvelMergeStrategy strategy = new MvelMergeStrategy();

    @Test
    @DisplayName("Should execute simple arithmetic expression")
    void arithmeticExpression() {
        MvelFieldDefinition fieldDef =
                MvelFieldDefinition.builder().expression("sources['a'] + sources['b']").build();

        List<LabeledSource<?>> sources =
                Arrays.asList(new LabeledSource<>("a", 10), new LabeledSource<>("b", 20));

        Object result = strategy.merge(sources, fieldDef, "testField");
        assertEquals(30, result);
    }

    @Test
    @DisplayName("Should execute conditional logic")
    void conditionalLogic() {
        MvelFieldDefinition fieldDef =
                MvelFieldDefinition.builder()
                        .expression(
                                "if (sources['prio'] > 100) { return sources['prio']; } else { return sources['backup']; }")
                        .build();

        // Case 1: Prio > 100
        List<LabeledSource<?>> sourcesHigh =
                Arrays.asList(new LabeledSource<>("prio", 150), new LabeledSource<>("backup", 10));
        assertEquals(150, strategy.merge(sourcesHigh, fieldDef, "testField"));

        // Case 2: Prio <= 100
        List<LabeledSource<?>> sourcesLow =
                Arrays.asList(new LabeledSource<>("prio", 50), new LabeledSource<>("backup", 10));
        assertEquals(10, strategy.merge(sourcesLow, fieldDef, "testField"));
    }

    @Test
    @DisplayName("Should return null for null/empty expression")
    void emptyExpression() {
        MvelFieldDefinition fieldDef = MvelFieldDefinition.builder().build();
        assertNull(strategy.merge(List.of(), fieldDef, "testField"));

        fieldDef.setExpression("");
        assertNull(strategy.merge(List.of(), fieldDef, "testField"));
    }

    @Test
    @DisplayName("Should handle missing sources gracefully (MVEL behavior)")
    void missingSources() {
        MvelFieldDefinition fieldDef =
                MvelFieldDefinition.builder()
                        .expression("sources['missing'] == null ? 'not found' : 'found'")
                        .build();

        List<LabeledSource<?>> sources = List.of(new LabeledSource<>("existing", 1));
        assertEquals("not found", strategy.merge(sources, fieldDef, "testField"));
    }
}
