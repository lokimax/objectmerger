package de.x132.person;

import com.google.gson.Gson;
import de.x132.LabeledSource;
import de.x132.MergeDefinition;
import de.x132.ObjectMerger;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiSourceMergeTest {

    private static final Gson gson = new Gson();

    @Test
    void testMergePersonData() throws Exception {
        Person analyticsPerson = gson.fromJson(new FileReader("src/test/resources/analytics-person.json"), Person.class);
        Person crmPerson = gson.fromJson(new FileReader("src/test/resources/crm-person.json"), Person.class);
        Person dbPerson = gson.fromJson(new FileReader("src/test/resources/db-person.json"), Person.class);
        MergeDefinition mergeDefinition = gson.fromJson(new FileReader("src/test/resources/person-merge-definition.json"), MergeDefinition.class);

        Person mergedPerson = ObjectMerger.merge(
                Person.class,
                mergeDefinition,
                new LabeledSource<>("analytics", analyticsPerson),
                new LabeledSource<>("crm", crmPerson),
                new LabeledSource<>("database", dbPerson)
        );

        assertEquals("Max Müller", mergedPerson.getName());
        assertEquals(35, mergedPerson.getAge());
        assertEquals("max@example.com", mergedPerson.getEmail());
        assertEquals("030-654321", mergedPerson.getPhone());
    }
}
