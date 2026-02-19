package de.x132.objectmerger.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapBasedMergeTest {

    @Test
    void testMapMerge() {
        // 1. Define Strategy
        MergeDefinition definition = new MergeDefinition();
        Map<String, FieldDefinition<?>> fields = new HashMap<>();

        // "name" uses Priority (source1 > source2)
        // "name" uses Priority (source1 > source2)
        PriorityFieldDefinition<Object> nameDef =
                PriorityFieldDefinition.builder()
                        .priority(Map.of("source1", 1, "source2", 2))
                        .build();
        fields.put("name", nameDef);

        // "age" uses Maximum
        StandardFieldDefinition<Object> ageDef =
                StandardFieldDefinition.builder().strategy("maximum").build();
        fields.put("age", ageDef);

        definition.setDefinitions(fields);

        // 2. Prepare Data
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "Max");
        data1.put("age", 30);
        LabeledSource<Map<String, Object>> source1 = new LabeledSource<>("source1", data1);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Moritz");
        data2.put("age", 40);
        LabeledSource<Map<String, Object>> source2 = new LabeledSource<>("source2", data2);

        // 3. Merge
        Map<String, Object> result = ObjectMerger.merge(definition, source1, source2);

        // 4. Verify
        assertEquals("Max", result.get("name")); // Source1 has priority 1 (higher than Source2's 2)
        assertEquals(40, result.get("age")); // Maximum of 30 and 40
    }
}
