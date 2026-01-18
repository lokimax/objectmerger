package de.x132.objectmerger.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class FieldDefinition {
  private String strategy;
  private Object defaultValue;
}
