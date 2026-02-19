package de.x132.objectmerger.strategy.list;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ListMergeStrategy Tests")
class ListMergeStrategyTest {

    private ListMergeStrategy strategy;
    private ListFieldDefinition<List<Object>> fieldDef;
    private ItemMergeDefinition itemMergeDefinition;

    @BeforeEach
    void setUp() {
        strategy = new ListMergeStrategy();
        fieldDef = mock(ListFieldDefinition.class);
        itemMergeDefinition = mock(ItemMergeDefinition.class);
        when(fieldDef.getItemMergeDefinition()).thenReturn(itemMergeDefinition);
    }

    @Test
    @DisplayName("Should instantiate ListMergeStrategy without errors")
    void testStrategyInstantiation() {
        // Arrange & Act & Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof MergeStrategy);
    }

    @Test
    @DisplayName("Should handle empty sources list")
    void testMergeWithEmptySources() {
        // Arrange
        String fieldName = "items";
        List<LabeledSource<?>> sources = new ArrayList<>();

        // Act & Assert - should handle empty list gracefully
        assertDoesNotThrow(
                () -> {
                    Object result = strategy.merge(sources, fieldDef, fieldName);
                    assertNotNull(result);
                    assertTrue(result instanceof List);
                    assertTrue(((List<?>) result).isEmpty());
                });
    }

    @Test
    @DisplayName("Should implement MergeStrategy interface correctly")
    void testMergeStrategyInterface() {
        // Arrange & Act & Assert
        assertTrue(strategy instanceof MergeStrategy);
    }

    @Test
    @DisplayName("Should have correct merge method signature")
    void testMergeMethodSignature() {
        // Arrange
        String fieldName = "items";
        List<LabeledSource<?>> sources = new ArrayList<>();

        when(fieldDef.getIdentifyBy()).thenReturn("id");

        // Act & Assert - verify method can be called with correct signature
        assertDoesNotThrow(
                () -> {
                    Object result = strategy.merge(sources, fieldDef, fieldName);
                    assertNotNull(result);
                });
    }

    @Test
    @DisplayName("Should return List type from merge")
    void testMergeReturnsListType() {
        // Arrange
        String fieldName = "items";
        List<LabeledSource<?>> sources = new ArrayList<>();

        when(fieldDef.getIdentifyBy()).thenReturn("id");

        // Act
        Object result = strategy.merge(sources, fieldDef, fieldName);

        // Assert
        assertTrue(result instanceof List);
    }

    @Test
    @DisplayName("Should merge lists by ID")
    void testMergeListsById() {
        // Arrange
        // We need an ItemMergeDefinition to tell how to merge the items
        ItemMergeDefinition itemDef = new ItemMergeDefinition();
        itemDef.setTargetClass(TestItem.class.getName());

        // Let's add a definition for 'name' to use priority
        PriorityFieldDefinition<Object> nameDef =
                PriorityFieldDefinition.builder().strategy("priority").build();
        StandardFieldDefinition<Object> idDef = StandardFieldDefinition.builder().build();
        itemDef.setDefinitions(Map.of("name", nameDef, "id", idDef));

        ListFieldDefinition<List<Object>> fieldDef =
                ListFieldDefinition.<List<Object>>builder()
                        .identifyBy("id")
                        .itemMergeDefinition(itemDef)
                        .build();

        TestItem item1a = new TestItem("1", "A");
        TestItem item2a = new TestItem("2", "B"); // Will be merged
        List<TestItem> list1 = List.of(item1a, item2a);

        TestItem item2b = new TestItem("2", "B_Updated"); // Should override B if source is later
        TestItem item3b = new TestItem("3", "C");
        List<TestItem> list2 = List.of(item2b, item3b);

        List<LabeledSource<?>> sources = new ArrayList<>();
        // Source 1
        TestContainer container1 = new TestContainer(list1);
        sources.add(new LabeledSource<>("s1", container1));

        // Source 2 (higher priority implicitly by order if using standard priority?)
        // Actually PriorityStrategy needs explicit priority mapping usually,
        // or it picks first/last depending on implementation.
        // Standard PriorityStrategy uses "source_1": 1 config.
        // Let's just use "standard" strategy for name which usually means "last one
        // wins" or arbitrary?
        // Wait, StandardMergeStrategy is empty. PriorityStrategy needs config.
        // PriorityStrategy uses min() logic (1 is higher priority than 2)
        nameDef.setPriority(Map.of("s1", 2, "s2", 1));

        TestContainer container2 = new TestContainer(list2);
        sources.add(new LabeledSource<>("s2", container2));

        // Act
        @SuppressWarnings("unchecked")
        List<TestItem> result =
                (List<TestItem>) (List) strategy.merge(sources, fieldDef, "members");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Check ID 1 (only in s1)
        TestItem res1 =
                result.stream().filter(i -> i.getId().equals("1")).findFirst().orElseThrow();
        assertEquals("A", res1.getName());

        // Check ID 2 (in s1 and s2) -> s2 has higher priority (2 > 1)
        TestItem res2 =
                result.stream().filter(i -> i.getId().equals("2")).findFirst().orElseThrow();
        assertEquals("B_Updated", res2.getName());

        // Check ID 3 (only in s2)
        TestItem res3 =
                result.stream().filter(i -> i.getId().equals("3")).findFirst().orElseThrow();
        assertEquals("C", res3.getName());
    }

    /** Test helper classes */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestItem {
        private String id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class TestContainer {
        private List<?> members;
    }
}
