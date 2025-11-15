package gui;

import java.awt.Color;

/**
 * @author BS
 */
public class ColorHolder {
    private Color colors[];
    private Color darkColors[];
	public static final int MAX_COLORS = 360;
	private static ColorHolder ins = new ColorHolder(MAX_COLORS);

    private ColorHolder(int numPlayers) {
		// see http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/

		colors = new Color[numPlayers];
		float golden_ratio_conjugate = 0.618033988749895f;
		float x = 0;
		for (int i = 0; i < MAX_COLORS; i++) {
			colors[i] = Color.getHSBColor(x % 1, 0.8f, 0.95f);
			x += golden_ratio_conjugate;
		}

        darkColors = new Color[colors.length];
        for (int i = 0; i  < colors.length; i++) {
            darkColors[i] = colors[i].darker();
        }
    }

	public static ColorHolder getInstance() {
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
