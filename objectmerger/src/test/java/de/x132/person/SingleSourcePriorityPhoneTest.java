package de.x132.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SingleSourcePriorityPhoneTest {

  private MergeDefinition phonePriorityCrmOnlyDefinition() {
    FieldDefinition phoneDef = new FieldDefinition();
    Map<String, Integer> prio = new HashMap<>();
    prio.put("crm", 1);
    phoneDef.setPriority(prio);
    phoneDef.setDefaultValue(null);

    MergeDefinition def = new MergeDefinition();
    Map<String, FieldDefinition> defs = new HashMap<>();
    defs.put("phone", phoneDef);
    def.setDefinitions(defs);
    return def;
  }

  @Test
  void phone_uses_crm_when_present() {
    // Given
    Person analytics = new Person();
    analytics.setPhone("030-654321");

    Person crm = new Person();
    crm.setPhone("030-123456");

    Person database = new Person();
    database.setPhone(null);

    MergeDefinition def = phonePriorityCrmOnlyDefinition();

    // When
    Person merged =
        ObjectMerger.merge(
            Person.class,
            def,
            new LabeledSource<>("analytics", analytics),
            new LabeledSource<>("crm", crm),
            new LabeledSource<>("database", database));

    // Then
    assertEquals("030-123456", merged.getPhone());
  }

  @Test
  void phone_is_null_when_crm_null_even_if_others_have_value() {
    // Given
    Person analytics = new Person();
    analytics.setPhone("030-654321");

    Person crm = new Person();
    crm.setPhone(null);

    Person database = new Person();
    database.setPhone("030-000000");

    MergeDefinition def = phonePriorityCrmOnlyDefinition();

    // When
    Person merged =
        ObjectMerger.merge(
            Person.class,
            def,
            new LabeledSource<>("analytics", analytics),
            new LabeledSource<>("crm", crm),
            new LabeledSource<>("database", database));

    // Then
    assertEquals(null, merged.getPhone());
  }

  @Test
  void phone_is_default_when_crm_source_missing() {
    // Given
    Person analytics = new Person();
    analytics.setPhone("030-654321");

    Person database = new Person();
    database.setPhone("030-000000");

    MergeDefinition def = phonePriorityCrmOnlyDefinition();

    // When: no CRM source is provided
    Person merged =
        ObjectMerger.merge(
            Person.class,
            def,
            new LabeledSource<>("analytics", analytics),
            new LabeledSource<>("database", database));

    // Then
    assertEquals(null, merged.getPhone());
  }
}
