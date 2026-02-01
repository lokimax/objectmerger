package de.x132.objectmerger.strategy.priority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@SuppressWarnings("rawtypes")
@DisplayName("PriorityMergeStrategy Tests")
class PriorityMergeStrategyTest {

  private PriorityMergeStrategy strategy;
  private PriorityFieldDefinition fieldDef;

  @BeforeEach
  void setUp() {
    strategy = new PriorityMergeStrategy();
    fieldDef = mock(PriorityFieldDefinition.class);
  }

  @Test
  @DisplayName("Should return value from source with lowest priority number")
  void testMergeWithPriority() {
    // Arrange
    String fieldName = "email";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    Map<String, Integer> priority = new HashMap<>();
    priority.put("source1", 3);
    priority.put("source2", 1);
    priority.put("source3", 2);

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
          .thenReturn("crm@example.com");
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn("db@example.com");
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
          .thenReturn("api@example.com");

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals("db@example.com", result); // source2 has priority 1 (lowest)
    }
  }

  @Test
  @DisplayName("Should skip sources with null values when priority is set")
  void testMergeWithPrioritySkipsNullValues() {
    // Arrange
    String fieldName = "phone";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    Map<String, Integer> priority = new HashMap<>();
    priority.put("source1", 1);
    priority.put("source2", 2);
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
          .thenReturn(null);
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn("123-456");
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
          .thenReturn("789-000");

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals("123-456", result); // source2 is used (source1 is null, source2 has priority)
    }
  }

  @Test
  @DisplayName("Should return default value when all sources have null values")
  void testMergeReturnsDefaultValueWhenAllNull() {
    // Arrange
    String fieldName = "age";
    Object source1 = new Object();
    Object source2 = new Object();

    Map<String, Integer> priority = new HashMap<>();
    priority.put("source1", 1);
    priority.put("source2", 2);

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", source1), new LabeledSource<>("source2", source2));

    Integer defaultValue = 0;
    when(fieldDef.getPriority()).thenReturn(priority);
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
  @DisplayName("Should handle unknown source labels with MAX_VALUE priority")
  void testMergeWithUnknownSourceLabel() {
    // Arrange
    String fieldName = "status";
    Object source1 = new Object();
    Object source2 = new Object();
    Object source3 = new Object();

    Map<String, Integer> priority = new HashMap<>();
    priority.put("source1", 1);
    // source2 and source3 are not in the priority map

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
          .thenReturn("active");
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
          .thenReturn("inactive");
      mockedObjectMerger
          .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
          .thenReturn("pending");

      // Act
      Object result = strategy.merge(sources, fieldDef, fieldName);

      // Assert
      assertEquals("active", result); // source1 has priority 1 (other sources get MAX_VALUE)
    }
  }

  @Test
  @DisplayName("Should implement MergeStrategy interface correctly")
  void testMergeStrategyInterface() {
    // Arrange & Act & Assert
    assertTrue(strategy instanceof MergeStrategy);
  }
}
