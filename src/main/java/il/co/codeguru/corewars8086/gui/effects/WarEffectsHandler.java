package il.co.codeguru.corewars8086.gui.effects;

import il.co.codeguru.corewars8086.gui.Canvas;
import il.co.codeguru.corewars8086.war.Competition;
import il.co.codeguru.corewars8086.war.Warrior;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles visual effects for the war frame.
 * Extracted to keep WarFrame.java focused on core UI logic.
 */
public class WarEffectsHandler {
    
    private final Canvas warCanvas;
    private final Competition competition;
    private GameEventDetector eventDetector;
    private final Map<String, Integer> warriorNameToIndex;
    private final Map<String, Long> recentDeathEvents;
    private boolean effectsEnabled;
    
    public WarEffectsHandler(Canvas warCanvas, Competition competition) {
        this.warCanvas = warCanvas;
        this.competition = competition;
        this.warriorNameToIndex = new HashMap<>();
        this.recentDeathEvents = new HashMap<>();
        this.effectsEnabled = true;
    }
    
    public void setEffectsEnabled(boolean enabled) {
        this.effectsEnabled = enabled;
    }
    
    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }
    
    /**
     * Initialize event detector for a new war.
     */
    public void onWarStart() {
        warCanvas.clearEffects();
        recentDeathEvents.clear();
        
        if (competition.getCurrentWar() != null) {
            eventDetector = new GameEventDetector(competition.getCurrentWar());
            eventDetector.updateWarriorRegions();
            warriorNameToIndex.clear();
            
            for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                Warrior warrior = competition.getCurrentWar().getWarrior(i);
                if (warrior != null) {
                    warriorNameToIndex.put(warrior.getName(), i);
                }
            }
        }
    }
    
    /**
     * Check for special events on memory write.
     */
    public void onMemoryWrite(int writerWarriorIndex, il.co.codeguru.corewars8086.memory.RealModeAddress address) {
        if (eventDetector != null && writerWarriorIndex >= 0) {
            GameEventDetector.GameEvent event = eventDetector.detectMemoryWrite(address, writerWarriorIndex);
            if (event != null) {
                triggerEffectForEvent(event);
            }
        }
    }
    
    /**
     * Update on each round.
     */
    public void onRound(int round) {
        if (eventDetector != null) {
            eventDetector.onRound(round);
            
            if (effectsEnabled) {
                GameEventDetector.GameEvent zombieEvent = eventDetector.checkZombieCapture();
                if (zombieEvent != null) {
                    triggerEffectForEvent(zombieEvent);
                }
            }
        }
    }
    
    /**
     * Update warrior regions when a warrior enters.
     */
    public void onWarriorBirth() {
        if (eventDetector != null) {
            eventDetector.updateWarriorRegions();
        }
    }
    
    /**
     * Trigger death effect.
     */
    public void onWarriorDeath(String warriorName) {
        if (eventDetector == null || competition.getCurrentWar() == null) {
            return;
        }
        
        Integer warriorIndex = warriorNameToIndex.get(warriorName);
        if (warriorIndex == null) {
            for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                Warrior w = competition.getCurrentWar().getWarrior(i);
                if (w != null && w.getName().equals(warriorName)) {
                    warriorIndex = i;
                    warriorNameToIndex.put(warriorName, i);
                    break;
                }
            }
        }
        
        if (warriorIndex != null) {
            GameEventDetector.GameEvent event = eventDetector.createDeathEvent(warriorName, warriorIndex);
            triggerEffectForEvent(event);
        }
    }
    
    /**
     * Update warrior regions at end of round.
     */
    public void onEndRound() {
        if (eventDetector != null && competition.getCurrentWar() != null) {
            eventDetector.updateWarriorRegions();
        }
    }
    
    /**
     * Show war end effects.
     */
    public void onWarEnd(int reason, String winners, int roundNumber) {
        if (!effectsEnabled) {
            return;
        }
        
        int centerX = Canvas.BOARD_SIZE * Canvas.DOT_SIZE / 2;
        int centerY = Canvas.BOARD_SIZE * Canvas.DOT_SIZE / 2;
        int canvasWidth = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        int canvasHeight = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        
        Color winnerColor = getWinnerColor(winners);
        
        if (reason == il.co.codeguru.corewars8086.war.CompetitionEventListener.SINGLE_WINNER) {
            showVictoryEffects(centerX, centerY, canvasWidth, canvasHeight, winners, winnerColor);
        } else if (reason == il.co.codeguru.corewars8086.war.CompetitionEventListener.MAX_ROUND_REACHED) {
            showTimeUpEffects(centerX, centerY, canvasWidth, canvasHeight, winners, winnerColor);
        }
    }
    
    private Color getWinnerColor(String winners) {
        if (competition.getCurrentWar() != null && winners != null && !winners.isEmpty()) {
            String[] winnerNames = winners.split(",");
            if (winnerNames.length > 0) {
                String firstWinner = winnerNames[0].trim();
                for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                    Warrior w = competition.getCurrentWar().getWarrior(i);
                    if (w != null && w.getName().equals(firstWinner)) {
                        return warCanvas.getColorForWarrior(i);
                    }
                }
            }
        }
        return Color.YELLOW;
    }
    
    private void showVictoryEffects(int centerX, int centerY, int canvasWidth, int canvasHeight, 
                                     String winners, Color winnerColor) {
        Color goldColor = new Color(255, 215, 0);
        
        warCanvas.addVisualEffect(new ParticleEffect(centerX, centerY, goldColor, 30, 2000));
        warCanvas.addVisualEffect(new PulseEffect(centerX, centerY, 100, goldColor, 5, 2000));
        
        warCanvas.addVisualEffect(new FloatingTextEffect(
            "VICTORY!", centerX, centerY - 40, goldColor,
            new Font("Arial", Font.BOLD, 42), 100, 12000, canvasWidth, canvasHeight
        ));
        
        warCanvas.addVisualEffect(new FloatingTextEffect(
            "WINNER: " + winners + "!", centerX, centerY + 30, winnerColor,
            new Font("Arial", Font.BOLD, 38), 80, 12000, canvasWidth, canvasHeight
        ));
    }
    
    private void showTimeUpEffects(int centerX, int centerY, int canvasWidth, int canvasHeight,
                                    String winners, Color winnerColor) {
        warCanvas.addVisualEffect(new ParticleEffect(
            centerX, centerY, new Color(150, 150, 255), 20, 1500
        ));
        
        warCanvas.addVisualEffect(new FloatingTextEffect(
            "TIME UP", centerX, centerY - 20, new Color(200, 200, 255),
            new Font("Arial", Font.BOLD, 36), 80, 10000, canvasWidth, canvasHeight
        ));
        
        warCanvas.addVisualEffect(new FloatingTextEffect(
            "Winners: " + winners, centerX, centerY + 30, winnerColor,
            new Font("Arial", Font.BOLD, 26), 60, 10000, canvasWidth, canvasHeight
        ));
    }
    
    /**
     * Triggers visual effects based on detected game events.
     */
    private void triggerEffectForEvent(GameEventDetector.GameEvent event) {
        if (!effectsEnabled) {
            return;
        }
        
        int[] screenPos = GameEventDetector.arenaOffsetToScreen(
            event.arenaOffset, Canvas.BOARD_SIZE, Canvas.DOT_SIZE
        );
        int x = screenPos[0];
        int y = screenPos[1];
        
        Color warriorColor = warCanvas.getColorForWarrior(
            event.warriorIndex >= 0 ? event.warriorIndex : 0
        );
        
        int canvasWidth = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        int canvasHeight = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        
        switch (event.type) {
            case BOMB_USED:
                triggerBombEffect(x, y);
                break;
            case ZOMBIE_CAPTURED:
                triggerZombieCapturedEffect(x, y, event, canvasWidth, canvasHeight);
                break;
            case WARRIOR_DEATH:
                triggerDeathEffect(x, y, event, warriorColor, canvasWidth, canvasHeight);
                break;
        }
    }
    
    private void triggerBombEffect(int x, int y) {
        warCanvas.addVisualEffect(new ExplosionEffect(x, y, 30, Color.CYAN, 800));
        warCanvas.addVisualEffect(new ParticleEffect(x, y, Color.ORANGE, 20, 1000));
    }
    
    private void triggerZombieCapturedEffect(int x, int y, GameEventDetector.GameEvent event,
                                              int canvasWidth, int canvasHeight) {
        String captorName = "Unknown";
        Color captorColor = Color.YELLOW;
        if (competition.getCurrentWar() != null && event.captorIndex >= 0) {
            Warrior captor = competition.getCurrentWar().getWarrior(event.captorIndex);
            if (captor != null) {
                captorName = captor.getName();
                captorColor = warCanvas.getColorForWarrior(event.captorIndex);
            }
        }
        
        warCanvas.addVisualEffect(new FireworksEffect(x, y, captorColor, 5, 2000));
        warCanvas.addVisualEffect(new FireworksEffect(x - 20, y - 15, captorColor, 4, 1800));
        warCanvas.addVisualEffect(new FireworksEffect(x + 20, y - 15, captorColor, 4, 1800));
        warCanvas.addVisualEffect(new ParticleEffect(x, y, captorColor, 25, 1500));
        warCanvas.addVisualEffect(new GlowEffect(x, y, 50, captorColor, 5, 1500));
        
        warCanvas.addVisualEffect(new FloatingTextEffect(
            "ZOMBIE CAPTURED!", x, y - 20, new Color(255, 215, 0),
            new Font("Arial", Font.BOLD, 28), 250, 10000, canvasWidth, canvasHeight
        ));
        warCanvas.addVisualEffect(new FloatingTextEffect(
            captorName + " controls " + event.warriorName, x, y + 10, captorColor,
            new Font("Arial", Font.BOLD, 18), 220, 10000, canvasWidth, canvasHeight
        ));
    }
    
    private void triggerDeathEffect(int x, int y, GameEventDetector.GameEvent event,
                                     Color warriorColor, int canvasWidth, int canvasHeight) {
        // Prevent duplicate death events (within 2 seconds)
        String deathKey = event.warriorName + "_death";
        long currentTime = System.currentTimeMillis();
        Long lastDeathTime = recentDeathEvents.get(deathKey);
        if (lastDeathTime != null && (currentTime - lastDeathTime) < 2000) {
            return;
        }
        recentDeathEvents.put(deathKey, currentTime);
        
        boolean isZombie = false;
        if (competition.getCurrentWar() != null && event.warriorIndex >= 0) {
            Warrior deadWarrior = competition.getCurrentWar().getWarrior(event.warriorIndex);
            if (deadWarrior != null && deadWarrior.isZombie()) {
                isZombie = true;
            }
        }
        
        warCanvas.addVisualEffect(new GlowEffect(x, y, 35, warriorColor, 4, 1000));
        warCanvas.addVisualEffect(new GraveEffect(x, y, event.warriorName, warriorColor, isZombie));
        
        if (isZombie) {
            warCanvas.addVisualEffect(new FloatingTextEffect(
                event.warriorName + " DESTROYED!", x, y, new Color(150, 150, 150),
                new Font("Arial", Font.BOLD, 20), 200, 8000, canvasWidth, canvasHeight
            ));
        } else {
            warCanvas.addVisualEffect(new FloatingTextEffect(
                event.message, x, y, warriorColor,
                new Font("Arial", Font.BOLD, 24), 250, 10000, canvasWidth, canvasHeight
            ));
        }
    }
}


