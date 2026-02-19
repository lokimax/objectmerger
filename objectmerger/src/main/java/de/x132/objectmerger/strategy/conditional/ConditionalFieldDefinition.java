package de.x132.objectmerger.strategy.conditional;

import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.config.ConditionalConfig;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConditionalFieldDefinition<T> extends FieldDefinition<T>
        implements ConditionalConfig<T> {

    @Builder.Default private List<ConditionCase<T>> cases = new ArrayList<>();

    private FieldDefinition<T> defaultStrategy;
}
