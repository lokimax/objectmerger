package de.x132.objectmerger.strategy.concatenate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.exception.InvalidSourceException;
import de.x132.objectmerger.strategy.MergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("ConcatenateStrategy Tests")
class ConcatenateStrategyTest {

    private ConcatenateStrategy strategy;
    private PriorityFieldDefinition<String> fieldDef;

    @BeforeEach
    void setUp() {
        strategy = new ConcatenateStrategy();
        fieldDef = mock(PriorityFieldDefinition.class);
    }

    @Test
    @DisplayName("Should concatenate strings with default delimiter")
    void testConcatenateWithDefaultDelimiter() {
        // Arrange
        String fieldName = "tags";
        Object source1 = new Object();
        Object source2 = new Object();
        Object source3 = new Object();

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2),
                        new LabeledSource<>("source3", source3));

        when(fieldDef.getPriority()).thenReturn(null);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn("java");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn("spring");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
                    .thenReturn("boot");

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("java,spring,boot", result);
        }
    }

    @Test
    @DisplayName("Should respect priority order for concatenation")
    void testConcatenateRespectsPriority() {
        // Arrange
        String fieldName = "description";
        Object source1 = new Object();
        Object source2 = new Object();
        Object source3 = new Object();

        Map<String, Integer> priority = new HashMap<>();
        priority.put("source3", 1);
        priority.put("source1", 2);
        priority.put("source2", 3);

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2),
                        new LabeledSource<>("source3", source3));

        when(fieldDef.getPriority()).thenReturn(priority);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn("first");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn("second");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
                    .thenReturn("third");

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("third,first,second", result); // Ordered by priority
        }
    }

    @Test
    @DisplayName("Should skip null values")
    void testConcatenateSkipsNullValues() {
        // Arrange
        String fieldName = "keywords";
        Object source1 = new Object();
        Object source2 = new Object();
        Object source3 = new Object();

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2),
                        new LabeledSource<>("source3", source3));

        when(fieldDef.getPriority()).thenReturn(null);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn("python");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn(null);
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
                    .thenReturn("javascript");

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("python,javascript", result); // source2 is skipped
        }
    }

    @Test
    @DisplayName("Should return default value when all sources are null")
    void testConcatenateReturnsDefaultWhenAllNull() {
        // Arrange
        String fieldName = "notes";
        Object source1 = new Object();
        Object source2 = new Object();

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2));

        String defaultValue = "No notes";
        when(fieldDef.getPriority()).thenReturn(null);
        when(fieldDef.getDefaultValue()).thenReturn(defaultValue);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn(null);
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn(null);

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("No notes", result);
        }
    }

    @Test
    @DisplayName("Should handle single value")
    void testConcatenateWithSingleValue() {
        // Arrange
        String fieldName = "category";
        Object source1 = new Object();

        List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

        when(fieldDef.getPriority()).thenReturn(null);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn("technology");

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("technology", result);
        }
    }

    @Test
    @DisplayName("Should throw exception for non-String values")
    void testConcatenateThrowsExceptionForNonString() {
        // Arrange
        String fieldName = "age";
        Object source1 = new Object();

        List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

        when(fieldDef.getPriority()).thenReturn(null);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn(25);

            // Act & Assert
            assertThrows(
                    InvalidSourceException.class,
                    () -> strategy.merge(sources, fieldDef, fieldName));
        }
    }

    @Test
    @DisplayName("Should implement MergeStrategy interface correctly")
    void testMergeStrategyInterface() {
        // Arrange & Act & Assert
        assertTrue(strategy instanceof MergeStrategy);
    }

    @Test
    @DisplayName("Should concatenate with priority and skip nulls")
    void testConcatenateWithPriorityAndNulls() {
        // Arrange
        String fieldName = "tags";
        Object source1 = new Object();
        Object source2 = new Object();
        Object source3 = new Object();

        Map<String, Integer> priority = new HashMap<>();
        priority.put("source1", 2);
        priority.put("source2", 1);
        priority.put("source3", 3);

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2),
                        new LabeledSource<>("source3", source3));

        when(fieldDef.getPriority()).thenReturn(priority);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn("java");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn(null);
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
                    .thenReturn("spring");

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("java,spring", result); // source2 skipped, ordered by priority
        }
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testConcatenateWithEmptyStrings() {
        // Arrange
        String fieldName = "parts";
        Object source1 = new Object();
        Object source2 = new Object();
        Object source3 = new Object();

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2),
                        new LabeledSource<>("source3", source3));

        when(fieldDef.getPriority()).thenReturn(null);
        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn("start");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn("");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
                    .thenReturn("end");

            // Act
            Object result = strategy.merge(sources, fieldDef, fieldName);

            // Assert
            assertEquals("start,,end", result); // Empty strings are kept
        }
    }
}
