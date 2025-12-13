package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/**
 * Glow effect - a localized area that blinks/pulses with a radial glow.
 * Creates a soft glowing highlight that pulses multiple times.
 * 
 * @author Visual Effects System
 */
public class GlowEffect extends AbstractVisualEffect {
    private final int centerX;
    private final int centerY;
    private final int radius;
    private final Color glowColor;
    private final int blinkCount;
    
    /**
     * Creates a glowing blink effect.
     * 
     * @param x Center X position
     * @param y Center Y position
     * @param radius Radius of the glow
     * @param color Glow color
     * @param blinkCount Number of blink pulses
     * @param duration Total effect duration in milliseconds
     */
    public GlowEffect(int x, int y, int radius, Color color, int blinkCount, long duration) {
        super(duration);
        this.centerX = x;
        this.centerY = y;
        this.radius = radius;
        this.glowColor = color;
        this.blinkCount = blinkCount;
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        long currentTime = System.currentTimeMillis();
        float progress = getProgress(currentTime);
        
        // Calculate blink intensity using sine wave for smooth pulsing
        // Multiple blinks over the duration
        float blinkPhase = progress * blinkCount * (float) Math.PI * 2;
        float blinkIntensity = (float) Math.pow(Math.max(0, Math.sin(blinkPhase)), 2);
        
        // Fade out overall as effect progresses
        float fadeOut = 1.0f - (progress * 0.5f); // Only fade to 50% so last blinks are still visible
        float alpha = blinkIntensity * fadeOut;
        
        if (alpha <= 0.01f) {
            g2d.dispose();
            return;
        }
        
        // Draw radial gradient glow
        Point2D center = new Point2D.Float(centerX, centerY);
        float[] dist = {0.0f, 0.5f, 1.0f};
        Color[] colors = {
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(alpha * 200)),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(alpha * 100)),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)
        };
        
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(gradient);
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Add a brighter core
        int coreRadius = radius / 3;
        Color[] coreColors = {
            new Color(255, 255, 255, (int)(alpha * 180)),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(alpha * 80)),
            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)
        };
        
        RadialGradientPaint coreGradient = new RadialGradientPaint(center, coreRadius, dist, coreColors);
        g2d.setPaint(coreGradient);
        g2d.fillOval(centerX - coreRadius, centerY - coreRadius, coreRadius * 2, coreRadius * 2);
        
        g2d.dispose();
    }
}


