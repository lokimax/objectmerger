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
    MergeDefinition def = new MergeDefinition();
    def.setTemplateSourceLabel("source1");

    User u1 = new User("Alice", 30, "Admin");
    User u2 = new User("Bob", 25, "User");

    User result =
        ObjectMerger.merge(
            User.class,
            def,
            new LabeledSource<>("source1", u1),
            new LabeledSource<>("source2", u2));

    Assertions.assertEquals("Alice", result.getName());
    Assertions.assertEquals(30, result.getAge());
    Assertions.assertEquals("Admin", result.getRole());
  }

  @Test
  void testTemplateModeWithOverride() {
    MergeDefinition def = new MergeDefinition();
    def.setTemplateSourceLabel("source1");
    Map<String, de.x132.objectmerger.strategy.FieldDefinition<?>> explicit = new HashMap<>();

    StandardFieldDefinition<String> roleDef = new StandardFieldDefinition<>();
    roleDef.setDefaultValue("SUPERUSER");
    explicit.put("role", roleDef);

    def.setDefinitions(explicit);

    User u1 = new User("Alice", 30, null);
    User u2 = new User("Bob", 25, "User");

    User result =
        ObjectMerger.merge(
            User.class,
            def,
            new LabeledSource<>("source1", u1),
            new LabeledSource<>("source2", u2));

    Assertions.assertEquals("Alice", result.getName());
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
