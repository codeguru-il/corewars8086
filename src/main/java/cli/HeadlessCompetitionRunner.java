package cli;

import com.google.common.primitives.Longs;
import war.Competition;
import war.CompetitionEventListener;
import war.ScoreEventListener;
import war.WarriorRepository;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
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
  private BufferedWriter replayWriter;

  private Path replayPath;
  
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
    // Open a JSONL replay file for this headless run (timestamped)
    try {
      String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
      String replayFileName = "replay-" + ts + ".jsonl";
      this.replayPath = Paths.get(replayFileName);
      this.replayWriter = Files.newBufferedWriter(replayPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    } catch (IOException e) {
      System.err.println("Failed to open replay JSONL file: " + e.getMessage());
      this.replayWriter = null;
    }

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
  public void onWarStart(long seed) {
  
  }
  
  @Override
  public void onWarEnd(int reason, String winners) {
    warCounter++;
    progressBar.stepTo(warCounter);
    // write a war-end event to the replay JSONL
  writeReplayJson(new String[] {
    "{\"type\":\"WAR_END\",\"round\":" + warCounter + ",\"reason\":\"" + reason + "\",\"winners\":\"" + winners + "\"}"
  });
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
    // write competition start meta
  writeReplayJson(new String[] {
    "{\"type\":\"COMPETITION_START\",\"total_wars\":" + totalWars + ",\"timestamp\":\"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()) + "\"}"
  });
  }
  
  @Override
  public void onCompetitionEnd() {
    progressBar.close();
    System.out.printf("Competition is over. Ran %d wars%n", warCounter);
    warThread = null;
    // write competition end meta and close the replay writer
  writeReplayJson(new String[] {
    "{\"type\":\"COMPETITION_END\",\"ran_rounds\":" + warCounter + "}"
  });
    if (this.replayWriter != null) {
      try {
        this.replayWriter.flush();
        this.replayWriter.close();
        System.out.println("Replay JSONL written to: " + this.replayPath.toAbsolutePath().toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  @Override
  public void onEndRound() {
  
  }
  
  @Override
  public void scoreChanged(String name, float addedValue, int groupIndex, int subIndex) {
  }

  private synchronized void writeReplayJson(String[] lines) {
    if (this.replayWriter == null) return;
    try {
      for (String l : lines) {
        this.replayWriter.write(l);
        this.replayWriter.newLine();
      }
      this.replayWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
