package de.x132.objectmerger.service;

import static org.junit.jupiter.api.Assertions.*;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.model.Person;
import de.x132.objectmerger.util.MergeDefinitionConverter;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("ObjectMergerService Integration Tests")
class ObjectMergerServiceIntegrationTest {

  @Autowired private ObjectMergerService mergerService;

  @Test
  @DisplayName("Should merge Person objects successfully")
  void testMergePerson() throws ClassNotFoundException {
    // Create merge definition using converter
    Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();
    defMap.put("name", Map.of("priority", Map.of("source1", 1)));
    defMap.put("age", Map.of("strategy", "maximum"));
    MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

    // Create Person sources
    Person person1 = new Person();
    person1.setName("John Doe");
    person1.setAge(25);

    Person person2 = new Person();
    person2.setName("Jane Doe");
    person2.setAge(30);

    List<LabeledSource<?>> sources =
        Arrays.asList(
            new LabeledSource<>("source1", person1), new LabeledSource<>("source2", person2));

    // Merge
    Object result = mergerService.merge("de.x132.objectmerger.model.Person", definition, sources);

    assertNotNull(result);
    assertTrue(result instanceof Person);
    Person merged = (Person) result;
    assertEquals("John Doe", merged.getName());
    assertEquals(30, merged.getAge());
  }

  @Test
  @DisplayName("Should throw SecurityException for unlisted class")
  void testMergeWithUnlistedClass() {
    Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();
    defMap.put("name", Map.of("priority", Map.of("source1", 1)));
    MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

    Person person = new Person();
    person.setName("Test");

    List<LabeledSource<?>> sources =
        Collections.singletonList(new LabeledSource<>("source1", person));

    assertThrows(
        SecurityException.class,
        () -> mergerService.merge("com.invalid.NonExistentClass", definition, sources));
  }

  @Test
  @DisplayName("Should handle empty sources list")
  void testMergeWithEmptySources() throws ClassNotFoundException {
    Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();
    defMap.put("name", Map.of("priority", Map.of("source1", 1)));
    MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

    List<LabeledSource<?>> emptySources = Collections.emptyList();

    Object result =
        mergerService.merge("de.x132.objectmerger.model.Person", definition, emptySources);

    assertNotNull(result);
    assertTrue(result instanceof Person);
  }

  @Test
  @DisplayName("Should handle single source")
  void testMergeWithSingleSource() throws ClassNotFoundException {
    Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();
    defMap.put("name", Map.of("priority", Map.of("source1", 1)));
    defMap.put("age", Map.of("strategy", "maximum"));
    MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

    Person person = new Person();
    person.setName("Single Person");
    person.setAge(42);

    List<LabeledSource<?>> sources =
        Collections.singletonList(new LabeledSource<>("source1", person));

    Object result = mergerService.merge("de.x132.objectmerger.model.Person", definition, sources);

    assertNotNull(result);
    assertTrue(result instanceof Person);
    Person merged = (Person) result;
    assertEquals("Single Person", merged.getName());
    assertEquals(42, merged.getAge());
  }
}
