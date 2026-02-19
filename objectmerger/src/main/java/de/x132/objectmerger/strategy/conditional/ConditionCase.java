package de.x132.objectmerger.strategy.conditional;

import de.x132.objectmerger.strategy.FieldDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionCase<T> {
    /**
     * MVEL expression evaluating to boolean. Available variables: 'sources' (List<LabeledSource>),
     * 'values' (Map<String, Object>).
     */
    private String condition;

    /** The strategy to apply if condition is true. */
    private FieldDefinition<T> useStrategy;
}
