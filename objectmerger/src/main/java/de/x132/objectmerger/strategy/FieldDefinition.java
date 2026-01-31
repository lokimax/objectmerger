package de.x132.objectmerger.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import de.x132.objectmerger.strategy.config.Defaultable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class FieldDefinition implements Defaultable {
  private String strategy;
  private Object defaultValue;
}
