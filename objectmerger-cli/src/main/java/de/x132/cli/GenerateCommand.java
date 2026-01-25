package de.x132.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.generator.MergeDefinitionGenerator;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "generate", description = "Generate a merge definition from a sample JSON file")
public class GenerateCommand implements Callable<Integer> {

  @Parameters(index = "0", description = "Path to sample JSON file")
  private Path samplePath;

  @Option(
      names = {"-o", "--output"},
      description = "Output JSON file; defaults to stdout")
  private Path outputPath;

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  public Integer call() throws Exception {
    try {
      Map<String, Object> sampleData;
      try (FileReader reader = new FileReader(samplePath.toFile())) {
        sampleData = gson.fromJson(reader, Map.class);
      }

      MergeDefinition definition = MergeDefinitionGenerator.generate(sampleData);

      String jsonOut = gson.toJson(definition);
      if (outputPath != null) {
        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
          writer.write(jsonOut);
        }
      } else {
        System.out.println(jsonOut);
      }
      return 0;

    } catch (Exception e) {
      System.err.println("Generation failed: " + e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }
}
