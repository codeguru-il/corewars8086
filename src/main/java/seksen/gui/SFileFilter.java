/*
 * SFileFilter.java
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
package seksen.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/* ImageFilter.java is used by FileChooserDemo2.java. */
public class SFileFilter extends FileFilter {

	final static public SFileFilter FF_ELKS = new SFileFilter(0);
	final static public SFileFilter FF_HEX =  new SFileFilter(1);
	final static public SFileFilter FF_MAC =  new SFileFilter(2);
	final static public SFileFilter FF_BIN =  new SFileFilter(3);
	final static public SFileFilter FF_LOC =  new SFileFilter(4);
	final static public SFileFilter FF_LST =  new SFileFilter(5);

	final static String extensions[] = new String[]{
			".elks",".hex",".mac",".bin",".loc",".lst"
	};

	final static String descriptions[] = new String[]{
			"(.elks) Elks files",
			"(.hex) Hex files",
			"(.mac) Machine files",
			"(.bin) Binary files",
			"(.loc) Paradigm Location files",
			"(.lst) IDA lst files"
	};

	private byte type;

	private SFileFilter(int type) {
		this.type = (byte) type;
	}

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        return f.getName().endsWith(extensions[type]);
    }

    //The description of this filter
    public String getDescription() {
        return descriptions[type];
    }
}
