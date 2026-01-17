package de.x132.objectmerger;

import lombok.Data;

@Data
public abstract class FieldDefinition {
  private String strategy;
  private Object defaultValue;
}
