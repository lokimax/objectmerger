package de.x132.objectmerger.helper;

import static org.junit.jupiter.api.Assertions.*;

import de.x132.objectmerger.exception.MergeExecutionException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReflectionHelperTest {

  @Test
  void testGetFieldValue_FromObject() {
    TestObject obj = new TestObject("testValue");
    Object value = ReflectionHelper.getFieldValue(obj, "field");
    assertEquals("testValue", value);
  }

  @Test
  void testGetFieldValue_FromMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("field", "mapValue");
    Object value = ReflectionHelper.getFieldValue(map, "field");
    assertEquals("mapValue", value);
  }

  @Test
  void testGetFieldValue_Null() {
    assertNull(ReflectionHelper.getFieldValue(null, "field"));
  }

  @Test
  void testGetFieldValue_MissingField() {
    TestObject obj = new TestObject("testValue");

    assertThrows(
        MergeExecutionException.class, () -> ReflectionHelper.getFieldValue(obj, "missingField"));
  }

  @Test
  void testSetFieldValue() throws NoSuchFieldException {
    TestObject obj = new TestObject("initial");
    Field field = TestObject.class.getDeclaredField("field");
    ReflectionHelper.setFieldValue(field, obj, "updated");
    assertEquals("updated", obj.getField());
  }

  @Test
  void testGetField_Success() {
    Field field = ReflectionHelper.getField(TestObject.class, "field");
    assertNotNull(field);
    assertEquals("field", field.getName());
  }

  @Test
  void testGetField_Missing() {
    Field field = ReflectionHelper.getField(TestObject.class, "missingField");
    assertNull(field);
  }

  private static class TestObject {
    private String field;

    public TestObject(String field) {
      this.field = field;
    }

    public String getField() {
      return field;
    }
  }
}
