package de.x132.objectmerger.helper;

import de.x132.objectmerger.exception.MergeExecutionException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Helper class for reflection operations. Encapsulates field access and modification logic. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionHelper {

    private static final ConcurrentHashMap<String, Field> fieldCache = new ConcurrentHashMap<>();

    /** Sets a field value safely. */
    public static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.error("Access denied for field '{}'", field.getName(), e);
            throw new MergeExecutionException("Access denied for field: " + field.getName(), e);
        }
    }

    /** Gets a field value safely from an object or Map. */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(fieldName);
        }
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            log.debug(
                    "Failed to get field value '{}' from object of type '{}'",
                    fieldName,
                    obj.getClass().getName());
            throw new MergeExecutionException(
                    "Failed to get field value: "
                            + fieldName
                            + " from object of type "
                            + obj.getClass().getName());
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            log.error("Access denied for field '{}'", fieldName, e);
            throw new MergeExecutionException("Access denied for field: " + fieldName, e);
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
