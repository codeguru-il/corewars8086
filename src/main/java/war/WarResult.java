package war;

import java.util.Arrays;

public class WarResult implements Comparable<WarResult> {
    private final int id;
    private final long seed;
    private final String[] winningTeamNames;
    private final WarriorGroup[] participatingGroups;
    
    // --- NEW: Metrics for Interest Score ---
    private final int kills;
    private final int closeCalls;
    private final int zombieTilesOverwritten;
    private final int playerTilesOverwritten;
    private final int totalCycles;
    private final float interestScore;

    public WarResult(int id, long seed, String[] winningTeamNames, WarriorGroup[] participatingGroups,
                     int kills, int closeCalls, int zombieTilesOverwritten, int playerTilesOverwritten, int totalCycles) {
        this.id = id;
        this.seed = seed;
        this.winningTeamNames = winningTeamNames;
        this.participatingGroups = participatingGroups;
        this.kills = kills;
        this.closeCalls = closeCalls;
        this.zombieTilesOverwritten = zombieTilesOverwritten;
        this.playerTilesOverwritten = playerTilesOverwritten;
        this.totalCycles = totalCycles;

        // --- The Interest Score Formula ---
        this.interestScore = (kills * 25.0f) +
                             (closeCalls * 15.0f) +
                             (zombieTilesOverwritten * 5.0f) +
                             (playerTilesOverwritten * 1.0f) +
                             (totalCycles * 0.01f);
    }

    public int getId() { return id; }
    public long getSeed() { return seed; }
    public WarriorGroup[] getParticipatingGroups() { return participatingGroups; }
    public float getInterestScore() { return interestScore; }

    // --- UPDATED: Sort by Interest Score ---
    @Override
    public int compareTo(WarResult other) {
        return Float.compare(other.interestScore, this.interestScore);
    }
    
    // Getters for JSON serialization
    public String[] getWinningTeamNames() { return winningTeamNames; }
    public int getKills() { return kills; }
    public int getCloseCalls() { return closeCalls; }
    public int getZombieTilesOverwritten() { return zombieTilesOverwritten; }
    public int getPlayerTilesOverwritten() { return playerTilesOverwritten; }
    public int getTotalCycles() { return totalCycles; }
}