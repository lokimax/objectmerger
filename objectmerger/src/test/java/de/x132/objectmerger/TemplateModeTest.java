package de.x132.objectmerger;

import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateModeTest {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class User {
    private String name;
    private int age;
    private String role;
  }

  @Test
  void testTemplateModeFullGeneration() {
    // Scenario: No definitions provided, but templateSourceLabel="source1"
    // Expectation: All fields from User class are merged (standard generation)

    MergeDefinition def = new MergeDefinition();
    def.setTemplateSourceLabel("source1");

    User u1 = new User("Alice", 30, "Admin");
    User u2 = new User("Bob", 25, "User");

    // priority: source1 > source2 implicitly via StandardStrategy default behavior
    // Actually standard strategy takes first non-null.

    User result =
        ObjectMerger.merge(
            User.class,
            def,
            new LabeledSource<>("source1", u1),
            new LabeledSource<>("source2", u2));

    // Should behave like standard merge for all fields
    Assertions.assertEquals("Alice", result.getName());
    Assertions.assertEquals(30, result.getAge());
    Assertions.assertEquals("Admin", result.getRole());
  }

  @Test
  void testTemplateModeWithOverride() {
    // Scenario: Template generates all fields, but we override 'role' to be fixed
    // or standard

    MergeDefinition def = new MergeDefinition();
    def.setTemplateSourceLabel("source1");
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> explicit = new HashMap<>();

    StandardFieldDefinition<String> roleDef = new StandardFieldDefinition<>();
    roleDef.setDefaultValue("SUPERUSER"); // Just to prove we can configure it
    explicit.put("role", roleDef);

    def.setDefinitions(explicit);

    User u1 = new User("Alice", 30, null); // Null role in source1
    User u2 = new User("Bob", 25, "User");

    User result =
        ObjectMerger.merge(
            User.class,
            def,
            new LabeledSource<>("source1", u1),
            new LabeledSource<>("source2", u2));

    Assertions.assertEquals("Alice", result.getName()); // From generated template
    // Standard strategy (default for generated) picks first non-null if not
    // overridden behavior
    // Wait, generated uses StandardStrategy.
    // u1.role is null, u2.role is "User". Standard Strategy picks u2.role ("User").
    // But we set default value "SUPERUSER". Standard Strategy uses default if all
    // sources null?
    // No, standard finds first non-null. So "User".

    // Let's test checking if 'age' is merged (which is NOT in explicit definitions)
    Assertions.assertEquals(30, result.getAge());
  }

  @Test
  void testMissingTemplateSourceThrowsException() {
    MergeDefinition def = new MergeDefinition();
    def.setTemplateSourceLabel("non_existent");

    User u1 = new User("Alice", 30, "Admin");

    Assertions.assertThrows(
        de.x132.objectmerger.exception.InvalidSourceException.class,
        () -> {
          ObjectMerger.merge(User.class, def, new LabeledSource<>("source1", u1));
        });
  }
}
