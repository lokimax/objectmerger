package de.x132.objectmerger.strategy.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.FieldDefinition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateMergeStrategiesTest {

  private final LatestDateStrategy latestStrategy = new LatestDateStrategy();
  private final EarliestDateStrategy earliestStrategy = new EarliestDateStrategy();
  private final FieldDefinition fieldDef = new FieldDefinition() {}; // Anonymous concrete subclass

  @Test
  @DisplayName("LatestDateStrategy picks the latest java.util.Date")
  void latestUtilDate() {
    Date d1 = new Date(100000L);
    Date d2 = new Date(200000L);
    Date d3 = new Date(150000L);

    List<LabeledSource<?>> sources = wrap(d1, d2, d3);
    Object result = latestStrategy.merge(sources, fieldDef, "value");

    assertEquals(d2, result);
  }

  @Test
  @DisplayName("LatestDateStrategy picks the latest java.time.LocalDate")
  void latestLocalDate() {
    LocalDate d1 = LocalDate.of(2020, 1, 1);
    LocalDate d2 = LocalDate.of(2025, 1, 1); // Latest
    LocalDate d3 = LocalDate.of(2022, 1, 1);

    List<LabeledSource<?>> sources = wrap(d1, d2, d3);
    Object result = latestStrategy.merge(sources, fieldDef, "value");

    assertEquals(d2, result);
  }

  @Test
  @DisplayName("EarliestDateStrategy picks the earliest java.time.LocalDateTime")
  void earliestLocalDateTime() {
    LocalDateTime d1 = LocalDateTime.of(2020, 1, 1, 12, 0); // Earliest
    LocalDateTime d2 = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime d3 = LocalDateTime.of(2022, 1, 1, 12, 0);

    List<LabeledSource<?>> sources = wrap(d1, d2, d3);
    Object result = earliestStrategy.merge(sources, fieldDef, "value");

    assertEquals(d1, result);
  }

  @Test
  @DisplayName("Strategy handles nulls gracefully")
  void handleNulls() {
    LocalDate d1 = LocalDate.of(2020, 1, 1);
    List<LabeledSource<?>> sources = wrap(null, d1, null);

    assertEquals(d1, latestStrategy.merge(sources, fieldDef, "value"));
    assertEquals(d1, earliestStrategy.merge(sources, fieldDef, "value"));
  }

  @Test
  @DisplayName("Returns default value if all sources are null")
  void allNullsReturnsDefault() {
    fieldDef.setDefaultValue(null);
    List<LabeledSource<?>> sources = wrap(null, null);
    assertNull(latestStrategy.merge(sources, fieldDef, "value"));

    fieldDef.setDefaultValue(
        "default"); // Just checking fallback mechanism, though type mismatch could happen in
    // real usage if not careful
    assertEquals("default", latestStrategy.merge(sources, fieldDef, "value"));
  }

  // Helber to create sources where "source" is an object with a "value" field
  // Or simply map merge calls to expect object having field.
  // Wait, the strategies assume the source IS the object holding the field.
  // So we need a simple wrapper class.

  private List<LabeledSource<?>> wrap(Object... values) {
    if (values == null) return Collections.emptyList();
    return Arrays.stream(values)
        .map(v -> new LabeledSource<>("test", new ValueWrapper(v)))
        .collect(java.util.stream.Collectors.toList());
  }

  public static class ValueWrapper {
    public Object value;

    public ValueWrapper(Object value) {
      this.value = value;
    }
  }
}
