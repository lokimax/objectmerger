package de.x132.objectmerger;

import de.x132.objectmerger.strategy.conditional.ConditionCase;
import de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionalMergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityMergeStrategy;
import de.x132.objectmerger.strategy.recursive.RecursiveFieldDefinition;
import de.x132.objectmerger.strategy.recursive.RecursiveMergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConditionalRecursiveTest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Address {
    private String street;
    private String zip;
    private boolean valid;
  }

  @Test
  void testConditionalIncludesRecursive() {
    // Scenario:
    // If source2.valid == true, use Recursive merge (Street -> Source2, Zip ->
    // Source1)
    // Else, use Standard merge (Source 1 entirely, ignoring Source 2 invalid data)

    Address a1 = new Address("Old St", "12345", true);
    Address a2 = new Address("New St", "99999", true); // Valid API response

    // 1. Define Recursive Strategy (The "Target" behavior)
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> nestedFields = new HashMap<>();
    PriorityFieldDefinition<String> streetDef = new PriorityFieldDefinition<>();
    streetDef.setStrategy(PriorityMergeStrategy.NAME);
    streetDef.setPriority(Map.of("source2", 1, "source1", 2));
    nestedFields.put("street", streetDef);

    // Zip: source1 > source2
    PriorityFieldDefinition<String> zipDef = new PriorityFieldDefinition<>();
    zipDef.setStrategy(PriorityMergeStrategy.NAME);
    zipDef.setPriority(Map.of("source1", 1, "source2", 2));
    nestedFields.put("zip", zipDef);

    RecursiveFieldDefinition<Address> recursiveDef = new RecursiveFieldDefinition<>();
    recursiveDef.setStrategy(RecursiveMergeStrategy.NAME);
    recursiveDef.setNestedDefinition(new MergeDefinition(nestedFields));

    // 2. Define Conditional Strategy
    ConditionalFieldDefinition<Address> conditionalDef = new ConditionalFieldDefinition<>();
    conditionalDef.setStrategy(ConditionalMergeStrategy.NAME);

    // Case 1: source2 is valid -> Use Recursive
    ConditionCase<Address> validCase = new ConditionCase<>();
    validCase.setCondition("values['source2'].valid == true");
    validCase.setUseStrategy(recursiveDef);

    conditionalDef.setCases(List.of(validCase));

    // Default: Standard (implicit? or explicit StandardFieldDefinition)
    // Default behavior of Conditional is Standard if no default set?
    // No, strategy logic: "if defaultStrategy != null... else return null".
    // We should set a default strategy to be safe, e.g. Priority(source1) or
    // Standard.
    StandardFieldDefinition<Address> fallback = new StandardFieldDefinition<>();
    // Standard usually picks first non-null. Order is source1, source2. So Source1.
    conditionalDef.setDefaultStrategy(fallback);

    MergeDefinition rootDef = new MergeDefinition();
    rootDef.setDefinitions(Map.of("self", conditionalDef)); // Wait, we are merging generic object?

    // Testing wrapping object:
    // Let's merge Address directly? PojoMerger merges Fields of targetClass.
    // So we need a container class.

    Wrapper w1 = new Wrapper(a1);
    Wrapper w2 = new Wrapper(a2);

    MergeDefinition wrapperDef = new MergeDefinition();
    wrapperDef.setDefinitions(Map.of("address", conditionalDef));

    // Test 1: Valid API
    Wrapper result1 =
        ObjectMerger.merge(
            Wrapper.class,
            wrapperDef,
            new LabeledSource<>("source1", w1),
            new LabeledSource<>("source2", w2));

    // Expect: Recursive merge (Street=New, Zip=12345)
    Assertions.assertEquals("New St", result1.getAddress().getStreet());
    Assertions.assertEquals("12345", result1.getAddress().getZip());

    // Test 2: Invalid API
    a2.setValid(false);
    // a2.setStreet("Bad St");

    Wrapper result2 =
        ObjectMerger.merge(
            Wrapper.class,
            wrapperDef,
            new LabeledSource<>("source1", w1),
            new LabeledSource<>("source2", w2));

    // Expect: Fallback to Standard (Source 1)
    Assertions.assertEquals("Old St", result2.getAddress().getStreet());
    Assertions.assertEquals("12345", result2.getAddress().getZip());
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Wrapper {
    private Address address;
  }
}
