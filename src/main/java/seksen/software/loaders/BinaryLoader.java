/*
 * BinaryLoader.java
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
package seksen.software.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import seksen.hardware.Address;
import seksen.hardware.InterruptHandler;
import seksen.hardware.Machine;
import seksen.hardware.cpu.CpuState;
import seksen.hardware.memory.MemoryAccessProtection;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.MemoryVmCompare;
import seksen.hardware.memory.RealModeMemory;

public class BinaryLoader {

	public static void loadBin(Machine machine, File file, Address address) throws IOException {
		FileInputStream input = new FileInputStream(file);
		try {
			machine.memory.writeMemory(address,
					input,machine.address.getMaxAddr());
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}

	private static final int ELKS_COMBID = 0x04100301;
	private static final int ELKS_SPLITID = 0x04200301;

	/*
	struct elks_exec_hdr
	{
		unsigned long type;
		unsigned long hlen;
		unsigned long tseg;
		unsigned long dseg;
		unsigned long bseg;
		unsigned long unused;
		unsigned long chmem;
		unsigned long unused2;
	};
	 */

	public static void loadElks(Machine machine, File file) throws IOException {
		FileInputStream input = new FileInputStream(file);

		byte[] header = new byte[32];
		input.read(header);

		ByteBuffer buf = ByteBuffer.wrap(header);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int type = buf.getInt(0);
		int hlen = buf.getInt(4);
		int tlen = buf.getInt(8);
		int dlen = buf.getInt(12);
		int blen = buf.getInt(16);
		int slen = 0x10000;

		if(hlen != 32 || type != ELKS_COMBID){
			return; //TODO throw some exception
		}

		Address tstart;
		if(machine.addressType == Address.ADR20){
			tstart = machine.newAddress(0x1000, 0x0000);
		} else {
			tstart = machine.newAddress(0x4900, 0x0000);
		}
		Address dstart = tstart.addAddress(tlen);
		Address bstart = dstart.addAddress(dlen);
		Address sstart = bstart.addAddress(blen).normalize();
		sstart = machine.newAddress(sstart.getSegment()+1, 0);
		Address hstart = sstart.addAddress(slen);

		RealModeMemory memory = machine.memory;

		if (memory instanceof MemoryVmCompare) {
			((MemoryVmCompare)memory).setCopyWrite();
		}

		try {
			memory.writeMemory(tstart, input, tlen);
			memory.writeMemory(dstart, input, dlen);
			memory.fillMemory(bstart, (byte)0, blen);
		} catch (MemoryException e) {
			e.printStackTrace();
		}

		MemoryAccessProtection resmem = (MemoryAccessProtection)
			machine.getDevice(MemoryAccessProtection.class);
		if( resmem != null ){
			resmem.setProtection(tstart, tlen, MemoryAccessProtection.PROT_EXEC|
					MemoryAccessProtection.PROT_READ);
			resmem.setProtection(dstart, dlen,
					MemoryAccessProtection.PROT_READ_WRITE);
			resmem.setProtection(bstart, blen,
					MemoryAccessProtection.PROT_READ_WRITE);

			int length = 0xA00000 - sstart.getLinearAddress();
			resmem.setProtection(sstart, length,
					MemoryAccessProtection.PROT_READ_WRITE);
		}

		CpuState state = machine.state;

		state.setCS(tstart.getSegment());
		state.setIP(tstart.getOffset());

		state.setDS(dstart.getSegment());

		state.setSS(sstart.getSegment());
		state.setSP(0);

		state.setFlags(0);

		InterruptHandler ih = (InterruptHandler) machine.getDevice(InterruptHandler.class);
	}

	public static void loadIntelHex(Machine machine, File file) throws Exception {
		IntelHexLoader ihl = new IntelHexLoader(machine);
		ihl.load(file);
	}
}
