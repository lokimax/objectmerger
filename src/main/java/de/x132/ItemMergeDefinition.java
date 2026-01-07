package de.x132;

import lombok.Data;

import java.util.Map;

@Data
public class ItemMergeDefinition {
    private String targetClass;
    private Map<String, FieldDefinition> definitions;
}
