package de.x132.objectmerger.strategy.priority;

import de.x132.objectmerger.FieldDefinition;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PriorityFieldDefinition extends FieldDefinition {
    private Map<String, Integer> priority;
}
