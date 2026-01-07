package de.x132.family;

import lombok.Data;

import java.util.List;

@Data
public class Family {
    private String familyName;
    private List<FamilyMember> members;
}
