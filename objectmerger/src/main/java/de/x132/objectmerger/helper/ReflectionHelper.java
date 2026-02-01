package de.x132.objectmerger.helper;

import java.lang.reflect.Field;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for reflection operations. Encapsulates field access and
 * modification logic.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionHelper {

    private static final java.util.concurrent.ConcurrentHashMap<String, Field> fieldCache = new java.util.concurrent.ConcurrentHashMap<>();

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
        if (obj == null)
            return null;
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(fieldName);
        }
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            // Logic for missing field is handled by getField returning null or logging
            // But getField logs warn and returns null if missing.
            // Original code threw RuntimeException if field missing (via getDeclaredField
            // throwing)
            // Let's preserve behavior: if getField returns null, we should probably throw
            // or log as before depending on context.
            // The original getFieldValue caught NoSuchFieldException and threw
            // RuntimeException.
            // My getField implementation below handles NoSuchField, so here we check for
            // null.
            log.debug(
                    "Failed to get field value '{}' from object of type '{}'",
                    fieldName,
                    obj.getClass().getName());
            throw new RuntimeException(
                    "Failed to get field value: "
                            + fieldName
                            + " from object of type "
                            + obj.getClass().getName());
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            log.error("Access denied for field '{}'", fieldName, e);
            throw new RuntimeException("Access denied for field: " + fieldName, e);
        }
    }

    /** Gets a declared field from a class safely, with caching. */
    public static Field getField(Class<?> clazz, String fieldName) {
        String cacheKey = clazz.getName() + "." + fieldName;
        return fieldCache.computeIfAbsent(
                cacheKey,
                key -> {
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        return field;
                    } catch (NoSuchFieldException e) {
                        log.warn(
                                "Field '{}' defined in mapping but missing in class '{}'",
                                fieldName,
                                clazz.getName());
                        return null;
                    }
                });
    }
}
