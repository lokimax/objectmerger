package de.x132.doc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionCase;
import de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition;
import de.x132.objectmerger.strategy.graaljs.GraalJsConditionalStrategy;
import de.x132.objectmerger.strategy.graaljs.GraalJsFieldDefinition;
import de.x132.objectmerger.strategy.graaljs.GraalJsMergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityMergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardMergeStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GraalJS Documentation Verification")
public class GraalJsDocumentationVerificationTest {

    @Test
    @DisplayName("Verify GraalJS Strategy Example from Doc")
    void verifyGraalJsStrategy() {
        Map<String, Object> source1 = new HashMap<>();
        source1.put("price", 100);
        source1.put("discount", 0.1);

        Map<String, Object> source2 = new HashMap<>(); // Empty

        GraalJsFieldDefinition finalPriceDef =
                GraalJsFieldDefinition.builder()
                        .strategy(GraalJsMergeStrategy.NAME)
                        .expression(
                                "sources.get('json1').get('price') * (1.0 - sources.get('json1').get('discount'))")
                        .defaultValue(0.0)
                        .build();

        Map<String, FieldDefinition<?>> definitions = new HashMap<>();
        definitions.put("finalPrice", finalPriceDef);

        MergeDefinition mergeDefinition = new MergeDefinition(definitions);

        Map<String, Object> result =
                ObjectMerger.merge(
                        mergeDefinition,
                        new LabeledSource<>("json1", source1),
                        new LabeledSource<>("json2", source2));

        Object val = result.get("finalPrice");
        // GraalJS returns generic Number, often Double or Integer.
        // Assert as double with delta.
        assertEquals(90.0, ((Number) val).doubleValue(), 0.01);
    }

    @Test
    @DisplayName("Verify Conditional Strategy (GraalJS) Example from Doc")
    void verifyConditionalStrategy() {
        // Documented Example: "values.get('json1') == 'adult'"

        Map<String, Object> source1 = new HashMap<>();
        source1.put("age", 18);
        source1.put("category", "adult");

        Map<String, Object> source2 = new HashMap<>();
        source2.put("age", 18);
        source2.put("category", "minor");

        ConditionCase<String> adultCase = new ConditionCase<>();
        // JS condition syntax
        adultCase.setCondition("values.get('json1') == 'adult'");

        Map<String, Integer> p = new HashMap<>();
        p.put("json1", 1);
        p.put("json2", 2);

        PriorityFieldDefinition<String> priorityDef =
                PriorityFieldDefinition.<String>builder()
                        .strategy(PriorityMergeStrategy.NAME)
                        .priority(p)
                        .build();

        adultCase.setUseStrategy(priorityDef);

        List<ConditionCase<String>> cases = new ArrayList<>();
        cases.add(adultCase);

        ConditionalFieldDefinition<String> categoryDef =
                ConditionalFieldDefinition.<String>builder()
                        .strategy(GraalJsConditionalStrategy.NAME) // "conditional"
                        .defaultValue("unknown")
                        .cases(cases)
                        .build();

        StandardFieldDefinition<String> defaultDef =
                StandardFieldDefinition.<String>builder()
                        .strategy(StandardMergeStrategy.NAME)
                        .defaultValue("fallback")
                        .build();
        categoryDef.setDefaultStrategy(defaultDef);

        Map<String, FieldDefinition<?>> definitions = new HashMap<>();
        definitions.put("category", categoryDef);

        MergeDefinition mergeDefinition = new MergeDefinition(definitions);

        Map<String, Object> result =
                ObjectMerger.merge(
                        mergeDefinition,
                        new LabeledSource<>("json1", source1),
                        new LabeledSource<>("json2", source2));

        assertEquals("adult", result.get("category"));
    }
}
