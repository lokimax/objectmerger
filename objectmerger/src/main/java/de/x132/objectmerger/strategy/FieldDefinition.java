package de.x132.objectmerger.strategy;

import de.x132.objectmerger.strategy.config.Defaultable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class FieldDefinition implements Defaultable {
  private String strategy;
  private Object defaultValue;
}
