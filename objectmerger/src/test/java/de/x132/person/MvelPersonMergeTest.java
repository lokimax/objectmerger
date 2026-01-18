package de.x132.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.testutil.TestGsonHelper;
import java.io.FileReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MvelPersonMergeTest {

    private static final Gson gson = TestGsonHelper.createGson();

    @Test
    @DisplayName("Should merge person data using MVEL rules identically to standard rules")
    void testMergePersonDataWithMvel() throws Exception {
        Person analyticsPerson = gson.fromJson(
                new FileReader("src/test/resources/person/analytics-person.json"), Person.class);
        Person crmPerson = gson.fromJson(new FileReader("src/test/resources/person/crm-person.json"), Person.class);
        Person dbPerson = gson.fromJson(new FileReader("src/test/resources/person/db-person.json"), Person.class);

        // Load the MVEL definition instead of the standard one
        MergeDefinition mergeDefinition = gson.fromJson(
                new FileReader("src/test/resources/person/person-merge-definition-mvel.json"),
                MergeDefinition.class);

        // Perform the merge
        Person mergedPerson = ObjectMerger.merge(
                Person.class,
                mergeDefinition,
                new LabeledSource<>("analytics", analyticsPerson),
                new LabeledSource<>("crm", crmPerson),
                new LabeledSource<>("database", dbPerson));

        // Assert results match the expected output from MultiSourceMergeTest
        assertEquals("Max Müller", mergedPerson.getName());
        assertEquals(35, mergedPerson.getAge());
        assertEquals("max@example.com", mergedPerson.getEmail());
        assertEquals("030-654321", mergedPerson.getPhone());
    }
}
