package de.x132.objectmerger.strategy.conditional;

import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.config.ConditionalConfig;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionalFieldDefinition<T> extends FieldDefinition<T>
    implements ConditionalConfig<T> {

  private List<ConditionCase<T>> cases = new ArrayList<>();

  private FieldDefinition<T> defaultStrategy;
}
