/*
 * SourceLine.java
 *
 * Copyright (C) 2006 - 2008 Erdem Güven <zuencap@users.sourceforge.net>.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package seksen.debug;

public class SourceLine {
	private final String sourcePath;
	private final int line;

	public SourceLine(String sourcePath, int line) {
		this.sourcePath = sourcePath;
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public String getPath() {
		return sourcePath;
	}
}
