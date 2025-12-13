package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Floating text effect - text that floats upward and fades out.
 * Used for event announcements.
 * 
 * @author Visual Effects System
 */
public class FloatingTextEffect extends AbstractVisualEffect {
    private final String text;
    private final int startX;
    private final int startY;
    private final Color textColor;
    private final Font font;
    private final float floatDistance;
    private final int canvasWidth;
    private final int canvasHeight;
    
    public FloatingTextEffect(String text, int x, int y, Color color, Font font, float floatDistance, long duration, int canvasWidth, int canvasHeight) {
        super(duration);
        this.text = text;
        this.textColor = color;
        this.font = font != null ? font : new Font("Arial", Font.BOLD, 16);
        this.floatDistance = floatDistance;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        
        // Clamp initial position to keep text within bounds
        // We'll calculate text width during render, but for now use a safe initial position
        this.startX = x;
        this.startY = y;
    }
    
    /**
     * Wraps text to fit within maxWidth, splitting at word boundaries.
     */
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        
        // If text already fits, return as single line
        if (fm.stringWidth(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }
        
        // Split at word boundaries
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            if (fm.stringWidth(testLine) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Current line is full, start new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, just add it
                    lines.add(word);
                }
            }
        }
        
        // Add remaining text
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        long currentTime = System.currentTimeMillis();
        float progress = getProgress(currentTime);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        
        // Wrap text to fit within canvas (with some margin)
        int maxWidth = canvasWidth - 20;
        List<String> lines = wrapText(text, fm, maxWidth);
        
        // Float upward
        int currentY = (int) (startY - floatDistance * progress);
        
        // Fade out - stay fully visible for 70% of duration, then fade in last 30%
        float alpha;
        if (progress < 0.7f) {
            alpha = 1.0f; // Fully visible
        } else {
            // Fade from 1.0 to 0.0 over the last 30%
            alpha = 1.0f - ((progress - 0.7f) / 0.3f);
        }
        if (alpha <= 0) {
            g2d.dispose();
            return;
        }
        
        // Draw each line
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = fm.stringWidth(line);
            
            // Center horizontally, or clamp to canvas bounds
            int lineX = Math.max(5, Math.min(startX - lineWidth / 2, canvasWidth - lineWidth - 5));
            int lineY = currentY + (i * lineHeight);
            
            // Clamp y position
            lineY = Math.max(lineHeight, Math.min(lineY, canvasHeight - 5));
            
            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, (int) (alpha * 128)));
            g2d.drawString(line, lineX + 2, lineY + 2);
            
            // Draw text
            g2d.setColor(new Color(
                textColor.getRed(),
                textColor.getGreen(),
                textColor.getBlue(),
                (int) (alpha * 255)
            ));
            g2d.drawString(line, lineX, lineY);
        }
        
        g2d.dispose();
    }
}

