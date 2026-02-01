package de.x132.objectmerger.helper;

import java.lang.reflect.Field;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Helper class for reflection operations. Encapsulates field access and modification logic. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionHelper {

  /** sets a field value safely. */
  public static void setFieldValue(Field field, Object target, Object value) {
    try {
      field.setAccessible(true);
      field.set(target, value);
    } catch (IllegalAccessException e) {
      log.error("Access denied for field '{}'", field.getName(), e);
      throw new RuntimeException("Access denied for field: " + field.getName(), e);
    }
  }

  /** gets a field value safely from an object or Map. */
  public static Object getFieldValue(Object obj, String fieldName) {
    if (obj == null) return null;
    if (obj instanceof Map) {
      return ((Map<?, ?>) obj).get(fieldName);
    }
    try {
      Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(obj);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      log.debug(
          "Failed to get field value '{}' from object of type '{}'",
          fieldName,
          obj.getClass().getName());
      throw new RuntimeException(
          "Failed to get field value: "
              + fieldName
              + " from object of type "
              + obj.getClass().getName(),
          e);
    }
  }

  /** Gets a declared field from a class safely. */
  public static Field getField(Class<?> clazz, String fieldName) {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException e) {
      log.warn(
          "Field '{}' defined in mapping but missing in class '{}'", fieldName, clazz.getName());
      return null;
    }
  }
}
