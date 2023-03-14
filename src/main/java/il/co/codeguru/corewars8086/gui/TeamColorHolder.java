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

    private String getTeamInitials(String team) {
        return team.substring(0, Math.min(this.teamInitialsLength, team.length())).toUpperCase();
    }

    public TeamColorHolder(String[] teamNames, int teamInitialsLength, Options options) {
        // see http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
        colorHolder = new Hashtable<>();
        this.teamInitialsLength = teamInitialsLength;

        fillCenterColors(options.colorsFile);

        float golden_ratio_conjugate = 0.618033988749895f;
        float x = 0;
        for (String teamName : teamNames) {
            String teamInitials = getTeamInitials(teamName);
            if (!colorHolder.contains(teamInitials)) {
                colorHolder.put(teamInitials, Color.getHSBColor(x % 1, 0.8f, 0.95f));
                x += golden_ratio_conjugate;

            }
        }
    }

    private void fillCenterColors(String filename) {
        File colorsFile = new File(filename);
        if (!colorsFile.isFile()) return;

        try {
            List<String> lines = Files.readAllLines(colorsFile.toPath());

            for (String line : lines) {
                String[] split = line.split(",\\s*", 2);

                if (split.length < 2) continue;

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
    private Color decodeHexColor(String color) {
        if (!color.matches("#[0-9a-fA-F]{6}")) {
            throw new NumberFormatException(String.format("%s is not a valid color string", color));
        }

        String hstr = color.replace("#", "0x");
        return Color.decode(hstr);
    }

    public Color getColor(String team, boolean darker) {
        String teamInitials = getTeamInitials(team);
        if (darker) {
            return colorHolder.get(teamInitials).darker();
        } else {
            return colorHolder.get(teamInitials);
        }
    }
}
