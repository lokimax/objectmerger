package de.x132.objectmerger.strategy.standard;

import de.x132.objectmerger.strategy.FieldDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StandardFieldDefinition<T> extends FieldDefinition<T> {}
