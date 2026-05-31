package de.x132.objectmerger.service;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.util.MergeDefinitionConverter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Implementation of ExampleMergeService.
 *
 * <p>Encapsulates mock data preparation and merges it using the core merger service. Keeping this
 * logic here satisfies the Single Responsibility Principle by decoupling controller endpoints from
 * mock data structures.
 */
@Service
public class ExampleMergeServiceImpl implements ExampleMergeService {

    private final ObjectMergerService mergerService;

    /**
     * Constructs a new ExampleMergeServiceImpl.
     *
     * @param mergerService The underlying ObjectMergerService to delegate merge logic to.
     */
    public ExampleMergeServiceImpl(ObjectMergerService mergerService) {
        this.mergerService = mergerService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object mergePersonExample() throws Exception {
        Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();

        // Priority rules for 'name'
        defMap.put(
                "name",
                Map.of(
                        "strategy",
                        "priority",
                        "priority",
                        Map.of("database", 1, "crm", 2, "analytics", 3)));

        // Maximum rule for 'age'
        defMap.put("age", Map.of("strategy", "maximum", "defaultValue", 0));

        // Priority rules for 'email'
        defMap.put(
                "email",
                Map.of(
                        "strategy",
                        "priority",
                        "priority",
                        Map.of("database", 1, "crm", 2, "analytics", 3)));

        // Priority rules for 'phone'
        defMap.put(
                "phone",
                Map.of(
                        "strategy",
                        "priority",
                        "priority",
                        Map.of("analytics", 1, "crm", 2, "database", 3)));

        MergeDefinition definition = MergeDefinitionConverter.fromMap(defMap);

        // Database source mock data
        Map<String, Object> dbData = new LinkedHashMap<>();
        dbData.put("name", "Max Müller");
        dbData.put("age", 30);
        dbData.put("email", "max@example.com");
        dbData.put("phone", null);

        // CRM source mock data
        Map<String, Object> crmData = new LinkedHashMap<>();
        crmData.put("name", "Maximilian Müller");
        crmData.put("age", 25);
        crmData.put("email", null);
        crmData.put("phone", "030-123456");

        // Analytics source mock data
        Map<String, Object> analyticsData = new LinkedHashMap<>();
        analyticsData.put("name", null);
        analyticsData.put("age", 35);
        analyticsData.put("email", "max.mueller@example.de");
        analyticsData.put("phone", "030-654321");

        List<LabeledSource<?>> sources =
                (List)
                        List.of(
                                new LabeledSource<>("database", dbData),
                                new LabeledSource<>("crm", crmData),
                                new LabeledSource<>("analytics", analyticsData));

        return mergerService.merge("de.x132.objectmerger.model.Person", definition, sources);
    }
}
