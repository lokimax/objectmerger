package de.x132.objectmerger.strategy;

import de.x132.objectmerger.strategy.config.Defaultable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class FieldDefinition<T> implements Defaultable<T> {
  private String strategy;
  private T defaultValue;
}
