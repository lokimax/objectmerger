package de.x132.doc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionCase;
import de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionalMergeStrategy;
import de.x132.objectmerger.strategy.mvel.MvelFieldDefinition;
import de.x132.objectmerger.strategy.mvel.MvelMergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityMergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardMergeStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class MvelDocumentationVerificationTest {

    @Test
    void verifyConditionalStrategy() {
        Map<String, Object> source1 = new HashMap<>();
        source1.put("age", 18);
        source1.put("category", "adult");

        Map<String, Object> source2 = new HashMap<>();
        source2.put("age", 18);
        source2.put("category", "minor");

        ConditionCase<String> adultCase = new ConditionCase<>();
        adultCase.setCondition("values['json1'] == 'adult'");

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
                        .strategy(ConditionalMergeStrategy.NAME)
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

    @Test
    void verifyMvelStrategy() {
        Map<String, Object> source1 = new HashMap<>();
        source1.put("price", 100);
        source1.put("discount", 0.1);

        Map<String, Object> source2 = new HashMap<>();

        MvelFieldDefinition finalPriceDef =
                MvelFieldDefinition.builder()
                        .strategy(MvelMergeStrategy.NAME)
                        .expression(
                                "sources['json1']['price'] * (1.0 - sources['json1']['discount'])")
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
        assertEquals(90.0, ((Number) val).doubleValue(), 0.01);
    }
}
