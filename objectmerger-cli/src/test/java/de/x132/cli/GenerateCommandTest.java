package de.x132.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class GenerateCommandTest {

  private Path tempDir;
  private Path sampleFile;
  private Path outputFile;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = Files.createTempDirectory("objectmerger-cli-test");
    sampleFile = tempDir.resolve("sample.json");
    outputFile = tempDir.resolve("output.json");

    try (FileWriter writer = new FileWriter(sampleFile.toFile())) {
      writer.write("{\"name\": \"test\", \"value\": 123}");
    }
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.walk(tempDir).sorted((a, b) -> b.compareTo(a)).map(Path::toFile).forEach(File::delete);
  }

  @Test
  void testGenerate() {
    GenerateCommand cmd = new GenerateCommand();
    CommandLine commandLine = new CommandLine(cmd);

    int exitCode = commandLine.execute(sampleFile.toString(), "-o", outputFile.toString());

    assertEquals(0, exitCode);
    assertTrue(Files.exists(outputFile));

    try {
      String content = Files.readString(outputFile);
      Gson gson = new Gson();
      Map map = gson.fromJson(content, Map.class);
      assertTrue(((Map) map.get("definitions")).containsKey("name"));
      assertTrue(((Map) map.get("definitions")).containsKey("value"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
