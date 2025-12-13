package il.co.codeguru.corewars8086.gui.effects;

import il.co.codeguru.corewars8086.cpu.CpuState;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Unsigned;
import il.co.codeguru.corewars8086.war.War;
import il.co.codeguru.corewars8086.war.Warrior;
import il.co.codeguru.corewars8086.war.WarriorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects special game events by tracking warrior code regions and memory writes.
 */
public class GameEventDetector {
    
    public enum EventType {
        BOMB_USED,
        ZOMBIE_CAPTURED,
        WARRIOR_DEATH
    }
    
    public static class GameEvent {
        public final EventType type;
        public final int arenaOffset; // Offset within arena (0 to ARENA_SIZE-1)
        public final int warriorIndex; // Index of warrior involved (-1 if not applicable)
        public final String warriorName; // Name of warrior involved
        public final String message; // Additional message
        public final int captorIndex; // Index of capturing warrior (for ZOMBIE_CAPTURED)
        
        public GameEvent(EventType type, int arenaOffset, int warriorIndex, String warriorName, String message) {
            this(type, arenaOffset, warriorIndex, warriorName, message, -1);
        }
        
        public GameEvent(EventType type, int arenaOffset, int warriorIndex, String warriorName, String message, int captorIndex) {
            this.type = type;
            this.arenaOffset = arenaOffset;
            this.warriorIndex = warriorIndex;
            this.warriorName = warriorName;
            this.message = message;
            this.captorIndex = captorIndex;
        }
    }
    
    private final War war;
    private final List<WarriorRegion> warriorRegions;
    
    // Bomb detection: track writes per warrior per round (not time-based!)
    // A bomb (like INT 0x86) performs many writes in a single round, normal execution does few
    private int[] writesPerWarriorThisRound; // Write count per warrior in current round
    private static final int BOMB_WRITE_THRESHOLD = 50; // INT 0x86 writes ~64 times via REP STOSD
    private static final int BOMB_DETECTION_GRACE_ROUNDS = 50; // Don't detect bombs in first N rounds (warriors may initialize memory)
    
    // Track captured zombies to prevent duplicate events (only fire once per zombie per war)
    private final Map<Integer, Boolean> capturedZombies; // warriorIndex -> captured flag
    
    // Track which warrior wrote to each arena offset (for zombie capture detection)
    // Key: arena offset, Value: warrior index that wrote there
    private final Map<Integer, Integer> arenaWrittenBy;
    
    // Track consecutive foreign code executions per zombie
    // Key: zombie warrior index, Value: [current captor index, consecutive instruction count]
    private final Map<Integer, int[]> zombieForeignCodeExecution;
    
    // Number of consecutive instructions a zombie must execute from foreign code to count as captured
    // (1 instruction could just be a kill, 2+ means it's actually running a program)
    private static final int CAPTURE_INSTRUCTION_THRESHOLD = 2;
    
    // Track whether the war has started (to ignore engine writes during initialization)
    private boolean warStarted;
    private int currentRound;
    
    private static class WarriorRegion {
        final int warriorIndex;
        final String name;
        final WarriorType type;
        
        WarriorRegion(int warriorIndex, String name, WarriorType type) {
            this.warriorIndex = warriorIndex;
            this.name = name;
            this.type = type;
        }
    }
    
    public GameEventDetector(War war) {
        this.war = war;
        this.warriorRegions = new ArrayList<>();
        this.capturedZombies = new HashMap<>();
        this.arenaWrittenBy = new HashMap<>();
        this.zombieForeignCodeExecution = new HashMap<>();
        this.warStarted = false;
        this.currentRound = 0;
        // Initialize write tracking array (will be resized when warriors are loaded)
        this.writesPerWarriorThisRound = new int[20]; // MAX_WARRIORS
    }
    
    /**
     * Updates the current round number. Should be called at the start of each round.
     * Resets per-round write counters for bomb detection.
     * 
     * @param round The current round number
     */
    public void onRound(int round) {
        this.currentRound = round;
        this.warStarted = true;
        
        // Reset write counters for all warriors at the start of each round
        // This is the key to round-based (not time-based) bomb detection
        java.util.Arrays.fill(writesPerWarriorThisRound, 0);
    }
    
    /**
     * Updates warrior region tracking. Should be called when warriors are loaded.
     */
    public void updateWarriorRegions() {
        warriorRegions.clear();
        if (war == null) return;
        
        int numWarriors = war.getNumWarriors();
        for (int i = 0; i < numWarriors; i++) {
            Warrior warrior = war.getWarrior(i);
            if (warrior != null && warrior.isAlive()) {
                warriorRegions.add(new WarriorRegion(
                    i,
                    warrior.getName(),
                    warrior.getType()
                ));
            }
        }
    }
    
