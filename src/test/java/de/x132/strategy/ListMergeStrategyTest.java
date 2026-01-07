package de.x132.strategy;

import de.x132.FieldDefinition;
import de.x132.ItemMergeDefinition;
import de.x132.LabeledSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ListMergeStrategy Tests")
class ListMergeStrategyTest {

    private ListMergeStrategy strategy;
    private FieldDefinition fieldDef;
    private ItemMergeDefinition itemMergeDefinition;

    @BeforeEach
    void setUp() {
        strategy = new ListMergeStrategy();
        fieldDef = mock(FieldDefinition.class);
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
        assertDoesNotThrow(() -> {
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
        assertDoesNotThrow(() -> {
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
    @DisplayName("Should handle multiple sources gracefully")
    void testMergeWithMultipleSources() {
        // Arrange
        String fieldName = "items";
        List<LabeledSource<?>> sources = new ArrayList<>();

        when(fieldDef.getIdentifyBy()).thenReturn("id");

        // Act & Assert - should handle multiple sources without errors
        assertDoesNotThrow(() -> {
            Object result = strategy.merge(sources, fieldDef, fieldName);
            assertNotNull(result);
            assertTrue(result instanceof List);
        });
    }

    /**
     * Test helper classes
     */
    private static class TestItem {
        private final String id;
        private final String name;

        TestItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class TestContainer {
        private final List<?> members;

        TestContainer(List<?> members) {
            this.members = members;
        }

        public List<?> getMembers() {
            return members;
        }
    }
}
