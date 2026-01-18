package de.x132.objectmerger.strategy.mvel;

import de.x132.objectmerger.strategy.FieldDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MvelFieldDefinition extends FieldDefinition {
  private String expression;
}
