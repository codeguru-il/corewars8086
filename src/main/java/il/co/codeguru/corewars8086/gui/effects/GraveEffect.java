package il.co.codeguru.corewars8086.gui.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Grave effect - a permanent marker showing where a warrior died.
 * Displays a grave image with the warrior's name, blinking continuously.
 * This effect never completes - it stays forever.
 */
public class GraveEffect extends AbstractVisualEffect {
    private final int centerX;
    private final int centerY;
    private final String warriorName;
    private final Color nameColor;
    private final boolean isZombie;
    
    // Grave image
    private static BufferedImage graveImage;
    private static final int GRAVE_WIDTH = 36;
    private static final int GRAVE_HEIGHT = 42;
    
    // Load image once statically
    static {
        try {
            graveImage = ImageIO.read(GraveEffect.class.getResourceAsStream("grave.png"));
        } catch (Exception e) {
            System.err.println("Could not load grave.png: " + e.getMessage());
            graveImage = null;
        }
    }
    
    /**
     * Creates a grave marker effect.
     * 
     * @param x Center X position
     * @param y Center Y position
     * @param warriorName Name of the dead warrior
     * @param color Color for the name text (warrior's color)
     * @param isZombie Whether this was a zombie (no grave shown for zombies)
     */
    public GraveEffect(int x, int y, String warriorName, Color color, boolean isZombie) {
        super(Long.MAX_VALUE); // Never expires
        this.centerX = x;
        this.centerY = y;
        this.warriorName = warriorName;
        this.nameColor = color;
        this.isZombie = isZombie;
    }
    
    @Override
    public boolean update(long currentTime) {
        // Never complete - grave stays forever
        return true;
    }
    
    @Override
    public boolean isComplete() {
        return false; // Never complete
    }
    
    @Override
    public void render(Graphics g) {
        // Don't show graves for zombies
        if (isZombie) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        long currentTime = System.currentTimeMillis();
        
        // Blink effect - vary alpha using sine wave
        float blinkPhase = (currentTime % 2000) / 2000.0f; // 2 second cycle
        float blinkIntensity = 0.5f + 0.5f * (float) Math.abs(Math.sin(blinkPhase * Math.PI));
        
        // Draw grave image with blinking alpha
        if (graveImage != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blinkIntensity));
            
            int imgX = centerX - GRAVE_WIDTH / 2;
            int imgY = centerY - GRAVE_HEIGHT / 2;
            g2d.drawImage(graveImage, imgX, imgY, GRAVE_WIDTH, GRAVE_HEIGHT, null);
            
            // Reset composite for text
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            // Fallback: draw a simple cross if image failed to load
            drawFallbackGrave(g2d, blinkIntensity);
        }
        
        // Draw warrior name below the grave
        drawName(g2d, blinkIntensity);
        
        g2d.dispose();
    }
    
    private void drawFallbackGrave(Graphics2D g2d, float alpha) {
        int a = (int) (alpha * 200);
        g2d.setColor(new Color(128, 128, 128, a));
        
        // Simple cross
        g2d.fillRect(centerX - 2, centerY - 10, 4, 16);
        g2d.fillRect(centerX - 6, centerY - 6, 12, 3);
    }
    
    private void drawName(Graphics2D g2d, float alpha) {
        Font font = new Font("Arial", Font.BOLD, 13);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        
        String displayName = warriorName;
        // Truncate long names
        if (displayName.length() > 12) {
            displayName = displayName.substring(0, 10) + "..";
        }
        
        int textWidth = fm.stringWidth(displayName);
        int textX = centerX - textWidth / 2;
        int textY = centerY + GRAVE_HEIGHT / 2 + 4; // Closer to grave
        
        int textAlpha = (int) (alpha * 255);
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, textAlpha / 2));
        g2d.drawString(displayName, textX + 1, textY + 1);
        
        // Draw text in warrior's color
        g2d.setColor(new Color(
            nameColor.getRed(),
            nameColor.getGreen(),
            nameColor.getBlue(),
            textAlpha
        ));
        g2d.drawString(displayName, textX, textY);
    }
}
