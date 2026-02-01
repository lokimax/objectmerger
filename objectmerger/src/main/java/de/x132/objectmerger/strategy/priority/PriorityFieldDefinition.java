package de.x132.objectmerger.strategy.priority;

import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.config.Prioritizable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PriorityFieldDefinition<T> extends FieldDefinition<T> implements Prioritizable {
  private Map<String, Integer> priority;
}
