package il.co.codeguru.corewars8086.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;

import javax.swing.JComponent;

/**
 * @author BS
 */
public class Canvas extends JComponent {
	private static final long serialVersionUID = 1L;
	
	public static final int BOARD_SIZE = 256;
    public static final int DOT_SIZE = 2;
    public static final byte EMPTY = -1;

    private  byte[][] data;

    public Canvas() {
        clear();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(BOARD_SIZE * DOT_SIZE, BOARD_SIZE * DOT_SIZE);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public void paintPixel(int number, byte color) {
        paintPixel(number % BOARD_SIZE, number / BOARD_SIZE, color);
    }

    public void paintPixel(int x, int y, byte color) {
        data[x][y] = color;
        Graphics g = getGraphics();
        if (g != null) {
            g.setColor(ColorHolder.getInstance().getColor(color,false));
            g.fillRect(x * DOT_SIZE, y * DOT_SIZE, DOT_SIZE, DOT_SIZE);
        }
    }

    /** 
     * Get the color of warrior <code>id</code>
     */
    public Color getColorForWarrior(int id) {
        return ColorHolder.getInstance().getColor(id,false);
    }

    /**
     * Clears the entire canvas
     */
    public void clear() {
        data = new byte[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(data[i], EMPTY);
        }
        repaint();
    }

    /**
     * When we have to - repaint the entire canvas
     */
    @Override
    public void paint(Graphics g) {
        g.fillRect(0,0, BOARD_SIZE * DOT_SIZE, BOARD_SIZE * DOT_SIZE);

        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                int cellVal = data[x][y];
                if (cellVal == EMPTY) {
                    continue;
                }

                g.setColor(ColorHolder.getInstance().getColor(cellVal,false));
                g.fillRect(x*DOT_SIZE, y*DOT_SIZE, DOT_SIZE, DOT_SIZE);
            }
        }
    }
}
