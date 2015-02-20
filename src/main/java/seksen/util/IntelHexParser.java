/*
 * IntelHexParser.java
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
package seksen.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class IntelHexParser {

	/*
    Each line of Intel HEX file consists of six parts:

    1. Start code, one character, an ASCII colon ':'.
    2. Byte count, two hex digits, a number of bytes (hex digit pairs) in the data field. 16 (0x10) or 32 (0x20) bytes of data are the usual compromise values between line length and address overhead.
    3. Address, four hex digits, a 16-bit address of the beginning of the memory position for the data. Limited to 64 kilobytes, the limit is worked around by specifying higher bits via additional record types. This address is big endian.
    4. Record type, two hex digits, 00 to 05, defining the type of the data field.
    5. Data, a sequence of n bytes of the data themselves. 2n hex digits.
    6. Checksum, two hex digits - the least significant byte of the two's complement sum of the values of all fields except fields 1 and 6. It is calculated by adding together the hex-encoded bytes (hex digit pairs), taking only the LSB, and either subtracting the byte from 0x100 or inverting the bytes (XORing 0xFF) and adding one (1). If the checksum is correctly calculated, adding all the bytes (the Byte count, both bytes in Address, the Record type, each Data byte and the Checksum) together will always result in a value wherein the least significant byte is zero (0x00).

    There are six record types:

    * 00, data record, contains data and 16-bit address. The format described above.
    * 01, End Of File record, a file termination record. No data. Has to be the last line of the file, only one per file permitted. Usually ':00000001FF'.
    * 02, Extended Segment Address Record, segment-base address. Used when 16 bits is not enough, identical to 80x86 real mode addressing. The address specified by the 02 record is multiplied by 16 (shifted 4 bits left) and added to the subsequent 00 record addresses. This allows addressing of up to a megabyte of address space. The address field of this record has to be 0000, the byte count is 02 (the segment is 16-bit). The least significant hex digit of the segment address is always 0.
    * 03, Start Segment Address Record. For 80x86 processors, it specifies the initial content of the CS:IP registers. The address field is 0000, the byte count is 04, the first two bytes are the CS value, the latter two are the IP value.
    * 04, Extended Linear Address Record, allowing for fully 32 bit addressing. The address field is 0000, the byte count is 02. The two data bytes represent the upper 16 bits of the 32 bit address, when combined with the address of the 00 type record.
    * 05, Start Linear Address Record. The address field is 0000, the byte count is 04. The 4 data bytes represent the 32-bit value loaded into the EIP register of the 80386 and higher CPU.
    */

	static public void parse(File file,
			IntelHexParseListener listener) throws Exception
	{
		BufferedReader rd = new BufferedReader(new FileReader(file));
		String line;
		int ext_addr = 0;

		while ((line = rd.readLine()) != null)
		{
			if (!line.startsWith(":"))
				throw new Exception(file+" is not a valid intel file");


			int lenData = Hex.getByte(line,1);
			int offset	= Hex.getWord(line,3);
			int type	= Hex.getByte(line,7);

			int chksum = lenData + (offset/256) + offset + type;

			for (int i = 0 ; i < lenData + 1; i++)
				chksum += Hex.getByte(line,9+i*2);
			chksum &= 0xff;

			if (chksum != 0)
				throw new Exception("Invalid chksum "+Hex.toHexString(chksum,2)+" in "+line);

            if (type == 1){
				break;
            } else if (type == 3){
                listener.startSegment(Hex.getWord(line,9),
                		Hex.getWord(line,13));
            } else if (type == 4){
            	ext_addr = Hex.getWord(line,9) << 16;
				continue;
			}

			if (type > 5)
				throw new Exception("Unsupported record type "+type+" : "+line);

			if (type != 0)
                continue;

			byte[] data = new byte[lenData];
			for (int i = 0 ; i < lenData ; i++){
				data[i] = (byte)Hex.getByte(line, 9+i*2);
			}

			listener.data((ext_addr | offset), data);
		}
	}
}
