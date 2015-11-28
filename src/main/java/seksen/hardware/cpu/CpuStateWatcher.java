/*
 * CpuStateWatcher.java
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
package seksen.hardware.cpu;

public class CpuStateWatcher extends CpuState {
	CpuStateListener listener;

	void setListener(CpuStateListener listener){
		this.listener = listener;
	}

	public void setSS(int value) {
		listener.registerChanged(CpuStateListener.INDEX_SS);
		super.setSS(value);
	}
}
