/*
 * VM86.java
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
package seksen.jni;

import seksen.hardware.cpu.CpuState;

public class VM86 {

	static {
		System.loadLibrary("seksen");
	}

	public static int mapMemory(int adr, int length) {
		return (int) MMap.mmap(adr, length,
			MMap.PROT_EXEC | MMap.PROT_READ | MMap.PROT_WRITE,
			MMap.MAP_ANON | MMap.MAP_PRIVATE | MMap.MAP_FIXED,
			0, 0);
	}

	public static native void writeMemory(int adr, byte[] data, int offset, int size);

	public static native void readMemory(int adr, byte[] data, int offset, int size);

	public static native void fillMemory(int adr, byte val, int size);

	public static native int vm86(int mode, int[] vm86plus_struct);
	public static final int TF_MASK = 0x00000100;
	public static final int IF_MASK = 0x00000200;
	public static final int IOPL_MASK = 0x00003000;
	public static final int NT_MASK = 0x00004000;
	public static final int VM_MASK = 0x00020000;
	public static final int AC_MASK = 0x00040000;
	public static final int VIF_MASK = 0x00080000;	/* virtual interrupt flag */

	public static final int VIP_MASK = 0x00100000;	/* virtual interrupt pending */

	public static final int ID_MASK = 0x00200000;
	public static final int BIOSSEG = 0x0f000;
	public static final int CPU_086 = 0;
	public static final int CPU_186 = 1;
	public static final int CPU_286 = 2;
	public static final int CPU_386 = 3;
	public static final int CPU_486 = 4;
	public static final int CPU_586 = 5;

	/*
	 * Return values for the 'vm86()' system call
	 */
	public static final int VM86_TYPE_MASK = 0xff;
	public static final int VM86_ARG_MASK = 0xff00;
	public static final int VM86_SIGNAL = 0;	/* return due to signal */

	public static final int VM86_UNKNOWN = 1;	/* unhandled GP fault - IO-instruction or similar */

	public static final int VM86_INTx = 2;	/* int3/int x instruction (ARG = x) */

	public static final int VM86_STI = 3;	/* sti/popf/iret instruction enabled virtual interrupts */

	/*
	 * Additional return values when invoking new vm86()
	 */
	public static final int VM86_PICRETURN = 4;	/* return due to pending PIC request */

	public static final int VM86_TRAP = 6;	/* return due to DOS-debugger request */

	/*
	 * function codes when invoking new vm86()
	 */
	public static final int VM86_PLUS_INSTALL_CHECK = 0;
	public static final int VM86_ENTER = 1;
	public static final int VM86_ENTER_NO_BYPASS = 2;
	public static final int VM86_REQUEST_IRQ = 3;
	public static final int VM86_FREE_IRQ = 4;
	public static final int VM86_GET_IRQ_BITS = 5;
	public static final int VM86_GET_AND_RESET_IRQ = 6;
	/*
	 * vm86plus_struct indexes
	 */
	private static final int INDEX_EBX = 0;
	private static final int INDEX_ECX = 1;
	private static final int INDEX_EDX = 2;
	private static final int INDEX_ESI = 3;
	private static final int INDEX_EDI = 4;
	private static final int INDEX_EBP = 5;
	private static final int INDEX_EAX = 6;
	private static final int INDEX___NULL_DS = 7;
	private static final int INDEX___NULL_ES = 8;
	private static final int INDEX___NULL_FS = 9;
	private static final int INDEX___NULL_GS = 10;
	private static final int INDEX_ORIG_EAX = 11;
	private static final int INDEX_EIP = 12;
	private static final int INDEX_CS = 13;
	private static final int INDEX_EFLAGS = 14;
	private static final int INDEX_ESP = 15;
	private static final int INDEX_SS = 16;
	private static final int INDEX_ES = 17;
	private static final int INDEX_DS = 18;
	private static final int INDEX_FS = 19;
	private static final int INDEX_GS = 20;
	private static final int INDEX_FLAGS = 21;
	private static final int INDEX_SCREEN_BITMAP = 22;
	private static final int INDEX_CPU_TYPE = 23;
	private static final int INDEX_INT_REVECTORED = 24;
	private static final int INDEX_INT21_REVECTORED = 32;
	private static final int INDEX_VM86PLUS = 40;

	public static int runVM80186(CpuState state) {
		int[] vm86struct = new int[49];

		vm86struct[INDEX_EAX] = state.getAX();
		vm86struct[INDEX_EBX] = state.getBX();
		vm86struct[INDEX_ECX] = state.getCX();
		vm86struct[INDEX_EDX] = state.getDX();
		vm86struct[INDEX_DS] = state.getDS();
		vm86struct[INDEX_ES] = state.getES();
		vm86struct[INDEX_ESI] = state.getSI();
		vm86struct[INDEX_EDI] = state.getDI();
		vm86struct[INDEX_SS] = state.getSS();
		vm86struct[INDEX_EBP] = state.getBP();
		vm86struct[INDEX_ESP] = state.getSP();
		vm86struct[INDEX_CS] = state.getCS();
		vm86struct[INDEX_EIP] = state.getIP();
		vm86struct[INDEX_EFLAGS] = state.getFlags();

		vm86struct[INDEX_CPU_TYPE] = CPU_186;

		for (int a = 0; a < 8; a++) {
			vm86struct[INDEX_INT_REVECTORED + a] = 0xffffffff;
		}

		int err = vm86(VM86_ENTER, vm86struct);

		state.setAX(vm86struct[INDEX_EAX]);
		state.setBX(vm86struct[INDEX_EBX]);
		state.setCX(vm86struct[INDEX_ECX]);
		state.setDX(vm86struct[INDEX_EDX]);
		state.setDS(vm86struct[INDEX_DS]);
		state.setES(vm86struct[INDEX_ES]);
		state.setSI(vm86struct[INDEX_ESI]);
		state.setDI(vm86struct[INDEX_EDI]);
		state.setSS(vm86struct[INDEX_SS]);
		state.setBP(vm86struct[INDEX_EBP]);
		state.setSP(vm86struct[INDEX_ESP]);
		state.setCS(vm86struct[INDEX_CS]);
		state.setIP(vm86struct[INDEX_EIP]);
		state.setFlags(vm86struct[INDEX_EFLAGS]);

		return err;
	}
}
