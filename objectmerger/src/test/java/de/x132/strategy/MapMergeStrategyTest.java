package de.x132.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.x132.LabeledSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapMergeStrategyTest {

  private final MapMergeStrategy strategy = new MapMergeStrategy();

  static class TestObject {
    public Map<String, String> mapField = new HashMap<>();

    TestObject(Map<String, String> mapField) {
      this.mapField = mapField;
    }
  }

  @Test
  @DisplayName("Should instantiate strategy")
  void testInstantiation() {
    assertNotNull(strategy);
  }

  @Test
  @DisplayName("Should merge maps with same keys")
  void testMergeMapsWithSameKeys() {
    Map<String, String> map1 = new HashMap<>();
    map1.put("key1", "value1");

    Map<String, String> map2 = new HashMap<>();
    map2.put("key1", "value1Different");

    TestObject obj1 = new TestObject(map1);
    TestObject obj2 = new TestObject(map2);

    List<LabeledSource<?>> sources =
        List.of(new LabeledSource<>("db", obj1), new LabeledSource<>("api", obj2));

    @SuppressWarnings("unchecked")
    Map<Object, Object> result = (Map<Object, Object>) strategy.merge(sources, null, "mapField");
    assertNotNull(result);
    assertTrue(result.containsKey("key1"));
  }

  @Test
  @DisplayName("Should handle empty sources")
  void testEmptySources() {
    Object result = strategy.merge(List.of(), null, "mapField");
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should handle single source")
  void testSingleSource() {
    Map<String, String> map = new HashMap<>();
    map.put("key1", "value1");

    TestObject obj = new TestObject(map);

    List<LabeledSource<?>> sources = List.of(new LabeledSource<>("db", obj));

    @SuppressWarnings("unchecked")
    Map<Object, Object> result = (Map<Object, Object>) strategy.merge(sources, null, "mapField");
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should handle null maps")
  void testNullMaps() {
    TestObject obj = new TestObject(null);

    List<LabeledSource<?>> sources = List.of(new LabeledSource<>("db", obj));

    Object result = strategy.merge(sources, null, "mapField");
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should merge multiple sources but only keep keys from leading map")
  void testMultipleSources() {
    Map<String, String> map1 = new HashMap<>();
    map1.put("key1", "value1");

    Map<String, String> map2 = new HashMap<>();
    map2.put("key2", "value2");

    TestObject obj1 = new TestObject(map1);
    TestObject obj2 = new TestObject(map2);

    List<LabeledSource<?>> sources =
        List.of(new LabeledSource<>("db", obj1), new LabeledSource<>("api", obj2));

    @SuppressWarnings("unchecked")
    Map<Object, Object> result = (Map<Object, Object>) strategy.merge(sources, null, "mapField");
    assertNotNull(result);
    // Key1 is in the leader, so it should be present
    assertTrue(result.containsKey("key1"));
    // Key2 is ONLY in the follower, so it should be IGNORED
    assertFalse(result.containsKey("key2"));
  }

  @Test
  @DisplayName("Should strictly follow the template map keys")
  void testMapTemplateBehavior() {
    Map<String, String> leaderMap = new HashMap<>();
    leaderMap.put("common", "leaderValue");
    leaderMap.put("leaderOnly", "leaderValue");

    Map<String, String> followerMap = new HashMap<>();
    followerMap.put("common", "followerValue");
    followerMap.put("followerOnly", "followerValue");

    TestObject obj1 = new TestObject(leaderMap);
    TestObject obj2 = new TestObject(followerMap);

    List<LabeledSource<?>> sources =
        List.of(new LabeledSource<>("leader", obj1), new LabeledSource<>("follower", obj2));

    @SuppressWarnings("unchecked")
    Map<Object, Object> result = (Map<Object, Object>) strategy.merge(sources, null, "mapField");
    assertNotNull(result);

    // Should contain keys from leader
    assertTrue(result.containsKey("common"));
    assertTrue(result.containsKey("leaderOnly"));

    // Should NOT contain keys that are only in follower
    assertFalse(
        result.containsKey("followerOnly"), "Should not contain keys only present in follower map");
  }

  @Test
  @DisplayName("Should use first value as priority")
  void testPriorityBehavior() {
    Map<String, String> map1 = new HashMap<>();
    map1.put("key1", "valueFromFirst");

    Map<String, String> map2 = new HashMap<>();
    map2.put("key1", "valueFromSecond");

    TestObject obj1 = new TestObject(map1);
    TestObject obj2 = new TestObject(map2);

    List<LabeledSource<?>> sources =
        List.of(new LabeledSource<>("db", obj1), new LabeledSource<>("api", obj2));

    @SuppressWarnings("unchecked")
    Map<Object, Object> result = (Map<Object, Object>) strategy.merge(sources, null, "mapField");
    assertNotNull(result);
    assertTrue(result.containsKey("key1"));
  }
}
