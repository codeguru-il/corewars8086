/*
 * MMap.java
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

/**
 *
 * @author Erdem Guven
 */
public class MMap {
	static {
		System.loadLibrary("seksen");
	}

	/* Protections are chosen from these bits, OR'd together.  The
	   implementation does not necessarily support PROT_EXEC or PROT_WRITE
	   without PROT_READ.  The only guarantees are that no writing will be
	   allowed without PROT_WRITE and no access will be allowed for PROT_NONE. */

	public static final int PROT_READ	=0x1;		/* Page can be read.  */
	public static final int PROT_WRITE	=0x2;		/* Page can be written.  */
	public static final int PROT_EXEC	=0x4;		/* Page can be executed.  */
	public static final int PROT_NONE	=0x0;		/* Page can not be accessed.  */
	public static final int PROT_GROWSDOWN	=0x01000000;	/* Extend change to start of
						   growsdown vma (mprotect only).  */
	public static final int PROT_GROWSUP	=0x02000000;	/* Extend change to start of
						   growsup vma (mprotect only).  */

	/* Sharing types (must choose one and only one of these).  */
	public static final int MAP_SHARED	=0x01;		/* Share changes.  */
	public static final int MAP_PRIVATE	=0x02;		/* Changes are private.  */

	/* Other flags.  */
	public static final int MAP_FIXED	=0x10;		/* Interpret addr exactly.  */
	public static final int MAP_FILE	=0;
	public static final int MAP_ANONYMOUS	=0x20;		/* Don't use a file.  */
	public static final int MAP_ANON	=MAP_ANONYMOUS;

	/**
	 * Calls mmap.
	 */
	public native static long mmap(long addr, int len, int prot, int flags, int fd, long off);
}
