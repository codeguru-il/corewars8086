package war;

import memory.MemoryEventListener;
import memory.RealModeAddress;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebSocketForwarder implements CompetitionEventListener, MemoryEventListener {

    private final WarEventSocketServer server;
    private final Competition competition;

    public WebSocketForwarder(WarEventSocketServer server, Competition competition) {
        this.server = server;
        this.competition = competition;
    }

    private void broadcast(String type, JSONObject payload) {
        JSONObject event = new JSONObject();
        event.put("type", type);
        event.put("payload", payload);
        server.broadcastEvent(event);
    }

    @Override
    public void onWarStart(long seed) {
        War currentWar = competition.getCurrentWar();
        JSONObject payload = new JSONObject();
        
        JSONArray warriors = new JSONArray();
        for (int i = 0; i < currentWar.getNumWarriors(); i++) {
            Warrior w = currentWar.getWarrior(i);
            JSONObject warriorJson = new JSONObject();
            warriorJson.put("name", w.getName());
            warriorJson.put("id", i);
            warriors.put(warriorJson);
        }
        payload.put("warriors", warriors);
        broadcast("WAR_START", payload);
    }

    @Override
    public void onRound(int round) {
        War currentWar = competition.getCurrentWar();
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
        broadcast("ROUND_UPDATE", payload);
    }
    
    @Override
    public void onMemoryWrite(RealModeAddress address) {
        if (address.getSegment() == War.ARENA_SEGMENT) {
            War currentWar = competition.getCurrentWar();
            if (currentWar == null) return;
            
            JSONObject payload = new JSONObject();
            payload.put("address", address.getLinearAddress());
            // We need to read the value back from memory
            byte value = currentWar.getMemory().readByte(address);
            payload.put("value", Byte.toUnsignedInt(value));
            payload.put("actorId", competition.getCurrentWarrior());
            broadcast("MEMORY_WRITE", payload);
        }
    }
    
    @Override
    public void onWarriorDeath(String warriorName, String reason) {
        War currentWar = competition.getCurrentWar();
        if (currentWar == null) return;

        JSONObject payload = new JSONObject();
        payload.put("warriorName", warriorName);
        payload.put("reason", reason);
        
        for (int i = 0; i < currentWar.getNumWarriors(); i++) {
            if (currentWar.getWarrior(i).getName().equals(warriorName)) {
                payload.put("warrior_id", i);
                break;
            }
        }
        broadcast("WARRIOR_DEATH", payload);
    }
    
    @Override
    public void onWarEnd(int reason, String winners) {
        JSONObject payload = new JSONObject();
        payload.put("reason", reason);
        payload.put("winners", winners);
        broadcast("WAR_END", payload);
    }

    // Unused but required by interfaces
    @Override public void onEndRound() {}
    @Override public void onWarriorBirth(String warriorName) {}
    @Override public void onCompetitionStart() {}
    @Override public void onCompetitionEnd() {}
}