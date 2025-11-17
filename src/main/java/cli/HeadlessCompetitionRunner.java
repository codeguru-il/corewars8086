package cli;

import com.google.common.primitives.Longs;
import war.*;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HeadlessCompetitionRunner implements ScoreEventListener, CompetitionEventListener {
    private final Options options;
    private int warCounter;
    private final Competition competition;
    private ProgressBar progressBar;
    private static final String REPLAYS_DIR = "replays";
    private final String timestamp;

    public HeadlessCompetitionRunner(Options options) throws Exception {
        this.options = options;
        System.out.println("CodeGuru - headless mode\n");
        
        // --- Generate timestamp once for the entire run ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy_HHmmss", Locale.ENGLISH);
        this.timestamp = LocalDateTime.now().format(formatter).toUpperCase();

        Path replaysPath = Paths.get(REPLAYS_DIR);
        if (!Files.exists(replaysPath)) {
            Files.createDirectories(replaysPath);
            System.out.println("Created output directory: " + REPLAYS_DIR);
        }
        
        this.competition = new Competition(options);
        this.competition.addCompetitionEventListener(this);
        WarriorRepository repository = competition.getWarriorRepository();
        System.out.printf("Loaded warriors: %s%n", Arrays.toString(repository.getGroupNames()));
        repository.addScoreEventListener(this);

        long seed = (Longs.tryParse(options.seed) != null) ? Long.parseLong(options.seed) : options.seed.hashCode();
        competition.setSeed(seed);

        System.out.println("\n--- Phase 1: Running Scoring Competition ---");
        runFullCompetition();

        // --- Always save scores to the timestamped JSON file ---
        saveScoresToJson();

        if (options.replays > 0) {
            System.out.println("\n--- Phase 2: Generating Replays for Top Warriors ---");
            generateTopReplays();
        }
        
        System.out.println("\nRun complete.");
    }

    private void runFullCompetition() throws InterruptedException {
        competition.runCompetitionInParallel(options.battlesPerCombo, options.combinationSize, options.threads);
    }
    
    private void saveScoresToJson() throws IOException {
        String filename = String.format("all_wars_%s.json", this.timestamp);
        Path outputPath = Paths.get(REPLAYS_DIR, filename);
        
        System.out.printf("Saving all %d war results to %s...%n", competition.getWarResults().size(), outputPath);
        JSONArray resultsArray = new JSONArray();
        for (WarResult result : competition.getWarResults()) {
            JSONObject resultJson = new JSONObject();
            resultJson.put("id", result.getId());
            resultJson.put("seed", result.getSeed());
            resultJson.put("score", result.getScore());
            resultJson.put("winners", new JSONArray(Arrays.asList(result.getWinningTeamNames())));

            JSONArray groupsJson = new JSONArray();
            for (WarriorGroup group : result.getParticipatingGroups()) {
                groupsJson.put(group.getName());
            }
            resultJson.put("groups", groupsJson);
            resultsArray.put(resultJson);
        }
        
        try (FileWriter file = new FileWriter(outputPath.toFile())) {
            file.write(resultsArray.toString(4));
        }
    }

    private void generateTopReplays() throws Exception {
        List<WarResult> results = new ArrayList<>(competition.getWarResults());
        Collections.sort(results);

        int count = Math.min(options.replays, results.size());
        System.out.printf("Found %d war results. Generating replays for the top %d highest-scoring wars.%n", results.size(), count);

        for (int i = 0; i < count; i++) {
            WarResult topResult = results.get(i);
            // --- UPDATED: Filename now includes the timestamp ---
            String replayFilename = String.format("replay_%d_%s.jsonl", topResult.getId(), this.timestamp);
            Path replayPath = Paths.get(REPLAYS_DIR, replayFilename);
            
            competition.rerunWarForReplay(topResult, replayPath.toString());
        }
    }

    @Override
    public void onCompetitionStart() {
        warCounter = 0;
        progressBar = new ProgressBarBuilder()
            .setTaskName("Running battles")
            .setStyle(ProgressBarStyle.ASCII)
            .setInitialMax(competition.getTotalNumberOfWars())
            .build();
    }
    
    @Override
    public void onWarEnd(int reason, String winners) {
        warCounter++;
        if (progressBar != null) progressBar.step();
    }
    
    @Override
    public void onCompetitionEnd() {
        if (progressBar != null) progressBar.close();
        System.out.printf("Scoring competition is over. Ran %d battles.%n", warCounter);
    }
    
    @Override public void onRound(int round) {}
    @Override public void onWarriorBirth(String warriorName) {}
    @Override public void onWarriorDeath(String warriorName, String reason) {}
    @Override public void onEndRound() {}
    @Override public void onWarStart(long seed) {}
    @Override public void scoreChanged(String name, float addedValue, int groupIndex, int subIndex) {}
}