package il.co.codeguru.corewars8086;

import com.google.devtools.common.options.OptionsParser;
import il.co.codeguru.corewars8086.cli.HeadlessCompetitionRunner;
import il.co.codeguru.corewars8086.cli.Options;
import il.co.codeguru.corewars8086.gui.CompetitionWindow;

import java.io.IOException;

public class CoreWarsEngine {
  public static void main(String[] args) throws IOException {
    OptionsParser optionsParser = OptionsParser.newOptionsParser(Options.class);
    optionsParser.parseAndExitUponError(args);
    Options options = optionsParser.getOptions(Options.class);
  
    if (options.headless) {
      HeadlessCompetitionRunner r = new HeadlessCompetitionRunner(options);
    } else {
      CompetitionWindow c = new CompetitionWindow(options);
      c.setVisible(true);
      c.pack();
    }
  }
}