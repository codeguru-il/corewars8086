package il.co.codeguru.corewars8086.gui.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Particle effect - multiple particles radiating outward.
 * Used for various explosive or impactful events.
 */
public class ParticleEffect extends AbstractVisualEffect {
    private static class Particle {
        float x, y;
        float vx, vy;
        Color color;
        float size;
        float life;
    }
    
    private final List<Particle> particles;
    private final Random random;
    
    public ParticleEffect(int x, int y, Color color, int particleCount, long duration) {
        super(duration);
        this.particles = new ArrayList<>();
        this.random = new Random();
        
        // Initialize particles
        for (int i = 0; i < particleCount; i++) {
            Particle p = new Particle();
            p.x = x;
            p.y = y;
            
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2 + random.nextDouble() * 4;
            p.vx = (float) (Math.cos(angle) * speed);
            p.vy = (float) (Math.sin(angle) * speed);
            
            p.color = color;
            p.size = 2 + random.nextFloat() * 3;
            p.life = 1.0f;
            
            particles.add(p);
        }
    }
    
    @Override
    public boolean update(long currentTime) {
        if (complete) return false;
        
        float progress = getProgress(currentTime);
        float deltaTime = 0.016f; // Approximate frame time
        
        // Update particles
        for (Particle p : particles) {
            p.x += p.vx * deltaTime * 60;
            p.y += p.vy * deltaTime * 60;
            p.vx *= 0.98f; // Friction
            p.vy *= 0.98f;
            p.life = 1.0f - progress;
        }
        
        return super.update(currentTime);
    }
    
    @Override
    public void render(Graphics g) {
        if (complete) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            
            int alpha = (int) (p.life * 255);
            g2d.setColor(new Color(
                p.color.getRed(),
                p.color.getGreen(),
                p.color.getBlue(),
                alpha
            ));
            
            int size = (int) p.size;
            g2d.fillOval((int) p.x - size / 2, (int) p.y - size / 2, size, size);
        }
        
        g2d.dispose();
    }
}

