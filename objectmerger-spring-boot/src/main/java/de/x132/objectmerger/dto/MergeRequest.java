package de.x132.objectmerger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@Schema(description = "Request to merge multiple sources into a single object")
public class MergeRequest {

  @JsonProperty("targetClass")
  @Schema(description = "Fully qualified target class name", example = "de.x132.cli.Person")
  private String targetClass;

  @JsonProperty("definition")
  @Schema(description = "Merge definition with strategies and priorities")
  private Map<String, Map<String, Object>> definition;

  @JsonProperty("sources")
  @Schema(description = "Labeled sources to merge")
  private List<LabeledSourceDTO> sources;
}
