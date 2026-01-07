package de.x132;

import lombok.Data;

import java.util.Map;

@Data
public class FieldDefinition {
    private Map<String, Integer> priority;
    private String strategy;
    private String identifyBy;
    private ItemMergeDefinition itemMergeDefinition;
    private Object defaultValue;
}
