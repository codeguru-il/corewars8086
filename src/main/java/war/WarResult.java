package war;

import java.util.Arrays;

public class WarResult implements Comparable<WarResult> {
    private final int id;
    private final long seed;
    private final String[] winningTeamNames;
    private final float score;
    private final WarriorGroup[] participatingGroups;

    public WarResult(int id, long seed, String[] winningTeamNames, float score, WarriorGroup[] participatingGroups) {
        this.id = id;
        this.seed = seed;
        this.winningTeamNames = winningTeamNames;
        this.score = score;
        this.participatingGroups = participatingGroups;
    }

    public int getId() { return id; }
    public long getSeed() { return seed; }
    public String[] getWinningTeamNames() { return winningTeamNames; }
    public float getScore() { return score; }
    public WarriorGroup[] getParticipatingGroups() { return participatingGroups; }

    @Override
    public int compareTo(WarResult other) {
        // Sort in descending order of score
        return Float.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return "WarResult{" +
                "id=" + id +
                ", seed=" + seed +
                ", winners=" + Arrays.toString(winningTeamNames) +
                ", score=" + score +
                '}';
    }
}