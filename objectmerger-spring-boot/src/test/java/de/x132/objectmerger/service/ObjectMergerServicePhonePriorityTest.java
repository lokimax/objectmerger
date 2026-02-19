package de.x132.objectmerger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.model.Person;
import de.x132.objectmerger.security.ClassLoadingGuard;
import de.x132.objectmerger.util.MergeDefinitionConverter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectMergerService Phone Priority (CRM-only) Tests")
class ObjectMergerServicePhonePriorityTest {

    private final ObjectMergerService mergerService = createService();

    private static ObjectMergerService createService() {
        ClassLoadingGuard guard = new ClassLoadingGuard();
        guard.setAllowedPackages(java.util.List.of("de.x132.objectmerger.model."));
        return new ObjectMergerService(guard);
    }

    private MergeDefinition crmOnlyPhoneDefinition() {
        Map<String, Map<String, Object>> defMap = new LinkedHashMap<>();
        Map<String, Object> phoneDef = new LinkedHashMap<>();
        phoneDef.put("strategy", "priority");
        phoneDef.put("priority", Map.of("crm", 1));
        phoneDef.put("defaultValue", null);
        defMap.put("phone", phoneDef);
        return MergeDefinitionConverter.fromMap(defMap);
    }

    @Test
    @DisplayName("phone uses CRM when present")
    void phone_uses_crm_when_present() throws ClassNotFoundException {
        // Given
        Person analytics = new Person();
        analytics.setPhone("030-654321");

        Person crm = new Person();
        crm.setPhone("030-123456");

        Person database = new Person();
        database.setPhone(null);

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("analytics", analytics),
                        new LabeledSource<>("crm", crm),
                        new LabeledSource<>("database", database));

        // When
        Object result =
                mergerService.merge(
                        "de.x132.objectmerger.model.Person", crmOnlyPhoneDefinition(), sources);

        // Then
        assertNotNull(result);
        Person merged = (Person) result;
        assertEquals("030-123456", merged.getPhone());
    }

    @Test
    @DisplayName("phone is null when CRM null even if others have value")
    void phone_is_null_when_crm_null_even_if_others_have_value() throws ClassNotFoundException {
        // Given
        Person analytics = new Person();
        analytics.setPhone("030-654321");

        Person crm = new Person();
        crm.setPhone(null);

        Person database = new Person();
        database.setPhone("030-000000");

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("analytics", analytics),
                        new LabeledSource<>("crm", crm),
                        new LabeledSource<>("database", database));

        // When
        Object result =
                mergerService.merge(
                        "de.x132.objectmerger.model.Person", crmOnlyPhoneDefinition(), sources);

        // Then
        assertNotNull(result);
        Person merged = (Person) result;
        assertNull(merged.getPhone());
    }

    @Test
    @DisplayName("phone is default when CRM source missing")
    void phone_is_default_when_crm_source_missing() throws ClassNotFoundException {
        // Given
        Person analytics = new Person();
        analytics.setPhone("030-654321");

        Person database = new Person();
        database.setPhone("030-000000");

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("analytics", analytics),
                        new LabeledSource<>("database", database));

        // When
        Object result =
                mergerService.merge(
                        "de.x132.objectmerger.model.Person", crmOnlyPhoneDefinition(), sources);

        // Then
        assertNotNull(result);
        Person merged = (Person) result;
        assertNull(merged.getPhone());
    }
}
