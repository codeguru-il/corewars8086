package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fireworks effect - colorful sparks that shoot up and fall with gravity.
 * Creates a celebratory burst effect perfect for special events like zombie captures.
 * 
 * @author Visual Effects System
 */
public class FireworksEffect extends AbstractVisualEffect {
    
    private static class Spark {
        float x, y;
        float vx, vy;
        Color color;
        float size;
        float life;
        float maxLife;
    }
    
    private final List<Spark> sparks;
    private final Random random;
    private final int centerX;
    private final int centerY;
    private final Color baseColor;
    private final float gravity = 0.15f;
    private long lastBurstTime;
    private int burstCount;
    private final int maxBursts;
    
    /**
     * Creates a fireworks effect.
     * 
     * @param x Center X position
     * @param y Center Y position
     * @param baseColor Base color for the fireworks (will create variations)
     * @param burstCount Number of burst explosions
     * @param duration Total effect duration in milliseconds
     */
    public FireworksEffect(int x, int y, Color baseColor, int burstCount, long duration) {
        super(duration);
        this.centerX = x;
        this.centerY = y;
        this.baseColor = baseColor;
        this.sparks = new ArrayList<>();
        this.random = new Random();
        this.lastBurstTime = 0;
        this.burstCount = 0;
        this.maxBursts = burstCount;
        
        // Create initial burst immediately
        createBurst(x, y);
    }
    
    private void createBurst(int burstX, int burstY) {
        // Create colorful sparks radiating outward
        int sparkCount = 12 + random.nextInt(8); // 12-20 sparks per burst
        
        for (int i = 0; i < sparkCount; i++) {
            Spark s = new Spark();
            s.x = burstX;
            s.y = burstY;
            
            // Random direction with upward bias
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2 + random.nextDouble() * 4;
            s.vx = (float) (Math.cos(angle) * speed);
            s.vy = (float) (Math.sin(angle) * speed - 2); // Upward bias
            
            // Create color variations - mix base color with random bright colors
            s.color = createVariedColor();
            s.size = 2 + random.nextFloat() * 2;
            s.maxLife = 0.6f + random.nextFloat() * 0.4f; // 0.6-1.0 life
            s.life = s.maxLife;
            
            sparks.add(s);
        }
        
        burstCount++;
    }
    
    private Color createVariedColor() {
        // Create sparkly color variations
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();
        
        // Random chance for golden/white sparkle
        if (random.nextFloat() < 0.3f) {
            return new Color(255, 255, 200); // Golden white sparkle
        }
        
        // Add some variation to base color
        r = Math.min(255, r + random.nextInt(60) - 20);
        g = Math.min(255, g + random.nextInt(60) - 20);
        b = Math.min(255, b + random.nextInt(60) - 20);
        
        return new Color(Math.max(0, r), Math.max(0, g), Math.max(0, b));
    }
    
    @Override
    public boolean update(long currentTime) {
        if (complete) return false;
        
        float deltaTime = 0.016f; // ~60fps
        
        // Create additional bursts at intervals
        long elapsed = currentTime - startTime;
        long burstInterval = duration / (maxBursts + 1);
        if (burstCount < maxBursts && elapsed - lastBurstTime > burstInterval) {
            // Create burst at slightly offset position
            int offsetX = centerX + random.nextInt(30) - 15;
            int offsetY = centerY + random.nextInt(20) - 10;
            createBurst(offsetX, offsetY);
            lastBurstTime = elapsed;
        }
        
        // Update all sparks
        for (Spark s : sparks) {
            if (s.life <= 0) continue;
            
            // Apply physics
            s.x += s.vx * deltaTime * 60;
            s.y += s.vy * deltaTime * 60;
            s.vy += gravity; // Gravity pulls down
            s.vx *= 0.98f; // Air resistance
            
            // Decay life
            s.life -= deltaTime * 1.5f;
        }
        
        // Remove dead sparks
        sparks.removeIf(s -> s.life <= 0);
        
        return super.update(currentTime);
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        
        for (Spark s : sparks) {
            if (s.life <= 0) continue;
            
            float lifeRatio = s.life / s.maxLife;
            int alpha = (int) (lifeRatio * 255);
            
            // Draw spark with glow
            g2d.setColor(new Color(
                s.color.getRed(),
                s.color.getGreen(),
                s.color.getBlue(),
                alpha / 2
            ));
            
            // Outer glow
            int glowSize = (int) (s.size * 2);
            g2d.fillOval((int) s.x - glowSize / 2, (int) s.y - glowSize / 2, glowSize, glowSize);
            
            // Inner bright core
            g2d.setColor(new Color(
                Math.min(255, s.color.getRed() + 50),
                Math.min(255, s.color.getGreen() + 50),
                Math.min(255, s.color.getBlue() + 50),
                alpha
            ));
            
            int size = (int) s.size;
            g2d.fillOval((int) s.x - size / 2, (int) s.y - size / 2, size, size);
        }
        
        g2d.dispose();
    }
}

