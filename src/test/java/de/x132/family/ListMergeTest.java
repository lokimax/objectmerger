package de.x132.family;

import com.google.gson.Gson;
import de.x132.LabeledSource;
import de.x132.MergeDefinition;
import de.x132.ObjectMerger;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListMergeTest {

    private static final Gson gson = new Gson();

    @Test
    void testMergeFamilies() throws Exception {
        Family dbFamily = gson.fromJson(new FileReader("src/test/resources/db-family.json"), Family.class);
        Family guiFamily = gson.fromJson(new FileReader("src/test/resources/gui-family.json"), Family.class);
        MergeDefinition mergeDefinition = gson.fromJson(new FileReader("src/test/resources/merge-definition.json"), MergeDefinition.class);

        Family mergedFamily = ObjectMerger.merge(
                Family.class,
                mergeDefinition,
                new LabeledSource<>("DB", dbFamily),
                new LabeledSource<>("GUI", guiFamily)
        );
        
        assertNotNull(mergedFamily);
        assertEquals("Miller Family", mergedFamily.getFamilyName());
        assertEquals(4, mergedFamily.getMembers().size());

        // More detailed assertions can be added here for each member
        System.out.println(gson.toJson(mergedFamily));
    }
}
