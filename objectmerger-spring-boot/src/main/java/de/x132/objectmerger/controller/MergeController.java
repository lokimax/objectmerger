package de.x132.objectmerger.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.x132.LabeledSource;
import de.x132.MergeDefinition;
import de.x132.objectmerger.dto.MergeRequest;
import de.x132.objectmerger.service.ObjectMergerService;
import de.x132.objectmerger.util.MergeDefinitionConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/merge")
@Tag(name = "ObjectMerger", description = "Merge multiple sources into a single object using strategies")
public class MergeController {

    private final ObjectMergerService mergerService;

    public MergeController(ObjectMergerService mergerService) {
        this.mergerService = mergerService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "Merge multiple sources (JSON)", description = "Merge multiple labeled sources into a single object using the provided merge definition (JSON format)")
    @ApiResponse(responseCode = "200", description = "Merge successful")
    @ApiResponse(responseCode = "400", description = "Invalid request or merge failed")
    public ResponseEntity<?> merge(@RequestBody MergeRequest request) {
        try {
            // Convert definition map to MergeDefinition using Gson
            MergeDefinition definition = MergeDefinitionConverter.fromMap(request.getDefinition());

            // Convert DTOs to LabeledSources
            @SuppressWarnings("unchecked")
            List<LabeledSource<?>> sources = (List) request.getSources().stream()
                    .map(dto -> new LabeledSource<>(dto.getLabel(), dto.getData()))
                    .toList();

            // Perform merge
            Object result = mergerService.merge(request.getTargetClass(), definition, sources);

            return ResponseEntity.ok(result);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Target class not found: " + request.getTargetClass()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Merge failed: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/yaml", consumes = "application/x-yaml", produces = "application/x-yaml")
    @Operation(summary = "Merge multiple sources (YAML)", description = "Merge multiple labeled sources into a single object using the provided merge definition (YAML format)")
    @ApiResponse(responseCode = "200", description = "Merge successful")
    @ApiResponse(responseCode = "400", description = "Invalid request or merge failed")
    public ResponseEntity<?> mergeYaml(@RequestBody MergeRequest request) {
        try {
            // Convert definition map to MergeDefinition using Gson
            MergeDefinition definition = MergeDefinitionConverter.fromMap(request.getDefinition());

            // Convert DTOs to LabeledSources
            @SuppressWarnings("unchecked")
            List<LabeledSource<?>> sources = (List) request.getSources().stream()
                    .map(dto -> new LabeledSource<>(dto.getLabel(), dto.getData()))
                    .toList();

            // Perform merge
            Object result = mergerService.merge(request.getTargetClass(), definition, sources);

            return ResponseEntity.ok(result);
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Target class not found: " + request.getTargetClass()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Merge failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the merge service is running")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping("/example")
    @Operation(summary = "Merge Person example", description = "Example merge of a Person object from three sources (database, crm, analytics)")
    public ResponseEntity<?> mergePersonExample() {
        try {
            // Create merge definition using converter
            Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();

            // name: priority based
            defMap.put("name", Map.of("priority", Map.of("database", 1, "crm", 2, "analytics", 3)));

            // age: maximum
            defMap.put("age", Map.of("strategy", "maximum", "defaultValue", 0));

            // email: priority based
            defMap.put("email", Map.of("priority", Map.of("database", 1, "crm", 2, "analytics", 3)));

            // phone: priority based (different order)
            defMap.put("phone", Map.of("priority", Map.of("analytics", 1, "crm", 2, "database", 3)));

            MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

            // Create sources with LinkedHashMap to allow null values
            Map<String, Object> dbData = new LinkedHashMap<>();
            dbData.put("name", "Max Müller");
            dbData.put("age", 30);
            dbData.put("email", "max@example.com");
            dbData.put("phone", null);

            Map<String, Object> crmData = new LinkedHashMap<>();
            crmData.put("name", "Maximilian Müller");
            crmData.put("age", 25);
            crmData.put("email", null);
            crmData.put("phone", "030-123456");

            Map<String, Object> analyticsData = new LinkedHashMap<>();
            analyticsData.put("name", null);
            analyticsData.put("age", 35);
            analyticsData.put("email", "max.mueller@example.de");
            analyticsData.put("phone", "030-654321");

            @SuppressWarnings("unchecked")
            List<LabeledSource<?>> sources = (List) List.of(
                    new LabeledSource<>("database", dbData),
                    new LabeledSource<>("crm", crmData),
                    new LabeledSource<>("analytics", analyticsData));

            // Merge using the service
            Object result = mergerService.merge("de.x132.objectmerger.model.Person", definition, sources);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Example merge failed: " + e.getMessage(), "type", e.getClass().getSimpleName()));
        }
    }
}
