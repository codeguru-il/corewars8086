package il.co.codeguru.corewars8086.cli;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public class Options extends OptionsBase {
  @Option(
      name = "headless",
      abbrev = 'h',
      help = "Run the engine in headless mode",
      category = "startup",
      defaultValue = "false"
  )
  public boolean headless;
  
  @Option(
      name = "comboSize",
      abbrev = 'c',
      help = "The size of each group combination",
      category = "headless",
      defaultValue = "4"
  )
  public int combinationSize;
  
  @Option(
      name = "battlesPerCombo",
      abbrev = 'b',
      help = "Battles per group combination",
      category = "headless",
      defaultValue = "100"
  )
  public int battlesPerCombo;
  
  @Option(
      name = "seed",
      abbrev = 's',
      help = "Starting seed for the game",
      category = "headless",
      defaultValue = "guru"
  )
  public String seed;
  
  @Option(
      name = "warriorsDir",
      abbrev = 'w',
      help = "Directory for warrior files",
      category = "data",
      defaultValue = "warriors"
  )
  public String warriorsDir;
  
  @Option(
      name = "zombiesDir",
      abbrev = 'z',
      help = "Directory for zombie files",
      category = "data",
      defaultValue = "zombies"
  )
  public String zombiesDir;
  
  @Option(
      name = "outputFile",
      abbrev = 'o',
      help = "Path to scores output file",
      category = "data",
      defaultValue = "scores.csv"
  )
  public String outputFile;
}
