package de.x132.objectmerger.strategy.map;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import java.util.List;
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
public class MapFieldDefinition<T> extends FieldDefinition<T> {
    private String keyStrategy;
    private List<String> keyTemplateSources;
    private ItemMergeDefinition itemMergeDefinition;
}
