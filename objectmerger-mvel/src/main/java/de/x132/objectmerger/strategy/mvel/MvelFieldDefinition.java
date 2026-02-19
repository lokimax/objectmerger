package de.x132.objectmerger.strategy.mvel;

import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.config.MvelConfig;
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
public class MvelFieldDefinition extends FieldDefinition<Object> implements MvelConfig {
    private String expression;
}
