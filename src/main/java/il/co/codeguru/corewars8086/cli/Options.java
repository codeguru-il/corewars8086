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
      name = "warsPerCombo",
      abbrev = 'w',
      help = "Wars per group combination",
      category = "headless",
      defaultValue = "100"
  )
  public int warsPerCombo;
  
  @Option(
      name = "seed",
      abbrev = 's',
      help = "Starting seed for the game",
      category = "headless",
      defaultValue = "guru"
  )
  public String seed;
}
