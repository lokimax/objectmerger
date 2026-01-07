package de.x132;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObjectMergerTest {

    private static final Gson gson = new Gson();

    @Test
    void testMergeFamilies() throws Exception {
        String dbObjectJson = readResourceFile("db-family.json");
        String guiObjectJson = readResourceFile("gui-family.json");
        String mergeDefinition = readResourceFile("merge-definition.json");

        String mergedJson = ObjectMerger.merge(dbObjectJson, "DB", guiObjectJson, "GUI", mergeDefinition);
        Family mergedFamily = gson.fromJson(mergedJson, Family.class);
        
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
