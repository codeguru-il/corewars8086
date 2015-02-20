/*
 * CpuRunner.java
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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import seksen.hardware.Address;
import seksen.hardware.Machine;
import seksen.hardware.memory.MemoryException;

public class CpuRunner extends Thread {
	private Machine machine;
	private Writer logWriter;
	private Runnable postProcess;
	private boolean run;

	private static final int HISTORYLENGTH = 10;
	private Address[] history;
	private int historyPosition = 0;

	public int instCounter = 0;

	public CpuRunner(Machine machine, Writer logWriter, Runnable postProcess ) {
		setDaemon(true);

		this.machine = machine;
		this.logWriter = logWriter;
		this.postProcess = postProcess;

		history = new Address[HISTORYLENGTH];
		Arrays.fill(history, machine.newAddress(0));
	}

	public void run() {
		CpuState state = machine.state;
		run = true;

		try {
			try {
				while(run){
					machine.do_cycle();
					instCounter ++;

					Address adr = machine.newAddress(state.getCS(), state.getIP());
					history[historyPosition] = adr;
					historyPosition = (historyPosition+1)%HISTORYLENGTH;
				}
			} catch (CpuException e) {
				logWriter.write(e.toString()+'\n');
			} catch (MemoryException e) {
				logWriter.write(e.toString()+'\n');
			}
			logWriter.write("Processed inst. count: "+instCounter+'\n');
		} catch (IOException e) {
			e.printStackTrace();
		}

		postProcess.run();
	}

	public void stopCpu(){
		run = false;
	}

	public Address[] getHistory() {
		Address[] newhis = new Address[HISTORYLENGTH];
		for(int a=0; a<HISTORYLENGTH; a++){
			newhis[a] = history[(historyPosition+HISTORYLENGTH-1-a)%HISTORYLENGTH];
		}
		return newhis;
	}
}
