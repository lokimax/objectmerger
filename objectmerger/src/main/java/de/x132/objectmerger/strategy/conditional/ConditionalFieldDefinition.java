package de.x132.objectmerger.strategy.conditional;

import de.x132.objectmerger.strategy.FieldDefinition;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionalFieldDefinition extends FieldDefinition {

  private List<ConditionCase> cases = new ArrayList<>();

  private FieldDefinition defaultStrategy;
}
