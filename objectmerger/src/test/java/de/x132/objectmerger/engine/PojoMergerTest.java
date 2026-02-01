package de.x132.objectmerger.engine;

import static org.junit.jupiter.api.Assertions.*;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PojoMergerTest {

  @Test
  void testMerge_SimpleSuccess() {
    MergeDefinition def = new MergeDefinition();
    Map<String, FieldDefinition<?>> fields = new HashMap<>();
    fields.put("value", StandardFieldDefinition.<String>builder().build());
    def.setDefinitions(fields);

    TestPojo source1 = new TestPojo("A");
    TestPojo source2 = new TestPojo(null);

    @SuppressWarnings("unchecked")
    LabeledSource<TestPojo>[] sources =
        new LabeledSource[] {
          new LabeledSource<>("s1", source1), new LabeledSource<>("s2", source2)
        };

    TestPojo result = PojoMerger.merge(TestPojo.class, def, sources);
    assertEquals("A", result.getValue());
  }

  @Test
  void testMerge_InstantiationError() {
    MergeDefinition def = new MergeDefinition();
    @SuppressWarnings("unchecked")
    LabeledSource<PrivatePojo>[] sources = new LabeledSource[] {};

    RuntimeException ex =
        assertThrows(
            RuntimeException.class, () -> PojoMerger.merge(PrivatePojo.class, def, sources));
    assertTrue(ex.getMessage().contains("Failed to merge objects"));
  }

  public static class TestPojo {
    private String value;

    public TestPojo() {}

    public TestPojo(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  private static class PrivatePojo {
    private PrivatePojo() {}
  }
}
