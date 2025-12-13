package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Explosion effect - expanding circles with color fade.
 * Used for bomb detonations.
 * 
 * @author Visual Effects System
 */
public class ExplosionEffect extends AbstractVisualEffect {
    private final int centerX;
    private final int centerY;
    private final int maxRadius;
    private final Color color;
    
    public ExplosionEffect(int x, int y, int maxRadius, Color color, long duration) {
        super(duration);
        this.centerX = x;
        this.centerY = y;
        this.maxRadius = maxRadius;
        this.color = color;
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        long currentTime = System.currentTimeMillis();
        float progress = getProgress(currentTime);
        
        // Multiple expanding circles
        for (int i = 0; i < 3; i++) {
            float circleProgress = Math.max(0, progress - i * 0.2f);
            if (circleProgress <= 0) continue;
            
            int radius = (int) (maxRadius * circleProgress);
            float alpha = 1.0f - circleProgress;
            
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


