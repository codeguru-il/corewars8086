import com.google.devtools.common.options.OptionsParser;
import cli.HeadlessCompetitionRunner;
import cli.Options;
import gui.CompetitionWindow;
import java.io.IOException;

public class CoreWarsEngine {
  // --- THIS IS THE CORRECTED LINE ---
  public static void main(String[] args) throws Exception { 
    OptionsParser optionsParser = OptionsParser.newOptionsParser(Options.class);
    optionsParser.parseAndExitUponError(args);
    Options options = optionsParser.getOptions(Options.class);
  
    if (options != null && options.headless) {
      new HeadlessCompetitionRunner(options);
    } else {
      CompetitionWindow c = new CompetitionWindow(options);
      c.setVisible(true);
      c.pack();
    }
  }
}