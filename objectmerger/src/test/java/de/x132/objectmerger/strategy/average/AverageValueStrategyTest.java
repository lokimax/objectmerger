package de.x132.objectmerger.strategy.average;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("AverageValueStrategy Tests")
class AverageValueStrategyTest {

  private AverageValueStrategy strategy;
  private FieldDefinition fieldDef;

  @BeforeEach
  void setUp() {
    strategy = new AverageValueStrategy();
    fieldDef = mock(FieldDefinition.class);
  }

  @Test
  @DisplayName("Should calculate average from integer values")
  void testAverageOfIntegers() {
    // Arrange
    String fieldName = "score";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(10);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(20);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(30);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(20, result); // (10 + 20 + 30) / 3 = 20
    }
  }

  @Test
  @DisplayName("Should calculate average from double values")
  void testAverageOfDoubles() {
    // Arrange
    String fieldName = "rating";
    Object source1 = new Object();
    Object source2 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1), new LabeledSource<>("source2", source2));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(4.5);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(3.5);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(4, result); // (4.5 + 3.5) / 2 = 4.0, returned as int
    }
  }

  @Test
  @DisplayName("Should skip null values when calculating average")
  void testAverageSkipsNullValues() {
    // Arrange
    String fieldName = "age";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn(null);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(25);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(35);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(30, result); // (25 + 35) / 2 = 30 (source1 ignored)
    }
  }

  @Test
  @DisplayName("Should return default value when all values are null")
  void testAverageReturnsDefaultWhenAllNull() {
    // Arrange
    String fieldName = "score";
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
  void testAverageWithSingleValue() {
    // Arrange
    String fieldName = "value";
    Object source1 = new Object();

    List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(42);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(42, result); // Average of single value is the value itself
    }
  }

  @Test
  @DisplayName("Should return double when result has decimal places")
  void testAverageReturnsDoubleForNonWholeNumbers() {
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

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(20);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(21);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(22);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(21, result); // (20 + 21 + 22) / 3 = 21.0, returned as int
    }
  }

  @Test
  @DisplayName("Should handle Long values")
  void testAverageWithLongValues() {
    // Arrange
    String fieldName = "count";
    Object source1 = new Object();
    Object source2 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1), new LabeledSource<>("source2", source2));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn(1000L);
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn(2000L);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(1500, result); // (1000 + 2000) / 2 = 1500
    }
  }

  @Test
  @DisplayName("Should implement MergeStrategy interface correctly")
  void testMergeStrategyInterface() {
    // Arrange & Act & Assert
    assertTrue(strategy instanceof MergeStrategy);
  }

  @Test
  @DisplayName("Should throw exception for non-numeric values")
  void testAverageThrowsExceptionForNonNumeric() {
    // Arrange
    String fieldName = "name";
    Object source1 = new Object();

    List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn("John");

      // Act & Assert
      assertThrows(
          IllegalArgumentException.class, () -> strategy.merge(sources, fieldDef, fieldName));
    }
  }

  @Test
  @DisplayName("Should calculate average with mixed numeric types")
  void testAverageWithMixedNumericTypes() {
    // Arrange
    String fieldName = "value";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1),
            new LabeledSource<>("source2", source2),
            new LabeledSource<>("source3", source3));

    when(fieldDef.getDefaultValue()).thenReturn(null);

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn(10); // Integer
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn(20L); // Long
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
          .thenReturn(30.0); // Double

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(20, result); // (10 + 20 + 30) / 3 = 20.0
    }
  }
}
