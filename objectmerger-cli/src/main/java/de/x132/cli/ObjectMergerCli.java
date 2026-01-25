package de.x132.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "objectmerger",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "ObjectMerger CLI tool",
    subcommands = {MergeCommand.class, GenerateCommand.class})
public class ObjectMergerCli {

  public static void main(String[] args) {
    int exit = new CommandLine(new ObjectMergerCli()).execute(args);
    System.exit(exit);
  }
}
