package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.cli.Options;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;

/**
 * @author BS
 * @author RM
 */
public class TeamColorHolder {
    private final Hashtable<String, Color> colorHolder;
    private final int teamInitialsLength;

    private static final int COLUMNS_IN_COLORS_FILE = 2;

    private String getTeamInitials(String team) {
        return team.substring(0, Math.min(this.teamInitialsLength, team.length())).toUpperCase();
    }

    public TeamColorHolder(String[] teamNames, int teamInitialsLength, Options options) {
        colorHolder = new Hashtable<>();
        this.teamInitialsLength = teamInitialsLength;

        fillCenterColors(options.colorsFile);
        fillTeamColors(teamNames);
    }

    private void fillTeamColors(String[] teamNames) {
        // see http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/

        final float SATURATION = 0.8f;
        final float BRIGHTNESS = 0.95f;
        final float GOLDEN_RATIO_CONJUGATE = 0.618033988749895f;

        float x = 0;
        for (String teamName : teamNames) {
            String teamInitials = getTeamInitials(teamName);
            if (!colorHolder.contains(teamInitials)) {
                colorHolder.put(teamInitials, Color.getHSBColor(x % 1, SATURATION, BRIGHTNESS));
                x += GOLDEN_RATIO_CONJUGATE;
            }
        }
    }

    private void fillCenterColors(String filename) {
        File colorsFile = new File(filename);
        if (!colorsFile.isFile()) return;

        try {
            List<String> lines = Files.readAllLines(colorsFile.toPath());

            for (String line : lines) {
                String[] split = line.split(",\\s*", COLUMNS_IN_COLORS_FILE);

                if (split.length < COLUMNS_IN_COLORS_FILE) continue;

                String center = split[0];
                String rawColor = split[1];

                if (center.length() != teamInitialsLength) continue;

                colorHolder.put(center.toUpperCase(), decodeHexColor(rawColor));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decodes a hex string of the format {@code #RRGGBB} (e.g., {@code "#00FF00"}) to a {@link java.awt.Color}
     */
    private Color decodeHexColor(String rawColor) {
        if (!rawColor.matches("#[0-9a-fA-F]{6}")) {
            throw new NumberFormatException(String.format("%s is not a valid color string", rawColor));
        }

        String hexColor = rawColor.replace("#", "0x");
        return Color.decode(hexColor);
    }

    public Color getColor(String team, boolean darker) {
        String teamInitials = getTeamInitials(team);
        Color color = colorHolder.get(teamInitials);

        return darker ? color.darker() : color;
    }
}
