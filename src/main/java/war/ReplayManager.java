package war;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplayManager {
    private final Map<Integer, List<ReplayEvent>> eventsByRound = new HashMap<>();
    private JSONObject warStartEvent;
    private int totalRounds = 0;

    public void load(String filename) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
            String line;
            // First line is always the header
            line = reader.readLine();
            if (line != null) {
                warStartEvent = new JSONObject(line);
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                JSONObject json = new JSONObject(line);
                ReplayEvent event = new ReplayEvent(json);
                eventsByRound.computeIfAbsent(event.round, k -> new ArrayList<>()).add(event);

                if ("WAR_END".equals(event.type)) {
                    totalRounds = event.round;
                }
            }
        }
        if (totalRounds == 0 && warStartEvent != null) {
            totalRounds = warStartEvent.optInt("max_rounds", 200000);
        }
    }

    public JSONObject getWarStartData() {
        return warStartEvent;
    }

    public List<ReplayEvent> getEventsInRound(int round) {
        return eventsByRound.getOrDefault(round, new ArrayList<>());
    }

    public int getTotalRounds() {
        return totalRounds;
    }
}