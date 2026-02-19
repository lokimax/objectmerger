package de.x132.objectmerger.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.list.ListFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapCompatibilityTest {

    @Test
    @DisplayName("Sales: Should sum values correctly using Map interface")
    void testSalesMerge() {
        // Definition
        MergeDefinition def = new MergeDefinition();
        Map<String, FieldDefinition<?>> fields = new HashMap<>();

        StandardFieldDefinition<Object> sumDef =
                StandardFieldDefinition.builder().strategy("sum").build();
        fields.put("totalSales", sumDef);

        def.setDefinitions(fields);

        // Data (SalesData equivalent)
        Map<String, Object> amazon = Map.of("totalSales", 1500);
        Map<String, Object> shopify = Map.of("totalSales", 2300);
        Map<String, Object> ebay = Map.of("totalSales", 800);
        Map<String, Object> woocommerce = Map.of("totalSales", 1200);

        // Merge
        Map<String, Object> result =
                ObjectMerger.merge(
                        def,
                        new LabeledSource<>("amazon", amazon),
                        new LabeledSource<>("shopify", shopify),
                        new LabeledSource<>("ebay", ebay),
                        new LabeledSource<>("woocommerce", woocommerce));

        // Verify (Expected: 5800)
        assertEquals(5800, ((Number) result.get("totalSales")).intValue());
    }

    @Test
    @DisplayName("Person: Should handle Priority and Max strategies using Map interface")
    void testPersonMerge() {
        // Definition
        MergeDefinition def = new MergeDefinition();
        Map<String, FieldDefinition<?>> fields = new HashMap<>();

        PriorityFieldDefinition<Object> nameDef =
                PriorityFieldDefinition.builder()
                        .priority(Map.of("db", 1, "crm", 2)) // db wins
                        .build();
        fields.put("name", nameDef);

        StandardFieldDefinition<Object> ageDef =
                StandardFieldDefinition.builder().strategy("maximum").build();
        fields.put("age", ageDef);

        def.setDefinitions(fields);

        // Data
        Map<String, Object> db = new HashMap<>();
        db.put("name", "Max DB");
        db.put("age", 30);

        Map<String, Object> crm = new HashMap<>();
        crm.put("name", "Max CRM");
        crm.put("age", 40);

        // Merge
        Map<String, Object> result =
                ObjectMerger.merge(
                        def, new LabeledSource<>("db", db), new LabeledSource<>("crm", crm));

        // Verify
        assertEquals("Max DB", result.get("name"));
        assertEquals(40, result.get("age"));
    }

    @Test
    @DisplayName("Family: Should handle List merging using Map interface")
    @SuppressWarnings("unchecked")
    void testFamilyMerge() {
        // Definition
        MergeDefinition def = new MergeDefinition();
        Map<String, FieldDefinition<?>> fields = new HashMap<>();

        // "members" is a List of objects, merged by "name"
        ListFieldDefinition<Object> membersDef =
                ListFieldDefinition.builder().strategy("mergeList").identifyBy("name").build();

        // ...

        // Define how to merge list items (Map based)
        ItemMergeDefinition itemDef = new ItemMergeDefinition();
        Map<String, FieldDefinition<?>> childFields = new HashMap<>();

        // Child name
        PriorityFieldDefinition<Object> childNameDef =
                PriorityFieldDefinition.builder().priority(Map.of("s1", 1, "s2", 2)).build();
        childFields.put("name", childNameDef);

        // Child age
        StandardFieldDefinition<Object> childAgeDef =
                StandardFieldDefinition.builder().strategy("maximum").build();
        childFields.put("age", childAgeDef);

        itemDef.setDefinitions(childFields);
        membersDef.setItemMergeDefinition(itemDef);

        fields.put("members", membersDef);

        def.setDefinitions(fields);

        // Data
        Map<String, Object> member1 = Map.of("name", "Child1", "age", 10);
        Map<String, Object> member2 = Map.of("name", "Child2", "age", 12);
        Map<String, Object> member1Update = Map.of("name", "Child1", "age", 11); // Update to Child1

        Map<String, Object> source1 = new HashMap<>();
        source1.put("members", Arrays.asList(member1, member2));

        Map<String, Object> source2 = new HashMap<>();
        source2.put("members", Arrays.asList(member1Update));

        // Merge
        Map<String, Object> result =
                ObjectMerger.merge(
                        def,
                        new LabeledSource<>("s1", source1),
                        new LabeledSource<>("s2", source2));

        // Verify
        List<Object> mergedMembers = (List<Object>) result.get("members");
        assertNotNull(mergedMembers);
        assertEquals(2, mergedMembers.size()); // Child1 (merged), Child2

        // Simple check: Child1 should typically take latest (or first depending on list
        // strategy details,
        // but default mergeList merges properties.
        // Wait, mergeList on Maps? It relies on ObjectMerger.merge recursively!
        // Does ObjectMerger.merge work recursively for Maps if the type isn't known?
        // "ListMergeStrategy" uses "resolveTargetType".
        // If target type is "Map", it might fail if it expects a class.
        // Let's verify if ListMergeStrategy supports dynamic Map merging.
        // If NOT, this test will fail and reveal a gap in "recursion".
    }
}
