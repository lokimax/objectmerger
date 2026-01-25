package de.x132.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "objectmerger", mixinStandardHelpOptions = true, version = "0.1.0", description = "Merge multiple JSON sources into a single object using a merge definition.")
public class ObjectMergerCli implements Callable<Integer> {

  @Option(names = { "-d", "--definition" }, required = true, description = "Path to merge definition JSON file")
  private Path definitionPath;

  @Option(names = { "-s",
      "--source" }, required = true, arity = "1..*", description = "Source in format label=path/to.json (repeat for multiple sources)")
  private List<String> sources;

  @Option(names = { "-o", "--output" }, description = "Output JSON file; defaults to stdout")
  private Path outputPath;

  private final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(FieldDefinition.class, new FieldDefinitionDeserializer())
      .create();

  @Override
  public Integer call() throws Exception {
    try {

      // Load merge definition
      MergeDefinition definition;
      try (FileReader reader = new FileReader(definitionPath.toFile())) {
        definition = gson.fromJson(reader, MergeDefinition.class);
      }

      // Rename variable to match ObjectMerger.merge parameter name (optional but
      // cleaner)
      MergeDefinition mergeDefinition = definition;

      // Load sources
      List<LabeledSource<Map<String, Object>>> labeledSources = new ArrayList<>();
      for (String src : sources) {
        int eq = src.indexOf('=');
        if (eq <= 0 || eq == src.length() - 1) {
          throw new IllegalArgumentException("Invalid --source format: " + src);
        }
        String label = src.substring(0, eq);
        Path path = Path.of(src.substring(eq + 1));
        try (FileReader reader = new FileReader(path.toFile())) {
          Map<String, Object> obj = gson.fromJson(reader, Map.class);
          labeledSources.add(new LabeledSource<>(label, obj));
        }
      }

      Map<String, Object> merged = ObjectMerger.merge(
          mergeDefinition,
          labeledSources.toArray(LabeledSource[]::new));

      String jsonOut = gson.toJson(merged);
      if (outputPath != null) {
        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
          writer.write(jsonOut);
        }
      } else {
        System.out.println(jsonOut);
      }
      return 0;

    } catch (JsonSyntaxException | JsonIOException e) {
      System.err.println("Failed to parse JSON: " + e.getMessage());
      return 3;
    } catch (Exception e) {
      System.err.println("Merge failed: " + e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }

  public static void main(String[] args) {
    int exit = new CommandLine(new ObjectMergerCli()).execute(args);
    System.exit(exit);
  }
}
