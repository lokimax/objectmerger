package de.x132;

import de.x132.strategy.ListMergeStrategy;
import de.x132.strategy.MaximumValueStrategy;
import de.x132.strategy.PriorityMergeStrategy;
import de.x132.strategy.MergeStrategy;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ObjectMerger {

    private static final Map<String, MergeStrategy<?>> STRATEGIES = Map.of(
            "mergeList", new ListMergeStrategy(),
            "maximum", new MaximumValueStrategy(),
            "priority", new PriorityMergeStrategy()
    );

    @SafeVarargs
    public static <T> T merge(Class<T> targetClass, MergeDefinition mergeDefinition, LabeledSource<T>... sources) {
        try {
            T result = targetClass.getDeclaredConstructor().newInstance();
            Map<String, FieldDefinition> definitions = mergeDefinition.getDefinitions();
            List<LabeledSource<T>> sourceList = Arrays.asList(sources);

            for (Map.Entry<String, FieldDefinition> entry : definitions.entrySet()) {
                String fieldName = entry.getKey();
                FieldDefinition fieldDef = entry.getValue();
                Field field = targetClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                
                MergeStrategy<?> strategy = STRATEGIES.get(
                        fieldDef.getStrategy() != null ? fieldDef.getStrategy() : "priority"
                );
                if (strategy != null) {
                    field.set(result, strategy.merge((List) sourceList, fieldDef, fieldName));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge objects", e);
        }
    }
    
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) return null;
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    public static MergeDefinition toMergeDefinition(ItemMergeDefinition itemMergeDefinition) {
        MergeDefinition mergeDefinition = new MergeDefinition();
        mergeDefinition.setDefinitions(itemMergeDefinition.getDefinitions());
        return mergeDefinition;
    }
}
