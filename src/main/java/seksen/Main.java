/*
 * Main.java
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
package seksen;

import seksen.gui.NewMachineDialog;

public class Main {
    public static void newMachine() {
    	new NewMachineDialog().setVisible(true);
	}

    public static void main(String[] args) {
    	newMachine();
    }
}
