package de.x132.objectmerger.strategy.nashorn;

import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.config.NashornConfig;
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
public class NashornFieldDefinition extends FieldDefinition<Object> implements NashornConfig {
    private String expression;
}
