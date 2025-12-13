package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Pulse effect - concentric ripples expanding from a point.
 * Used for captures and other impactful events.
 */
public class PulseEffect extends AbstractVisualEffect {
    private final int centerX;
    private final int centerY;
    private final int maxRadius;
    private final Color color;
    private final int ringCount;
    
    public PulseEffect(int x, int y, int maxRadius, Color color, int ringCount, long duration) {
        super(duration);
        this.centerX = x;
        this.centerY = y;
        this.maxRadius = maxRadius;
        this.color = color;
        this.ringCount = ringCount;
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        long currentTime = System.currentTimeMillis();
        float progress = getProgress(currentTime);
        
        // Draw multiple expanding rings
        for (int i = 0; i < ringCount; i++) {
            float ringProgress = (progress + i * (1.0f / ringCount)) % 1.0f;
            if (ringProgress < 0) continue;
            
            int radius = (int) (maxRadius * ringProgress);
            float alpha = 1.0f - ringProgress;
            
            if (alpha <= 0) continue;
            
            g2d.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (alpha * 255)
            ));
            
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
        
        g2d.dispose();
    }
}



