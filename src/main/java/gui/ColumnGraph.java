package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.cli.Options;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * @author BS
 */
public class ColumnGraph extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private float[][] values;
    private String[] names;
    private float maxValue;
    private double reduceFactor;
    private long numTopTeams;
    private TeamColorHolder teamColorHolder;

    private static final int NAME_HEIGHT = 14;
    // We assume the teams' first 3 characters are the school name
    private static final int SCHOOL_PREFIX_LEN = 3;

    public ColumnGraph(String[] names, Options options) {
        super();
        this.names = new String[names.length];
        // the first element holds the sum of all the other values
        values = new float[names.length][3];
        System.arraycopy(names, 0, this.names, 0, names.length);
        maxValue = 0;
        reduceFactor = 5;
        numTopTeams = Math.min(names.length / 2, 10); // If you are in the top half you count as top team
        // Give out colors by the team name first 3 characters
        // So teams from the same school have the same color
        teamColorHolder = new TeamColorHolder(names, SCHOOL_PREFIX_LEN, options);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(500,500);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public void addToValue(int pos, int subIndex, float value) {
        values[pos][0]+= value;
        values[pos][subIndex+1]+= value;

        if (values[pos][0] > maxValue) {
            // reset graph factor by half to make more room
            maxValue = values[pos][0];
            if (maxValue * reduceFactor > getSize().height-10) {
                reduceFactor *= 0.5;
            }
        }
        repaint();
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
        Dimension d = getSize();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,d.width, d.height);
        d.setSize(d.width, d.height - NAME_HEIGHT);
        g.drawRect(0,0,d.width, d.height);
		final int numPlayers = names.length;
		int columnWidth = d.width / numPlayers;

        ColorHolder colorHolder= ColorHolder.getInstance();
        for (int i = 0; i < numPlayers; i++) {
            paintColumn(g, i, columnWidth, d.height, colorHolder);
            g.setColor(getTeamColor(i, false));
            g.drawString(names[i], i*columnWidth+5, d.height+NAME_HEIGHT-2);
        }
    }

    private int getTeamRating(int teamCol) {
        int rating = 1;
        for (int i = 0; i < values.length; i++) {
            // For every different team that has a higher score, this teams ranks one less 
            // For a tie, both teams lose one rating score
            if (teamCol != i && values[teamCol][0] <= values[i][0]) {
                rating++;
            }
        }
        return rating;
    }

    private boolean isTopTeam(int teamCol) {
        return getTeamRating(teamCol) <= numTopTeams;
    }

    private Color getTeamColor(int teamIndex, boolean darker) {
        return teamColorHolder.getColor(names[teamIndex], darker);
    }

    // Copied from this stackover flow question - https://stackoverflow.com/questions/10083913/how-to-rotate-text-with-graphics2d-in-java
    public static void drawRotatedString(Graphics g, double x, double y, int angle, String text) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate((float)x,(float)y);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawString(text,0,0);
        g2d.rotate(-Math.toRadians(angle));
        g2d.translate(-(float)x,-(float)y);
    }

    public static Color invertColor(Color c) {
        int r = 255 - c.getRed();
        int g = 255 - c.getGreen();
        int b = 255 - c.getBlue();
        return new Color(r, g, b, c.getAlpha());
    }

    private void paintColumn(Graphics g, int col, int width, int startHeight, ColorHolder colorHolder) {
        g.setColor(getTeamColor(col, false));
        int height1 = (int) (reduceFactor*values[col][1]);
        g.fill3DRect(col*width, startHeight - height1, width, height1, true);
        g.setColor(getTeamColor(col, true));
        int height2 = (int) (reduceFactor*values[col][2]);
        int boxTopY = startHeight - height1 - height2;
        g.fill3DRect(col*width, boxTopY, width, height2, false);
        g.drawString("" + (int)(values[col][0] * 10)/10.0f, col*width, boxTopY - 5);


        if (isTopTeam(col)) {
            Font origFontBackup = g.getFont();
            // For top teams, draw their initials on top of the rectangle
            // This should be their school initials - for example,
            // "OST" for Ostrovski or "GBA" for GreenBlitz Academy
            String teamInitials = names[col].substring(0, Math.min(SCHOOL_PREFIX_LEN, names[col].length())).toUpperCase();

            // About font size - the number specified is the font's "em height"
            // See this stackoverflow question - https://graphicdesign.stackexchange.com/questions/4035/what-does-the-size-of-the-font-translate-to-exactly
            // We treat as approximately the max height of a character
            // wild guess - The width of the common character will be around the same
            int teamInitialsFontSize = Math.min(width/2, 100);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, teamInitialsFontSize));
            g.drawString(teamInitials, col*width, boxTopY - 25);

           
            
            Color bakColor = g.getColor();
            Color gold = new Color(255, 215, 0);
            g.setColor(gold);
            // Draw the rating (e.g "1") on top of the box, in gold
            int ratingFontSize = (int)Math.min(width*1.3, 180);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, ratingFontSize));
            g.drawString(String.valueOf(getTeamRating(col)), (int)((col + 0.1)*(width)), boxTopY - teamInitialsFontSize - 35);
            g.setColor(bakColor);



            // Here the font size should match:
            // The font height is the width because it's horizontal
            int rotatedNameFontSize = (int)Math.min(width * 0.6, 140);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, rotatedNameFontSize));
            int charwidth = g.getFontMetrics().charWidth('A'); // monospaced so all char widths are the same
            int lettersCanFitInRect = (int)((height1 + height2) / charwidth);
            String nameCanFit = names[col].substring(0, Math.min(lettersCanFitInRect, names[col].length()));

            // Invert the color so we can see best
            Color curColor = g.getColor();
            g.setColor(invertColor(curColor));
            drawRotatedString(g, col*width + g.getFontMetrics().getAscent()/2, boxTopY + 5, 90, nameCanFit);
            g.setColor(curColor);
            

            g.setFont(origFontBackup);
        }
    }	
}
