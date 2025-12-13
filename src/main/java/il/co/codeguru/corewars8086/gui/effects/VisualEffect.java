package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Graphics;

/**
 * Base interface for all visual effects.
 * Effects are time-based animations that render on top of the canvas.
 */
public interface VisualEffect {
    /**
     * Updates the effect's animation state based on elapsed time.
     * 
     * @param currentTime Current time in milliseconds
     * @return true if the effect should continue, false if it's complete
     */
    boolean update(long currentTime);
    
    /**
     * Renders the effect using the provided Graphics context.
     * 
     * @param g Graphics context to render to
     */
    void render(Graphics g);
    
    /**
     * Checks if the effect has completed and should be removed.
     * 
     * @return true if effect is complete, false otherwise
     */
    boolean isComplete();
}

