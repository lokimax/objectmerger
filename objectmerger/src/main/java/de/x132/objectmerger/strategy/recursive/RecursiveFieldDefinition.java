package de.x132.objectmerger.strategy.recursive;

import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RecursiveFieldDefinition<T> extends FieldDefinition<T> {
  @Builder.Default private MergeDefinition nestedDefinition = new MergeDefinition();
}
