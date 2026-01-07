package de.x132;

import lombok.Data;

import java.util.Map;

@Data
public class MergeDefinition {
    private Map<String, FieldDefinition> definitions;
}
