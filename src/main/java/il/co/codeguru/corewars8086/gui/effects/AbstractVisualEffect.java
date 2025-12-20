package il.co.codeguru.corewars8086.gui.effects;

/**
 * Abstract base class for visual effects providing common functionality.
 */
public abstract class AbstractVisualEffect implements VisualEffect {
    protected long startTime;
    protected long duration;
    protected boolean complete;
    
    /**
     * Creates a new visual effect with the specified duration.
     * 
     * @param duration Duration in milliseconds
     */
    protected AbstractVisualEffect(long duration) {
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
        this.complete = false;
    }
    
    @Override
    public boolean update(long currentTime) {
        long elapsed = currentTime - startTime;
        if (elapsed >= duration) {
            complete = true;
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isComplete() {
        return complete;
    }
    
    /**
     * Gets the progress of the effect (0.0 to 1.0).
     * 
     * @param currentTime Current time in milliseconds
     * @return Progress from 0.0 (start) to 1.0 (end)
     */
    protected float getProgress(long currentTime) {
        long elapsed = currentTime - startTime;
        return Math.min(1.0f, (float) elapsed / duration);
    }
}



