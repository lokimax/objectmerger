package de.x132.objectmerger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing the request payload for merging object sources.
 *
 * <p>Uses a Java Record to satisfy immutability requirements and reduce Lombok boilerplate.
 *
 * @param targetClass Fully qualified name of the target class to instantiate or map.
 * @param definition The configuration details for merging fields (strategies, priorities, etc.).
 * @param sources List of input data objects labeled by their origins.
 */
@Schema(description = "Request to merge multiple sources into a single object")
public record MergeRequest(
        @JsonProperty("targetClass")
                @Schema(
                        description = "Fully qualified target class name",
                        example = "de.x132.cli.Person")
                String targetClass,
        @JsonProperty("definition")
                @Schema(description = "Merge definition with strategies and priorities")
                Map<String, Map<String, Object>> definition,
        @JsonProperty("sources") @Schema(description = "Labeled sources to merge")
                List<LabeledSourceDTO> sources) {}
