package il.co.codeguru.corewars8086.cli;

import com.google.common.primitives.Longs;
import il.co.codeguru.corewars8086.war.Competition;
import il.co.codeguru.corewars8086.war.CompetitionEventListener;
import il.co.codeguru.corewars8086.war.ScoreEventListener;
import il.co.codeguru.corewars8086.war.WarriorRepository;

import java.io.IOException;

public class HeadlessCompetitionRunner implements ScoreEventListener, CompetitionEventListener {
  private final Options options;
  
  private int warCounter;
  private int totalWars;
  
  private final Competition competition;
  private long seed;
  
  private Thread warThread;
  
  public HeadlessCompetitionRunner(Options options) throws IOException {
    this.options = options;
    this.competition = new Competition(options);
    this.competition.addCompetitionEventListener(this);
    WarriorRepository repository = competition.getWarriorRepository();
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
    if (warCounter % 10 == 0) {
      System.out.printf("Wars so far: (%d out of %d)%n", warCounter, totalWars);
    }
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
    System.out.println("Starting competition.");
  }
  
  @Override
  public void onCompetitionEnd() {
    System.out.println("Competition is over.");
    warThread = null;
  }
  
  @Override
  public void onEndRound() {
  
  }
  
  @Override
  public void scoreChanged(String name, float addedValue, int groupIndex, int subIndex) {
  }
}
