package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages all active visual effects.
 * Handles updating and rendering effects, and removes completed ones.
 */
public class VisualEffectManager {
    private final List<VisualEffect> effects;
    
    public VisualEffectManager() {
        this.effects = new ArrayList<>();
    }
    
    /**
     * Adds a new visual effect to be managed.
     * 
     * @param effect The effect to add
     */
    public void addEffect(VisualEffect effect) {
        synchronized (effects) {
            effects.add(effect);
        }
    }
    
    /**
     * Updates all active effects and removes completed ones.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        synchronized (effects) {
            Iterator<VisualEffect> it = effects.iterator();
            while (it.hasNext()) {
                VisualEffect effect = it.next();
                if (!effect.update(currentTime) || effect.isComplete()) {
                    it.remove();
                }
            }
        }
    }
    
    /**
     * Renders all active effects.
     * 
     * @param g Graphics context to render to
     */
    public void render(Graphics g) {
        synchronized (effects) {
            for (VisualEffect effect : effects) {
                effect.render(g);
            }
        }
    }
    
    /**
     * Removes all active effects.
     */
    public void clear() {
        synchronized (effects) {
            effects.clear();
        }
    }
    
    /**
     * Gets the number of active effects.
     * 
     * @return Number of active effects
     */
    public int getActiveEffectCount() {
        synchronized (effects) {
            return effects.size();
        }
    }
}

