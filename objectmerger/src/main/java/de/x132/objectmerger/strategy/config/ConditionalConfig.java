package de.x132.objectmerger.strategy.config;

import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionCase;
import java.util.List;

public interface ConditionalConfig {
  List<ConditionCase> getCases();

  FieldDefinition getDefaultStrategy();
}
