package de.x132.objectmerger.controller;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.dto.MergeRequest;
import de.x132.objectmerger.generator.MergeDefinitionGenerator;
import de.x132.objectmerger.security.ClassLoadingGuard;
import de.x132.objectmerger.service.ObjectMergerService;
import de.x132.objectmerger.util.MergeDefinitionConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/merge")
@Tag(
        name = "ObjectMerger",
        description = "Merge multiple sources into a single object using strategies")
public class MergeController {

    private final ObjectMergerService mergerService;
    private final ClassLoadingGuard classLoadingGuard;

    public MergeController(ObjectMergerService mergerService, ClassLoadingGuard classLoadingGuard) {
        this.mergerService = mergerService;
        this.classLoadingGuard = classLoadingGuard;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(
            summary = "Merge multiple sources (JSON)",
            description =
                    "Merge multiple labeled sources into a single object using the provided merge definition (JSON format)")
    @ApiResponse(responseCode = "200", description = "Merge successful")
    @ApiResponse(responseCode = "400", description = "Invalid request or merge failed")
    public ResponseEntity<?> merge(@RequestBody MergeRequest request) {
        try {
            MergeDefinition definition = MergeDefinitionConverter.fromMap(request.getDefinition());

            @SuppressWarnings("unchecked")
            List<LabeledSource<?>> sources =
                    (List)
                            request.getSources().stream()
                                    .map(dto -> new LabeledSource<>(dto.getLabel(), dto.getData()))
                                    .toList();

            Object result = mergerService.merge(request.getTargetClass(), definition, sources);

            return ResponseEntity.ok(result);
        } catch (SecurityException securityException) {
            log.warn("Blocked merge request: {}", securityException.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", securityException.getMessage()));
        } catch (ClassNotFoundException classNotFoundException) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target class not found"));
        } catch (Exception e) {
            log.error("Merge failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Merge failed"));
        }
    }

    @PostMapping(value = "/yaml", consumes = "application/x-yaml", produces = "application/x-yaml")
    @Operation(
            summary = "Merge multiple sources (YAML)",
            description =
                    "Merge multiple labeled sources into a single object using the provided merge definition (YAML format)")
    @ApiResponse(responseCode = "200", description = "Merge successful")
    @ApiResponse(responseCode = "400", description = "Invalid request or merge failed")
    public ResponseEntity<?> mergeYaml(@RequestBody MergeRequest request) {
        try {
            MergeDefinition definition = MergeDefinitionConverter.fromMap(request.getDefinition());

            @SuppressWarnings("unchecked")
            List<LabeledSource<?>> sources =
                    (List)
                            request.getSources().stream()
                                    .map(dto -> new LabeledSource<>(dto.getLabel(), dto.getData()))
                                    .toList();

            Object result = mergerService.merge(request.getTargetClass(), definition, sources);

            return ResponseEntity.ok(result);
        } catch (SecurityException securityException) {
            log.warn("Blocked YAML merge request: {}", securityException.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", securityException.getMessage()));
        } catch (ClassNotFoundException classNotFoundException) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target class not found"));
        } catch (Exception e) {
            log.error("YAML merge failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Merge failed"));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the merge service is running")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping("/example")
    @Operation(
            summary = "Merge Person example",
            description =
                    "Example merge of a Person object from three sources (database, crm, analytics)")
    public ResponseEntity<?> mergePersonExample() {
        try {
            Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();

            defMap.put(
                    "name",
                    Map.of(
                            "strategy",
                            "priority",
                            "priority",
                            Map.of("database", 1, "crm", 2, "analytics", 3)));

            defMap.put("age", Map.of("strategy", "maximum", "defaultValue", 0));

            defMap.put(
                    "email",
                    Map.of(
                            "strategy",
                            "priority",
                            "priority",
                            Map.of("database", 1, "crm", 2, "analytics", 3)));

            defMap.put(
                    "phone",
                    Map.of(
                            "strategy",
                            "priority",
                            "priority",
                            Map.of("analytics", 1, "crm", 2, "database", 3)));

            MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

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
            List<LabeledSource<?>> sources =
                    (List)
                            List.of(
                                    new LabeledSource<>("database", dbData),
                                    new LabeledSource<>("crm", crmData),
                                    new LabeledSource<>("analytics", analyticsData));

            Object result =
                    mergerService.merge("de.x132.objectmerger.model.Person", definition, sources);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Example merge failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Example merge failed"));
        }
    }

    @PostMapping("/generator/class")
    @Operation(
            summary = "Generate definition from Class",
            description =
                    "Generates a default merge definition based on the fields of a known class")
    public ResponseEntity<?> generateFromClass(@RequestBody Map<String, String> request) {
        String className = request.get("className");

        if ("Person".equalsIgnoreCase(className)) {
            className = "de.x132.objectmerger.model.Person";
        }

        try {
            Class<?> clazz = classLoadingGuard.loadClassSafely(className);
            MergeDefinition definition = MergeDefinitionGenerator.generate(clazz);
            return ResponseEntity.ok(definition);
        } catch (SecurityException securityException) {
            log.warn("Blocked class generation request: {}", securityException.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", securityException.getMessage()));
        } catch (ClassNotFoundException classNotFoundException) {
            return ResponseEntity.badRequest().body(Map.of("error", "Class not found"));
        }
    }

    @PostMapping("/generator/json")
    @Operation(
            summary = "Generate definition from JSON",
            description = "Generates a default merge definition based on the keys of a JSON object")
    public ResponseEntity<?> generateFromJson(@RequestBody Map<String, Object> json) {
        MergeDefinition definition = MergeDefinitionGenerator.generate(json);
        return ResponseEntity.ok(definition);
    }
}
