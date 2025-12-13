package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Flash effect - full-screen color overlay that fades out.
 * Used for dramatic events like deaths or captures.
 * 
 * @author Visual Effects System
 */
public class FlashEffect extends AbstractVisualEffect {
    private final Color flashColor;
    private final int width;
    private final int height;
    
    public FlashEffect(Color color, int width, int height, long duration) {
        super(duration);
        this.flashColor = color;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        long currentTime = System.currentTimeMillis();
        float progress = getProgress(currentTime);
        
        // Fade out quickly (first 30% of duration)
        float alpha = Math.max(0, 1.0f - (progress * 3.33f));
        
        g2d.setColor(new Color(
            flashColor.getRed(),
            flashColor.getGreen(),
            flashColor.getBlue(),
            (int) (alpha * 128) // Max 50% opacity
        ));
        
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
    }
}


