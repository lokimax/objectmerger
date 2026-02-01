package de.x132.objectmerger;

import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityMergeStrategy;
import de.x132.objectmerger.strategy.nested.NestedFieldDefinition;
import de.x132.objectmerger.strategy.nested.NestedMergeStrategy;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NestedMergeTest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Person {
    private String name;
    private Address address;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Address {
    private String street;
    private String zip;
    private String city;
  }

  @Test
  void testNestedMergeWithSelectiveFields() {
    // Sources
    Address a1 = new Address("Old St", "12345", "HomeCity");
    Person p1 = new Person("Max", a1);

    Address a2 = new Address("New St", "99999", "ApiCity"); // API has different data
    Person p2 = new Person("Max", a2);

    // Goal:
    // Street -> API (Source2)
    // Zip -> Local (Source1)
    // City -> Local (Source1) - Implicit priority

    // 1. Nested Definition for Address
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> nestedFields = new HashMap<>();

    // Street: Priority [source2, source1]
    PriorityFieldDefinition<String> streetDef = new PriorityFieldDefinition<>();
    streetDef.setStrategy(PriorityMergeStrategy.NAME);
    streetDef.setPriority(Map.of("source2", 1, "source1", 2));
    nestedFields.put("street", streetDef);

    // Zip: Priority [source1, source2]
    PriorityFieldDefinition<String> zipDef = new PriorityFieldDefinition<>();
    zipDef.setStrategy(PriorityMergeStrategy.NAME);
    zipDef.setPriority(Map.of("source1", 1, "source2", 2));
    nestedFields.put("zip", zipDef);

    MergeDefinition nestedMergeDef = new MergeDefinition(nestedFields);

    // 2. Main Definition for Person
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> mainFields = new HashMap<>();

    NestedFieldDefinition<Address> addressDef = new NestedFieldDefinition<>();
    addressDef.setStrategy(NestedMergeStrategy.NAME);
    addressDef.setNestedDefinition(nestedMergeDef);
    mainFields.put("address", addressDef);

    MergeDefinition mainMergeDef = new MergeDefinition(mainFields);

    // 3. Execution
    Person result = ObjectMerger.merge(
        Person.class,
        mainMergeDef,
        new LabeledSource<>("source1", p1),
        new LabeledSource<>("source2", p2));

    // 4. Verification
    Assertions.assertNotNull(result.getAddress());
    Assertions.assertEquals(
        "New St", result.getAddress().getStreet(), "Street should come from Source2 (API)");
    Assertions.assertEquals(
        "12345", result.getAddress().getZip(), "Zip should come from Source1 (Local)");

    Assertions.assertNull(
        result.getAddress().getCity(),
        "City should be null because it was not in nested definition");
  }

  @Test
  void testNestedMergeWithTemplateModeSynergy() {
    // Same as above, but using Template Mode for the nested definition to include
    // ALL fields automatically

    Address a1 = new Address("Old St", "12345", "HomeCity");
    Person p1 = new Person("Max", a1);

    Address a2 = new Address("New St", "99999", "ApiCity");
    Person p2 = new Person("Max", a2);

    // Nested Definition: Use Template Mode "source1" to pick up all fields (street,
    // zip, city)
    MergeDefinition nestedMergeDef = new MergeDefinition();
    nestedMergeDef.setTemplateSourceLabel("source1");

    // Override Street to prioritize API
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> explicitNested = new HashMap<>();
    PriorityFieldDefinition<String> streetDef = new PriorityFieldDefinition<>();
    streetDef.setStrategy(PriorityMergeStrategy.NAME);
    streetDef.setPriority(Map.of("source2", 1, "source1", 2));
    explicitNested.put("street", streetDef);
    nestedMergeDef.setDefinitions(explicitNested);

    // Main Definition
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> mainFields = new HashMap<>();
    NestedFieldDefinition<Address> addressDef = new NestedFieldDefinition<>();
    addressDef.setStrategy(NestedMergeStrategy.NAME);
    addressDef.setNestedDefinition(nestedMergeDef);
    mainFields.put("address", addressDef);

    MergeDefinition mainMergeDef = new MergeDefinition(mainFields);

    Person result = ObjectMerger.merge(
        Person.class,
        mainMergeDef,
        new LabeledSource<>("source1", p1),
        new LabeledSource<>("source2", p2));

    Assertions.assertEquals("New St", result.getAddress().getStreet());
    Assertions.assertEquals(
        "12345", result.getAddress().getZip());

    Assertions.assertEquals(
        "HomeCity", result.getAddress().getCity(), "City should be included via Template Mode");
  }
}
