package war;

import java.util.EventListener;

/**
 * Interface for War events' listeners (implemented by the UI).
 * 
 * @author DL
 */
public interface CompetitionEventListener extends EventListener  {
	
    /**
     * Called when a new War is started.
     * @param seed  Seed for the war's RNG features.
     */
    void onWarStart(long seed);

    /** Possible reasons for a war session to end. */
    public static final int SINGLE_WINNER = 0;
    public static final int MAX_ROUND_REACHED = 1;
    public static final int ABORTED = 2;

    /**
     * Called when a War ends.
     * @param reason   One of the above reasons.
     * @param winners  Winning warrior(s) name(s).
     */
    void onWarEnd(int reason, String winners);	

    /**
     * Called when a new round is started.
     * @param round   0-based round number.
     */
    void onRound(int round);

    /**
     * Called when a new warrior enters the arena.
     * @param warriorName  Warrior's name.
     */
    void onWarriorBirth(String warriorName);

    /**
     * Called when a warrior dies.
     * @param warriorName  Warrior's name.
     * @param reason       Reason for death.
     */
    void onWarriorDeath(String warriorName, String reason);

    void onCompetitionStart();

    void onCompetitionEnd();
    
    void onEndRound();
}
