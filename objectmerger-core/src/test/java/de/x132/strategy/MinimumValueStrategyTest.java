package de.x132.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.x132.FieldDefinition;
import de.x132.LabeledSource;
import de.x132.ObjectMerger;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("MinimumValueStrategy Tests")
class MinimumValueStrategyTest {

  private MinimumValueStrategy strategy;
  private FieldDefinition fieldDef;

  @BeforeEach
  void setUp() {
    strategy = new MinimumValueStrategy();
    fieldDef = mock(FieldDefinition.class);
  }

  @Test
  @DisplayName("Should return the minimum value from multiple sources")
  void testMergeReturnsMinimumValue() {
    // Arrange
    String fieldName = "price";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      // Configure mock to return different price values
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(100);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(50);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(75);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(50, result); // source2 has minimum value
      mockedObjectMerger.verify(() -> ObjectMerger.getFieldValue(source1, fieldName));
      mockedObjectMerger.verify(() -> ObjectMerger.getFieldValue(source2, fieldName));
      mockedObjectMerger.verify(() -> ObjectMerger.getFieldValue(source3, fieldName));
    }
  }

  @Test
  @DisplayName("Should skip null values when finding minimum")
  void testMergeSkipsNullValues() {
    // Arrange
    String fieldName = "rating";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn(null);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(3);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(5);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(3, result); // source1 is null, minimum of [3, 5] is 3
    }
  }

  @Test
  @DisplayName("Should return default value when all sources have null values")
  void testMergeReturnsDefaultValueWhenAllNull() {
    // Arrange
    String fieldName = "discount";
    Object source1 = new Object();
    Object source2 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1), new LabeledSource<>("source2", source2));

    Integer defaultValue = 0;
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
      assertEquals(0, result);
    }
  }

  @Test
  @DisplayName("Should handle single value")
  void testMergeWithSingleValue() {
    // Arrange
    String fieldName = "value";
    Object source1 = new Object();

    List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(42);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(42, result); // Minimum of single value is the value itself
    }
  }

  @Test
  @DisplayName("Should handle negative numbers")
  void testMergeWithNegativeNumbers() {
    // Arrange
    String fieldName = "temperature";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(-5);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(10);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(0);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(-5, result); // Minimum of [-5, 10, 0] is -5
    }
  }

  @Test
  @DisplayName("Should implement MergeStrategy interface correctly")
  void testMergeStrategyInterface() {
    // Arrange & Act & Assert
    assertTrue(strategy instanceof MergeStrategy);
  }

  @Test
  @DisplayName("Should return minimum from mixed positive and negative")
  void testMergeWithMixedValues() {
    // Arrange
    String fieldName = "balance";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn(1000);
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn(-500);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(200);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(-500, result); // Minimum of [1000, -500, 200] is -500
    }
  }

  @Test
  @DisplayName("Should return zero as minimum when comparing [0, 100, 50]")
  void testMergeWithZeroAsMinimum() {
    // Arrange
    String fieldName = "count";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(0);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(100);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(50);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(0, result); // Minimum of [0, 100, 50] is 0
    }
  }
}
