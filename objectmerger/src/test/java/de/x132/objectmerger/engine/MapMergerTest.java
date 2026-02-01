package de.x132.objectmerger.engine;

import static org.junit.jupiter.api.Assertions.*;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapMergerTest {

  @Test
  void testMerge_SimpleMap() {
    MergeDefinition def = new MergeDefinition();
    Map<String, FieldDefinition<?>> fields = new HashMap<>();
    fields.put("key", StandardFieldDefinition.<String>builder().build());
    def.setDefinitions(fields);

    Map<String, Object> map1 = new HashMap<>();
    map1.put("key", "val1");
    Map<String, Object> map2 = new HashMap<>();
    map2.put("key", "val2");

    @SuppressWarnings("unchecked")
    LabeledSource<Map<String, Object>>[] sources =
        new LabeledSource[] {new LabeledSource<>("s1", map1), new LabeledSource<>("s2", map2)};

    Map<String, Object> result = MapMerger.merge(def, sources);
    assertEquals("val1", result.get("key"));
  }

  @Test
  void testMerge_EmptySources() {
    MergeDefinition def = new MergeDefinition();
    def.setDefinitions(new HashMap<>());

    @SuppressWarnings("unchecked")
    LabeledSource<Map<String, Object>>[] sources = new LabeledSource[] {};

    Map<String, Object> result = MapMerger.merge(def, sources);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
