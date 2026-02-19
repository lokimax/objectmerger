package de.x132.objectmerger.strategy.graaljs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.x132.objectmerger.LabeledSource;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GraalJsMergeStrategyTest {

    private final GraalJsMergeStrategy strategy = new GraalJsMergeStrategy();

    @Test
    @DisplayName("Should execute simple arithmetic expression")
    void arithmeticExpression() {
        GraalJsFieldDefinition fieldDef =
                GraalJsFieldDefinition.builder().expression("sources.a + sources.b").build();

        List<LabeledSource<?>> sources =
                Arrays.asList(new LabeledSource<>("a", 10), new LabeledSource<>("b", 20));

        Object result = strategy.merge(sources, fieldDef, "testField");
        // JS numbers might be doubles or integers depending on engine, GraalJS returns generic
        // Number or int
        // Assert equals handles primitive types if correct
        assertEquals(30, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should execute conditional logic")
    void conditionalLogic() {
        GraalJsFieldDefinition fieldDef =
                GraalJsFieldDefinition.builder()
                        .expression(
                                "if (sources.prio > 100) { sources.prio } else { sources.backup }")
                        .build();

        // Case 1: Prio > 100
        List<LabeledSource<?>> sourcesHigh =
                Arrays.asList(new LabeledSource<>("prio", 150), new LabeledSource<>("backup", 10));
        assertEquals(150, ((Number) strategy.merge(sourcesHigh, fieldDef, "testField")).intValue());

        // Case 2: Prio <= 100
        List<LabeledSource<?>> sourcesLow =
                Arrays.asList(new LabeledSource<>("prio", 50), new LabeledSource<>("backup", 10));
        assertEquals(10, ((Number) strategy.merge(sourcesLow, fieldDef, "testField")).intValue());
    }

    @Test
    @DisplayName("Should return default value for null/empty expression")
    void emptyExpression() {
        GraalJsFieldDefinition fieldDef = GraalJsFieldDefinition.builder().build();
        assertNull(strategy.merge(List.of(), fieldDef, "testField"));

        fieldDef.setExpression("");
        assertNull(strategy.merge(List.of(), fieldDef, "testField"));
    }

    @Test
    @DisplayName("Should handle missing sources gracefully (JS behavior)")
    void missingSources() {
        GraalJsFieldDefinition fieldDef =
                GraalJsFieldDefinition.builder()
                        .expression("sources.missing == null ? 'not found' : 'found'")
                        .build();

        List<LabeledSource<?>> sources = List.of(new LabeledSource<>("existing", 1));
        assertEquals("not found", strategy.merge(sources, fieldDef, "testField"));
    }

    @Test
    @DisplayName("Should execute conditional strategy logic")
    void conditionalStrategy() {
        GraalJsConditionalStrategy<Object> conditionalStrategy = new GraalJsConditionalStrategy<>();

        // Use Map sources to allow field extraction
        List<LabeledSource<?>> mapSources =
                Arrays.asList(
                        new LabeledSource<>("s1", java.util.Map.of("amount", 150)),
                        new LabeledSource<>("s2", java.util.Map.of("amount", 50)));

        de.x132.objectmerger.strategy.conditional.ConditionCase<Object> case1 =
                new de.x132.objectmerger.strategy.conditional.ConditionCase<>(
                        "values.s1 > 100",
                        de.x132.objectmerger.strategy.standard.StandardFieldDefinition.builder()
                                .build());

        de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition<Object> def =
                de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition.builder()
                        .cases(java.util.List.of(case1))
                        .defaultStrategy(
                                de.x132.objectmerger.strategy.standard.StandardFieldDefinition
                                        .builder()
                                        .build())
                        .build();

        // Condition s1(150) > 100 is true
        // Standard merge on "amount" returns 150 (from s1)
        Object result = conditionalStrategy.merge(mapSources, def, "amount");
        assertEquals(150, result);

        // Test false condition
        de.x132.objectmerger.strategy.conditional.ConditionCase<Object> case2 =
                new de.x132.objectmerger.strategy.conditional.ConditionCase<>(
                        "values.s1 > 200",
                        de.x132.objectmerger.strategy.standard.StandardFieldDefinition.builder()
                                .build());

        de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition<Object> def2 =
                de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition.builder()
                        .cases(java.util.List.of(case2))
                        .defaultStrategy(
                                de.x132.objectmerger.strategy.standard.StandardFieldDefinition
                                        .builder()
                                        .build())
                        .build();

        // Condition s1 > 200 is false -> default strategy -> 150
        assertEquals(150, conditionalStrategy.merge(mapSources, def2, "amount"));
    }
}
