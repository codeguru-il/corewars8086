package il.co.codeguru.corewars8086.gui;

import java.awt.Color;

/**
 * @author BS
 */
public class ColorHolder {
    private Color colors[];
    private Color darkColors[];
    private static ColorHolder ins;

    private ColorHolder() {
        colors = new Color[21];
        colors[0] = Color.WHITE;
        colors[1] = Color.BLUE;
        colors[2] = Color.CYAN;
        colors[3] = Color.GREEN;
        colors[4] = Color.MAGENTA;
        colors[5] = Color.ORANGE;
        colors[6] = Color.PINK;
        colors[7] = Color.RED;
        colors[8] = Color.WHITE;
        colors[9] = Color.YELLOW;
        colors[10] = Color.GRAY;
        colors[11] = Color.LIGHT_GRAY;
        colors[12] = new Color(0xB9FEB4); // flourescent green
        colors[13] = new Color(153,255,153); // light green
        colors[14] = new Color(204,51,255); // light purple
        colors[15] = new Color(51,102,255); // light blue
        colors[16] = new Color(0,51,51); // turquize
        colors[17] = new Color(153,204,204); // something...
        colors[18] = new Color(204,204, 0); //mustared
        colors[19] = new Color(255,255,204); // light yellow
        colors[20] = new Color(0xD3E0FD);

        darkColors = new Color[colors.length];
        for (int i = 0; i  < colors.length; i++) {
            darkColors[i] = colors[i].darker();
        }
    }

    public static ColorHolder getInstance() {
        if (ins == null) {
            ins = new ColorHolder();
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
