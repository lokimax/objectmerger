package de.x132.objectmerger.strategy.conditional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Conditional Merge Strategy Tests")
class ConditionalMergeTest {

  private ConditionalMergeStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new ConditionalMergeStrategy();
  }

  @Test
  @DisplayName("Should use case strategy when condition matches")
  void shouldUseCaseStrategyWhenConditionMatches() {
    // Arrange

    // Case 1: If 'master' is present, use Priority
    ConditionCase<Object> case1 = new ConditionCase<>();
    case1.setCondition("values.containsKey('master')");

    PriorityFieldDefinition<Object> priorityDef =
        PriorityFieldDefinition.<Object>builder()
            .strategy("priority")
            .priority(Map.of("master", 1))
            .build();
    case1.setUseStrategy(priorityDef);

    ConditionalFieldDefinition<Object> fieldDef =
        ConditionalFieldDefinition.<Object>builder()
            .cases(List.of(case1))
            .defaultStrategy(StandardFieldDefinition.<Object>builder().strategy("standard").build())
            .build();

    // Sources
    LabeledSource<Map<String, Object>> src1 = new LabeledSource<>("master", Map.of("field", "A"));
    LabeledSource<Map<String, Object>> src2 = new LabeledSource<>("other", Map.of("field", "B"));
    List<LabeledSource<?>> sources = List.of(src1, src2);

    // Act
    Object result = strategy.merge(sources, fieldDef, "field");

    // Assert
    // Priority logic: master=1 -> should pick "A"
    assertEquals("A", result);
  }

  @Test
  @DisplayName("Should use default strategy when no condition matches")
  void shouldUseDefaultStrategyWhenNoConditionMatches() {
    // Arrange
    // Case 1: If 'special' source present (it's not)
    ConditionCase<Object> case1 = new ConditionCase<>();
    case1.setCondition("values.containsKey('special')");

    ConditionalFieldDefinition<Object> fieldDef =
        ConditionalFieldDefinition.<Object>builder()
            .cases(List.of(case1))
            .defaultStrategy(StandardFieldDefinition.<Object>builder().strategy("standard").build())
            .build();

    // Sources
    LabeledSource<Map<String, Object>> src1 = new LabeledSource<>("master", Map.of("field", "A"));
    List<LabeledSource<?>> sources = List.of(src1);

    // Act
    Object result = strategy.merge(sources, fieldDef, "field");

    // Assert
    // Standard Strategy logic
    assertEquals("A", result);
  }

  @Test
  @DisplayName("Should return null if no condition matches and no default strategy")
  void shouldReturnNullIfNoMatchAndNoDefault() {
    ConditionalFieldDefinition<Object> fieldDef =
        ConditionalFieldDefinition.<Object>builder().cases(List.of()).defaultStrategy(null).build();

    List<LabeledSource<?>> sources = List.of(new LabeledSource<>("s1", Map.of("f", "v")));

    Object result = strategy.merge(sources, fieldDef, "f");

    assertNull(result);
  }
}
