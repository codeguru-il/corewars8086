package il.co.codeguru.corewars8086.cli;

import com.google.common.primitives.Longs;
import il.co.codeguru.corewars8086.war.Competition;
import il.co.codeguru.corewars8086.war.CompetitionEventListener;
import il.co.codeguru.corewars8086.war.ScoreEventListener;
import il.co.codeguru.corewars8086.war.WarriorRepository;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author RM
 */
public class HeadlessCompetitionRunner implements ScoreEventListener, CompetitionEventListener {
  private final Options options;
  
  private int warCounter;
  private int totalWars;
  
  private final Competition competition;
  private long seed;
  
  private Thread warThread;
  
  private ProgressBar progressBar;
  
  public HeadlessCompetitionRunner(Options options) throws IOException {
    this.options = options;
    System.out.println("CoreWars8086 - headless mode\n");
    
    this.competition = new Competition(options);
    this.competition.addCompetitionEventListener(this);
    WarriorRepository repository = competition.getWarriorRepository();
    System.out.printf("Loaded warriors: %s%n", Arrays.toString(repository.getGroupNames()));
    repository.addScoreEventListener(this);
  
    if (Longs.tryParse(options.seed) != null) this.seed = Long.parseLong(options.seed);
    else this.seed = options.seed.hashCode();
    
    this.warCounter = 0;
    this.totalWars = 0;
    this.runWar();
  }
  
  public boolean runWar() {
    try {
      competition.setSeed(this.seed);
      if (competition.getWarriorRepository().getNumberOfGroups() < options.combinationSize) {
        System.err.printf("Not enough survivors (got %d but %d are needed)%n", competition.getWarriorRepository().getNumberOfGroups(), options.combinationSize);
        return false;
      }
      
      warThread = new Thread("CompetitionThread") {
        @Override
        public void run() {
          try {
            if (options.parallel) {
              competition.runCompetitionInParallel(options.battlesPerCombo, options.combinationSize, options.threads);
            } else {
              competition.runCompetition(options.battlesPerCombo, options.combinationSize, false);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      
      warThread.start();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  @Override
  public void onWarStart() {
  
  }
  
  @Override
  public void onWarEnd(int reason, String winners) {
    warCounter++;
    progressBar.stepTo(warCounter);
  }
  
  @Override
  public void onRound(int round) {
  
  }
  
  @Override
  public void onWarriorBirth(String warriorName) {
  
  }
  
  @Override
  public void onWarriorDeath(String warriorName, String reason) {
  
  }
  
  @Override
  public void onCompetitionStart() {
    warCounter = 0;
    totalWars = competition.getTotalNumberOfWars();
    competition.setAbort(false);
    System.out.printf("Starting competition (%d wars)%s.%n", totalWars, options.parallel ? " in parallel" : "");
    progressBar = new ProgressBarBuilder()
        .setTaskName("Running wars")
        .setStyle(ProgressBarStyle.ASCII)
        .setInitialMax(totalWars)
        .showSpeed()
        .build();
  }
  
  @Override
  public void onCompetitionEnd() {
    progressBar.close();
    System.out.printf("Competition is over. Ran %d wars%n", warCounter);
    warThread = null;
  }
  
  @Override
  public void onEndRound() {
  
  }
  
  @Override
  public void scoreChanged(String name, float addedValue, int groupIndex, int subIndex) {
  }
}
