package war;

// CORRECTED IMPORTS
import memory.MemoryEventListener;
import memory.RealModeAddress;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReplayRecorder implements CompetitionEventListener, MemoryEventListener {

    private BufferedWriter writer;
    private War currentWar;
    private final Competition competition;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public ReplayRecorder(String filename, Competition competition) throws IOException {
        this.writer = Files.newBufferedWriter(Paths.get(filename));
        this.competition = competition;
    }

    private synchronized void writeEvent(String type, JSONObject payload) {
        if (isClosed.get()) return; // Don't write to a closed stream
        try {
            JSONObject event = new JSONObject();
            event.put("type", type);
            event.put("payload", payload);
            writer.write(event.toString());
            writer.newLine();
        } catch (IOException e) {
            // This might happen in parallel mode, but we've guarded against it.
            // e.printStackTrace(); 
        }
    }

    @Override
    public void onWarStart(long seed) {
        this.currentWar = competition.getCurrentWar();
        
        JSONObject payload = new JSONObject();
        payload.put("seed", seed);
        payload.put("max_rounds", Competition.MAX_ROUND);

        JSONArray warriors = new JSONArray();
        for (int i = 0; i < currentWar.getNumWarriors(); i++) {
            Warrior w = currentWar.getWarrior(i);
            JSONObject warriorJson = new JSONObject();
            warriorJson.put("name", w.getName());
            warriorJson.put("id", i);
            warriorJson.put("load_offset", w.getLoadOffset());
            
            byte[] code = new byte[w.getCodeSize()];
            for (int j = 0; j < w.getCodeSize(); j++) {
                code[j] = currentWar.getMemory().readByte(
                    new RealModeAddress(War.ARENA_SEGMENT, (short)(w.getLoadOffset() + j)));
            }
            warriorJson.put("code", Base64.getEncoder().encodeToString(code));
            warriors.put(warriorJson);
        }
        payload.put("warriors", warriors);
        writeEvent("WAR_START", payload);
    }

    @Override
    public void onMemoryWrite(RealModeAddress address) {
        if (currentWar != null && address.getSegment() == War.ARENA_SEGMENT) {
            JSONObject payload = new JSONObject();
            payload.put("round", currentWar.getCurrentRound());
            payload.put("actorId", currentWar.getCurrentWarrior());
            payload.put("address", address.getLinearAddress());
            byte value = currentWar.getMemory().readByte(address);
            payload.put("value", Byte.toUnsignedInt(value));
            writeEvent("MEMORY_WRITE", payload);
        }
    }
    
    @Override
    public void onRound(int round) {
        if (currentWar == null) return;
        JSONObject payload = new JSONObject();
        payload.put("round", round);
        
        JSONArray warriorStates = new JSONArray();
        for (int i = 0; i < currentWar.getNumWarriors(); i++) {
            if (currentWar.getWarrior(i).isAlive()) {
                JSONObject state = new JSONObject();
                state.put("id", i);
                state.put("ip", new RealModeAddress(
                    currentWar.getWarrior(i).getCpuState().getCS(),
                    currentWar.getWarrior(i).getCpuState().getIP()
                ).getLinearAddress());
                warriorStates.put(state);
            }
        }
        payload.put("warriorStates", warriorStates);
        
        writeEvent("ROUND_UPDATE", payload);
    }
    
    @Override
    public void onWarriorDeath(String warriorName, String reason) {
        if (currentWar == null) return;
        JSONObject payload = new JSONObject();
        payload.put("round", currentWar.getCurrentRound());
        payload.put("warriorName", warriorName);
        payload.put("reason", reason);
        
        for (int i = 0; i < currentWar.getNumWarriors(); i++) {
            if (currentWar.getWarrior(i).getName().equals(warriorName)) {
                payload.put("warrior_id", i);
                break;
            }
        }
        writeEvent("WARRIOR_DEATH", payload);
    }

    @Override
    public void onWarEnd(int reason, String winners) {
        if (currentWar == null) return;
        JSONObject payload = new JSONObject();
        payload.put("round", currentWar.getCurrentRound());
        payload.put("reason", reason);
        payload.put("winners", winners);
        writeEvent("WAR_END", payload);
        
        closeStream();
    }
    
    private void closeStream() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override public void onCompetitionStart() {}
    @Override public void onCompetitionEnd() { closeStream(); }
    @Override public void onWarriorBirth(String warriorName) {}
    @Override public void onEndRound() {}
}