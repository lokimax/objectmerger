package de.x132.family;

import com.google.gson.Gson;
import de.x132.ObjectMerger;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListMergeTest {

    private static final Gson gson = new Gson();

    @Test
    void testMergeFamilies() throws Exception {
        Family dbFamily = gson.fromJson(new FileReader("src/test/resources/db-family.json"), Family.class);
        Family guiFamily = gson.fromJson(new FileReader("src/test/resources/gui-family.json"), Family.class);
        String mergeDefinition = readResourceFile("merge-definition.json");

        Family mergedFamily = ObjectMerger.merge(dbFamily, "DB", guiFamily, "GUI", mergeDefinition, Family.class);
        
        assertNotNull(mergedFamily);
        assertEquals("Miller Family", mergedFamily.getFamilyName());
        assertEquals(3, mergedFamily.getMembers().size());

        // More detailed assertions can be added here for each member
        System.out.println(gson.toJson(mergedFamily));
    }

    private String readResourceFile(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("File not found: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
