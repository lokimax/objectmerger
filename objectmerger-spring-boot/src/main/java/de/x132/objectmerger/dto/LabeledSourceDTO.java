package de.x132.objectmerger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object representing a single labeled source object.
 *
 * <p>Uses a Java Record to minimize boilerplate and enforce immutability for input payloads.
 *
 * @param label The label/identifier for the source.
 * @param data The source object data.
 */
@Schema(description = "A labeled source for merging")
public record LabeledSourceDTO(
        @Schema(description = "Label/identifier for the source", example = "database") String label,
        @Schema(description = "The source object data") Object data) {}
