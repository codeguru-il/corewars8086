package il.co.codeguru.corewars8086.gui;

import java.awt.Color;
import java.util.Hashtable;

/**
 * @author BS
 */
public class TeamColorHolder {
	public static final int MAX_COLORS = 365;
	private Hashtable<String, Color> teamToColor;
	private int teamInitialsLength;

	private String getTeamInitials(String team) {
		return team.substring(0, Math.min(this.teamInitialsLength, team.length())).toUpperCase();
	}

	public TeamColorHolder(String[] teamNames, int teamInitialsLength) {
		// see http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
		teamToColor = new Hashtable<String, Color>();
		this.teamInitialsLength = teamInitialsLength;

		float golden_ratio_conjugate = 0.618033988749895f;
		float x = 0;
		for (int i = 0; i < teamNames.length; i++) {
			String teamInitials = getTeamInitials(teamNames[i]);
			if (!teamToColor.contains(teamInitials)) {
				teamToColor.put(teamInitials, Color.getHSBColor(x % 1, 0.8f, 0.95f));
				x += golden_ratio_conjugate;

			}
		}
  }

	public Color getColor(String team, boolean darker) {
		String teamInitials = getTeamInitials(team);
		if (darker) {
			return teamToColor.get(teamInitials).darker();
		} else {
			return teamToColor.get(teamInitials);
		}
	}
}
