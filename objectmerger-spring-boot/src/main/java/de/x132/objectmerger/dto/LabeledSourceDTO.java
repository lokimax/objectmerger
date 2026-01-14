package de.x132.objectmerger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A labeled source for merging")
public class LabeledSourceDTO {

  @Schema(description = "Label/identifier for the source", example = "database")
  private String label;

  @Schema(description = "The source object data")
  private Object data;
}
