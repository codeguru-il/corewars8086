package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.utils.EventMulticaster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;


/**
 * @author BS
 */
public class Canvas extends JComponent implements MouseInputListener {
	private static final long serialVersionUID = 1L;
	
	public static final int BOARD_SIZE = 256;
    public static final int DOT_SIZE = 2;
    public static final byte EMPTY = -1;

    private byte[][] data;
    private boolean[][] darker;
    
    private boolean[][] pointer; 
       
	private EventMulticaster eventCaster;
	private MouseAddressRequest eventHandler;

	private int MouseX, MouseY;

    public Canvas() {
		eventCaster = new EventMulticaster(MouseAddressRequest.class);
		eventHandler = (MouseAddressRequest) eventCaster.getProxy();
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.MouseX = 0;
		this.MouseY = 0;
        clear();
    }

    public static Dimension getCanvasSize()
    {
    	return new Dimension(BOARD_SIZE * DOT_SIZE, BOARD_SIZE * DOT_SIZE);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(BOARD_SIZE * DOT_SIZE, BOARD_SIZE * DOT_SIZE);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public void paintPixel(int number, byte color, boolean darker) {
        paintPixel(number % BOARD_SIZE, number / BOARD_SIZE, color, darker);
    }

    public void paintPixel(int x, int y, byte color, boolean darker) {
        data[x][y] = color;
        this.darker[x][y] = darker;
        Graphics g = getGraphics();
        if (g != null) {
            g.setColor(ColorHolder.getInstance().getColor(color, darker));
            g.fillRect(x * DOT_SIZE, y * DOT_SIZE, DOT_SIZE, DOT_SIZE);
        }
    }

    /** 
     * Get the color of warrior <code>id</code>
     */
    public Color getColorForWarrior(int id, boolean isSecond) {
        return ColorHolder.getInstance().getColor(id, isSecond);
    }
    
    
	public void paintPointer(int x, int y, Color color) {
		pointer[x][y] = true;
		Graphics g = getGraphics();
		if (g != null) {
			g.setColor(color);
			g.fillRect(x * DOT_SIZE, y * DOT_SIZE, DOT_SIZE, DOT_SIZE);
		}
	}
    
	public void paintPointer(int x, int y, byte color) {
		this.paintPointer(x, y, ColorHolder.getInstance().getColor(color, true));
	}

	public void paintPointer(int number, byte color) {
		this.paintPointer(number % BOARD_SIZE, number / BOARD_SIZE, color);
	}

    /**
     * Clears the entire canvas
     */
    public void clear() {
		data = new byte[BOARD_SIZE][BOARD_SIZE];
		darker = new boolean[BOARD_SIZE][BOARD_SIZE];
		pointer = new boolean[BOARD_SIZE][BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE; i++)
			for (int j = 0; j < BOARD_SIZE; j++) {
				data[i][j] = EMPTY;
				darker[i][j] = false;
				pointer[i][j] = false;
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

                g.setColor(ColorHolder.getInstance().getColor(cellVal, darker[x][y]));
                g.fillRect(x*DOT_SIZE, y*DOT_SIZE, DOT_SIZE, DOT_SIZE);
            }
        }
    }
    
	@Override
	public void mouseMoved(MouseEvent e) {

		Graphics g = this.getGraphics();

		if (g != null) {
			// delete Mouse
			this.clearMousePointer(g);

			if (true) {
				MouseX = e.getX() / DOT_SIZE;
				MouseY = e.getY() / DOT_SIZE;

				// draw new Mouse
				g.setColor(Color.WHITE);

				g.fillRect(MouseX * DOT_SIZE, MouseY * DOT_SIZE, DOT_SIZE,
						DOT_SIZE);
			}
		}
	}
    
	private void clearMousePointer(Graphics g) {
		try {
			g.setColor(ColorHolder.getInstance()
					.getColor(data[MouseX][MouseY], darker[MouseX][MouseY]));
		} catch (Exception ex) {
			// TODO the true variable of the color
			g.setColor(new Color(51, 51, 51)); 
		}
		g.fillRect(MouseX * DOT_SIZE, MouseY * DOT_SIZE, DOT_SIZE, DOT_SIZE);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		eventHandler.addressAtMouseLocationRequested(this.MouseX + BOARD_SIZE* this.MouseY);
	}
	
	@Override
	public void mouseExited(MouseEvent arg0) {
		this.clearMousePointer(this.getGraphics());
	}

	public void addListener(MouseAddressRequest l) {
		eventCaster.add(l);
	}
	
	public void deletePointers() {
		for (int i = 0; i < BOARD_SIZE; i++)
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (pointer[i][j] == true && data[i][j] != EMPTY) {
					pointer[i][j] = false;
					paintPixel(i, j, data[i][j], darker[i][j]);
				}
			}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