    /**
     * Checks if any zombie is currently executing code written by another warrior.
     * A zombie is considered "captured" only after executing multiple consecutive
     * instructions from the same foreign warrior's code (to distinguish from kills).
     * Should be called each round after warriors have executed.
     * 
     * @return GameEvent if a zombie capture was detected, null otherwise
     */
    public GameEvent checkZombieCapture() {
        if (war == null || !warStarted) return null;
        
        int arenaStart = War.ARENA_SEGMENT * 0x10;
        
        for (WarriorRegion region : warriorRegions) {
            // Only check zombies
            if (region.type != WarriorType.ZOMBIE && region.type != WarriorType.ZOMBIE_H) {
                continue;
            }
            
            // Skip already captured zombies
            if (capturedZombies.containsKey(region.warriorIndex)) {
                continue;
            }
            
            Warrior zombie = war.getWarrior(region.warriorIndex);
            if (zombie == null || !zombie.isAlive()) {
                continue;
            }
            
            // Get the zombie's current IP (instruction pointer)
            CpuState state = zombie.getCpuState();
            int cs = Unsigned.unsignedShort(state.getCS());
            int ip = Unsigned.unsignedShort(state.getIP());
            int linearIP = cs * 16 + ip;
            int arenaOffset = linearIP - arenaStart;
            
            // Check if this location was written by another warrior
            Integer writerIndex = arenaWrittenBy.get(arenaOffset);
            if (writerIndex != null && writerIndex != region.warriorIndex) {
                // Zombie is executing foreign code - track consecutive executions
                int[] tracking = zombieForeignCodeExecution.get(region.warriorIndex);
                
                if (tracking == null) {
                    // First time executing foreign code
                    tracking = new int[]{writerIndex, 1};
                    zombieForeignCodeExecution.put(region.warriorIndex, tracking);
                } else if (tracking[0] == writerIndex) {
                    // Same captor - increment counter
                    tracking[1]++;
                } else {
                    // Different captor - reset counter
                    tracking[0] = writerIndex;
                    tracking[1] = 1;
                }
                
                // Check if threshold reached - zombie is truly captured!
                if (tracking[1] >= CAPTURE_INSTRUCTION_THRESHOLD) {
                    capturedZombies.put(region.warriorIndex, true);
                    
                    Warrior captor = war.getWarrior(writerIndex);
                    String captorName = captor != null ? captor.getName() : "Unknown";
                    
                    return new GameEvent(
                        EventType.ZOMBIE_CAPTURED,
                        arenaOffset,
                        region.warriorIndex,
                        region.name,
                        captorName + " captured " + region.name + "!",
                        writerIndex
                    );
                }
            } else {
                // Zombie is executing its own code (or untracked code) - reset tracking
                zombieForeignCodeExecution.remove(region.warriorIndex);
            }
        }
        
        return null;
    }
    
    /**
     * Analyzes a memory write to detect special events.
     * 
     * @param address Memory address being written to
     * @param writerWarriorIndex Index of warrior performing the write
     * @return GameEvent if a special event was detected, null otherwise
     */
    public GameEvent detectMemoryWrite(RealModeAddress address, int writerWarriorIndex) {
        if (war == null || writerWarriorIndex < 0) return null;
        
        int linearAddress = address.getLinearAddress();
        int arenaStart = War.ARENA_SEGMENT * 0x10;
        int arenaEnd = arenaStart + War.ARENA_SIZE;
        
        // Check if write is in arena
        if (linearAddress < arenaStart || linearAddress >= arenaEnd) {
            return null;
        }
        
        int arenaOffset = linearAddress - arenaStart;
        
        // Track who wrote to this location (for zombie capture detection)
        arenaWrittenBy.put(arenaOffset, writerWarriorIndex);
        
        // Check for bomb usage: count writes per warrior per round (not time-based!)
        // A bomb (INT 0x86, REP STOSD, etc.) performs many writes in a single round
        // Only detect after grace period (warriors may legitimately initialize memory early on)
        boolean pastGracePeriod = warStarted && currentRound >= BOMB_DETECTION_GRACE_ROUNDS;
        
        if (pastGracePeriod && writerWarriorIndex < writesPerWarriorThisRound.length) {
            writesPerWarriorThisRound[writerWarriorIndex]++;
        }
        
        if (pastGracePeriod && 
            writerWarriorIndex < writesPerWarriorThisRound.length &&
            writesPerWarriorThisRound[writerWarriorIndex] >= BOMB_WRITE_THRESHOLD) {
            // Reset counter so we don't fire multiple times for same bomb
            writesPerWarriorThisRound[writerWarriorIndex] = 0;
            Warrior writer = war.getWarrior(writerWarriorIndex);
            return new GameEvent(
                EventType.BOMB_USED,
                arenaOffset,
                writerWarriorIndex,
                writer != null ? writer.getName() : "Unknown",
                "BOMB!"
            );
        }
        
        return null;
    }
    
    /**
     * Creates a death event.
     */
    public GameEvent createDeathEvent(String warriorName, int warriorIndex) {
        int arenaOffset = 0;
        if (war != null && warriorIndex >= 0 && warriorIndex < war.getNumWarriors()) {
            Warrior warrior = war.getWarrior(warriorIndex);
            if (warrior != null) {
                arenaOffset = Unsigned.unsignedShort(warrior.getLoadOffset());
            }
        }
        
        return new GameEvent(
            EventType.WARRIOR_DEATH,
            arenaOffset,
            warriorIndex,
            warriorName,
            warriorName + " ELIMINATED!"
        );
    }
    
    /**
     * Converts arena offset to screen coordinates.
     */
    public static int[] arenaOffsetToScreen(int arenaOffset, int boardSize, int dotSize) {
        int x = (arenaOffset % boardSize) * dotSize + dotSize / 2;
        int y = (arenaOffset / boardSize) * dotSize + dotSize / 2;
        return new int[]{x, y};
    }
}
