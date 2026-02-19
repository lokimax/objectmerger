package de.x132.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.testutil.MvelTestGsonHelper;
import java.io.FileReader;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapPersonMergeTest {

    private static final Gson gson = MvelTestGsonHelper.createGson();

    @Test
    @DisplayName("Should merge person data using MVEL rules identically to standard rules")
    void testMergePersonDataWithMvel() throws Exception {
        Map<String, Object> analyticsPerson =
                gson.fromJson(
                        new FileReader("src/test/resources/person/analytics-person.json"),
                        Map.class);
        Map<String, Object> crmPerson =
                gson.fromJson(
                        new FileReader("src/test/resources/person/crm-person.json"), Map.class);
        Map<String, Object> dbPerson =
                gson.fromJson(
                        new FileReader("src/test/resources/person/db-person.json"), Map.class);

        // Load the MVEL definition instead of the standard one
        MergeDefinition mergeDefinition =
                gson.fromJson(
                        new FileReader(
                                "src/test/resources/person/person-merge-definition-mvel.json"),
                        MergeDefinition.class);

        // Perform the merge
        Map<String, Object> mergedPerson =
                ObjectMerger.merge(
                        mergeDefinition,
                        new LabeledSource<>("analytics", analyticsPerson),
                        new LabeledSource<>("crm", crmPerson),
                        new LabeledSource<>("database", dbPerson));

        // Assert results match the expected output from MultiSourceMergeTest
        assertEquals("Max Müller", mergedPerson.get("name"));
        assertEquals(35, ((Number) mergedPerson.get("age")).intValue());
        assertEquals("max@example.com", mergedPerson.get("email"));
        assertEquals("030-654321", mergedPerson.get("phone"));
    }
}
