package de.x132.objectmerger.strategy.maximum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("MaximumValueStrategy Tests")
class MaximumValueStrategyTest {

  private MaximumValueStrategy strategy;
  private FieldDefinition fieldDef;

  @BeforeEach
  void setUp() {
    strategy = new MaximumValueStrategy();
    fieldDef = mock(FieldDefinition.class);
  }

  @Test
  @DisplayName("Should return the maximum value from multiple sources")
  void testMergeReturnsMaximumValue() {
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

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      // Configure mock to return different age values
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(25);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(35);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(30);

      when(fieldDef.getDefaultValue()).thenReturn(0);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(35, result);
      mockedObjectMerger.verify(() -> ObjectMerger.getFieldValue(any(), eq(fieldName)), times(3));
    }
  }

  @Test
  @DisplayName("Should return default value when all values are null")
  void testMergeReturnsDefaultValueWhenAllNull() {
    // Arrange
    String fieldName = "age";
    Object defaultValue = 18;
    Object source1 = new Object();
    Object source2 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1), new LabeledSource<>("source2", source2));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      // Configure mock to return null values
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(any(), eq(fieldName)))
          .thenReturn(null);

      when(fieldDef.getDefaultValue()).thenReturn(defaultValue);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(defaultValue, result);
    }
  }

  @Test
  @DisplayName("Should filter null values and return maximum of non-null values")
  void testMergeFiltersNullValues() {
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

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      // Configure mock: null, 40, null
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
          .thenReturn(null);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source2, fieldName)).thenReturn(40);
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
          .thenReturn(null);

      when(fieldDef.getDefaultValue()).thenReturn(0);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(40, result);
    }
  }

  @Test
  @DisplayName("Should handle single source")
  void testMergeWithSingleSource() {
    // Arrange
    String fieldName = "score";
    Object source1 = new Object();
    List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(100);
      when(fieldDef.getDefaultValue()).thenReturn(0);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(100, result);
    }
  }

  @Test
  @DisplayName("Should return default value with empty sources")
  void testMergeWithEmptySources() {
    // Arrange
    String fieldName = "age";
    List<LabeledSource<?>> sources = Arrays.asList();
    Object defaultValue = 21;

    when(fieldDef.getDefaultValue()).thenReturn(defaultValue);

    // Act
    Object result = strategy.merge(sources, fieldDef, fieldName);

    // Assert
    assertEquals(defaultValue, result);
  }

  @Test
  @DisplayName("Should handle mixed null and non-null values")
  void testMergeMixedValues() {
    // Arrange
    String fieldName = "value";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();
    Object source4 = new Object();

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("s1", source1),
            new LabeledSource<>("s2", source2),
            new LabeledSource<>("s3", source3),
            new LabeledSource<>("s4", source4));

    try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
      // Configure: 10, null, 50, 30
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source1, fieldName)).thenReturn(10);
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn(null);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source3, fieldName)).thenReturn(50);
      mockedObjectMerger.when(() -> ObjectMerger.getFieldValue(source4, fieldName)).thenReturn(30);

      when(fieldDef.getDefaultValue()).thenReturn(0);

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals(50, result);
    }
  }
}
