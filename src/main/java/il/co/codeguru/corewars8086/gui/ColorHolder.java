package il.co.codeguru.corewars8086.gui;

import java.awt.Color;

/**
 * @author BS
 */
public class ColorHolder {
    private Color colors[];
    private Color darkColors[];
    private static ColorHolder ins;

    private ColorHolder(int numPlayers) {
		colors = new Color[numPlayers];
		float interval = 360 / (numPlayers);
		float x = 0;
		for (int i = 0; i < numPlayers; i++) {
			colors[i] = Color.getHSBColor(x / 360, 1, 1);
			x += interval;
		}

        darkColors = new Color[colors.length];
        for (int i = 0; i  < colors.length; i++) {
            darkColors[i] = colors[i].darker();
        }
    }

	public static ColorHolder getInstance() {
		if (ins == null) {
			throw new IllegalArgumentException("First call getInstance with numPlayers to init instance");
		}
		return ins;
	}

    public static ColorHolder getInstance(int numPlayers) {
        if (ins == null) {
            ins = new ColorHolder(numPlayers);
        }
        return ins;
    }

    public Color getColor(int pos, boolean darker) {
        if (darker) {
            return darkColors[pos];
        } else {
            return colors[pos];
        }
    }
}
