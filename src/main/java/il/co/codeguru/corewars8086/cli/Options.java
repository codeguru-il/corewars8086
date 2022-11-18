package il.co.codeguru.corewars8086.cli;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

/**
 * @author RM
 */
public class Options extends OptionsBase {
  @Option(
      name = "headless",
      abbrev = 'h',
      help = "Run the engine in headless mode",
      category = "Startup",
      defaultValue = "false"
  )
  public boolean headless;
  
  @Option(
      name = "comboSize",
      abbrev = 'c',
      help = "The size of each group combination",
      category = "Headless",
      defaultValue = "4"
  )
  public int combinationSize;
  
  @Option(
      name = "battlesPerCombo",
      abbrev = 'b',
      help = "Battles per group combination",
      category = "Headless",
      defaultValue = "100"
  )
  public int battlesPerCombo;
  
  @Option(
      name = "seed",
      abbrev = 's',
      help = "Starting seed for the game",
      category = "Headless",
      defaultValue = "guru"
  )
  public String seed;
  
  @Option(
      name = "parallel",
      abbrev = 'p',
      help = "Run multiple battles concurrently (only in headless mode)",
      category = "Headless",
      defaultValue = "true"
  )
  public boolean parallel;
  
  @Option(
      name = "threads",
      abbrev = 't',
      help = "Number of threads for parallel mode",
      category = "Headless",
      defaultValue = "4"
  )
  public int threads;
  
  @Option(
      name = "warriorsDir",
      abbrev = 'w',
      help = "Directory for warrior files",
      category = "Data",
      defaultValue = "survivors"
  )
  public String warriorsDir;
  
  @Option(
      name = "zombiesDir",
      abbrev = 'z',
      help = "Directory for zombie files",
      category = "Data",
      defaultValue = "zombies"
  )
  public String zombiesDir;
  
  @Option(
      name = "outputFile",
      abbrev = 'o',
      help = "Path to scores output file",
      category = "Data",
      defaultValue = "scores.csv"
  )
  public String outputFile;
}
