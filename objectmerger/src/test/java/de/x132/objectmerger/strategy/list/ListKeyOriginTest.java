package de.x132.objectmerger.strategy.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("List Key Origin / Template Logic Tests")
class ListKeyOriginTest {

  private ListMergeStrategy strategy;
  private ListFieldDefinition fieldDef;

  @BeforeEach
  void setUp() {
    strategy = new ListMergeStrategy();
    fieldDef = new ListFieldDefinition();
    fieldDef.setIdentifyBy("id");
    ItemMergeDefinition itemDef = new ItemMergeDefinition();
    StandardFieldDefinition idDef = new StandardFieldDefinition();
    itemDef.setDefinitions(Map.of("id", idDef));
    fieldDef.setItemMergeDefinition(itemDef);
  }

  @Test
  @DisplayName("Should apply Union Logic (Template): Keep items present in Key Origins")
  void testTemplateUnion() {
    fieldDef.setKeyOriginLabels(List.of("A", "B"));
    fieldDef.setRequirePresenceInAllKeyOrigins(false);

    List<LabeledSource<?>> sources =
        createSources(
            "A", List.of(Map.of("id", "1")),
            "B", List.of(Map.of("id", "2")),
            "C", List.of(Map.of("id", "3")));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result =
        (List<Map<String, Object>>) strategy.merge(sources, fieldDef, "items");

    assertEquals(2, result.size());
    assertTrue(containsId(result, "1"));
    assertTrue(containsId(result, "2"));
  }

  @Test
  @DisplayName("Should apply Intersection Logic: Keep items present in ALL Key Origins")
  void testIntersectionStrict() {
    fieldDef.setKeyOriginLabels(List.of("A", "B"));
    fieldDef.setRequirePresenceInAllKeyOrigins(true);

    List<LabeledSource<?>> sources =
        createSources(
            "A", List.of(Map.of("id", "1"), Map.of("id", "2"), Map.of("id", "4")),
            "B", List.of(Map.of("id", "2"), Map.of("id", "3"), Map.of("id", "4")),
            "C",
                List.of(
                    Map.of("id", "1"), Map.of("id", "2"), Map.of("id", "3"), Map.of("id", "4")));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result =
        (List<Map<String, Object>>) strategy.merge(sources, fieldDef, "items");

    assertEquals(2, result.size());
    assertTrue(containsId(result, "2"));
    assertTrue(containsId(result, "4"));
  }

  @Test
  @DisplayName(
      "Should allow intersection of ALL sources if Key Origins list is implicitly All (Wait, logic requires explicit list for intersection?)")
  void testIntersectionExplicitAll() {
    fieldDef.setKeyOriginLabels(List.of("A", "B"));
    fieldDef.setRequirePresenceInAllKeyOrigins(true);

    List<LabeledSource<?>> sources =
        createSources(
            "A", List.of(Map.of("id", "1")),
            "B", List.of(Map.of("id", "1")));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result =
        (List<Map<String, Object>>) strategy.merge(sources, fieldDef, "items");
    assertEquals(1, result.size());
    assertTrue(containsId(result, "1"));
  }

  @Test
  @DisplayName("Should keep item if it is in Key Origin (Mixed Sources)")
  void testMixedSourcesTemplate() {
    fieldDef.setKeyOriginLabels(List.of("A"));

    List<LabeledSource<?>> sources =
        createSources(
            "A", List.of(Map.of("id", "1")),
            "B", List.of(Map.of("id", "1"), Map.of("id", "2")));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result =
        (List<Map<String, Object>>) strategy.merge(sources, fieldDef, "items");

    assertEquals(1, result.size());
    assertTrue(containsId(result, "1"));
  }

  private List<LabeledSource<?>> createSources(
      String label1, List<Object> list1, String label2, List<Object> list2) {
    List<LabeledSource<?>> sources = new ArrayList<>();
    sources.add(new LabeledSource<>(label1, Map.of("items", list1)));
    sources.add(new LabeledSource<>(label2, Map.of("items", list2)));
    return sources;
  }

  private List<LabeledSource<?>> createSources(
      String l1, List<Object> li1, String l2, List<Object> li2, String l3, List<Object> li3) {
    List<LabeledSource<?>> sources = new ArrayList<>();
    sources.add(new LabeledSource<>(l1, Map.of("items", li1)));
    sources.add(new LabeledSource<>(l2, Map.of("items", li2)));
    sources.add(new LabeledSource<>(l3, Map.of("items", li3)));
    return sources;
  }

  private boolean containsId(List<Map<String, Object>> result, String id) {
    return result.stream().anyMatch(m -> id.equals(m.get("id")));
  }
}
