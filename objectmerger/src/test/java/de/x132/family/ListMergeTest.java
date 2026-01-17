package de.x132.family;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import java.io.FileReader;
import org.junit.jupiter.api.Test;

class ListMergeTest {

  private static final Gson gson = de.x132.testutil.TestGsonHelper.createGson();

  @Test
  void testMergeFamilies() throws Exception {
    Family dbFamily = gson.fromJson(new FileReader("src/test/resources/family/db-family.json"), Family.class);
    Family guiFamily = gson.fromJson(new FileReader("src/test/resources/family/gui-family.json"), Family.class);
    MergeDefinition mergeDefinition = gson.fromJson(
        new FileReader("src/test/resources/family/merge-definition.json"),
        MergeDefinition.class);

    Family mergedFamily = ObjectMerger.merge(
        Family.class,
        mergeDefinition,
        new LabeledSource<>("DB", dbFamily),
        new LabeledSource<>("GUI", guiFamily));

    assertNotNull(mergedFamily);
    assertEquals("Miller Family", mergedFamily.getFamilyName());
    assertEquals(4, mergedFamily.getMembers().size());

    // More detailed assertions can be added here for each member
    System.out.println(gson.toJson(mergedFamily));
  }
}
