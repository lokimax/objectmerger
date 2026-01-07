package de.x132.family;

import java.util.List;
import lombok.Data;

@Data
public class Family {
  private String familyName;
  private List<FamilyMember> members;
}
