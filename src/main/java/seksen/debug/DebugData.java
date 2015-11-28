/*
 * DebugData.java
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

import seksen.hardware.Address;

public class DebugData {

    public SymbolList symbolList;
    public SourceList sourceList;
    public File sourceDir;

    public DebugData() {
        symbolList = new SymbolList();
        sourceList = new SourceList();
    }

    public void loadParadigmLocFile(Address an_addr, File file) throws IOException {
        LineNumberReader linereader = new LineNumberReader(new FileReader(file));
        String line;

        //Search symbol list header
        while (true) {
            line = linereader.readLine();
            if (line == null) {
                return;
            }
            if (line.startsWith("  Address         Publics by Address")) {
                break;
            }
        }
        linereader.readLine(); //skip the empty line

        ArrayList symbolarray = new ArrayList();

        //Read symbol data
        while (true) {
            line = linereader.readLine();
            if (line.length() == 0) { //end of list
                break;
            }

            int seg = Integer.parseInt(line.substring(1, 5), 16);
            int off = Integer.parseInt(line.substring(6, 10), 16);

            symbolarray.add(new Symbol(
                an_addr.newAddress(seg, off), line.substring(17)));
        }

        Symbol[] symbols = (Symbol[]) symbolarray.toArray(new Symbol[0]);
        symbolarray.clear();

        symbolList = new SymbolList(symbols);

        // Look for line numbers
        ArrayList sources = new ArrayList();

        while (true) {
            line = linereader.readLine();
            if (line == null) {
                break;
            }

            if (line.startsWith("   Line numbers from ")) {
                int a = line.indexOf(" (", 21);
                if (a < 0) {
                    continue;
                }

                String path;
                int segment;
                short offset;
                int line_num;
                ArrayList lines = new ArrayList();

                path = line.substring(21, a);
                path = lookForFile(path);
                segment = Integer.parseInt(line.substring(a + 10, a + 14), 16);
                /*System.out.println(
                "File: " + path +
                " segment: " + segment);*/


                while (true) {
                    line = linereader.readLine();
                    if (line.length() == 0) { //end of list
                        break;
                    }

                    int i = 0;
                    while (true) {
                        int d = line.indexOf(':', i);
                        if (d < 0) {
                            break;
                        }

                        int c = line.indexOf('h', d + 1);
                        if (c < 0) {
                            break;
                        }

                        line_num = Integer.parseInt(line.substring(i, d).trim());
                        line_num--;
                        offset = Short.parseShort(line.substring(d + 1, c), 16);
                        lines.add(new int[]{offset, line_num});

                        /*System.out.println(
                        "line: " + line_num +
                        " addr: " + offset );*/

                        i = c + 1;
                    }

                    if (i == 0) {
                        break;
                    }
                }

                sources.add(createSource(path, segment, lines));
            }
        }

        Source[] source_array = new Source[sources.size()];
        sources.toArray(source_array);

        sourceList.add(source_array);
    }

    protected Source createSource(String path, int segment, ArrayList lines) {
        int a = lines.size();
        short offsets[] = new short[a];
        int line_nums[] = new int[a];

        while (a > 0) {
            a--;
            int[] la = (int[]) lines.get(a);
            offsets[a] = (short) la[0];
            line_nums[a] = la[1];
        }

        return new Source(path, segment, offsets, line_nums);
    }

    protected String lookForFile(String file) {
        // Correct path separators
        if (File.separator.equals("/")) {
            file = file.replace('\\', '/');
        }

        { // If file name is in correct case
            File f = new File(file);
            if (f.exists()) {
                return file;
            }
        }

        if (sourceDir != null) {
            String path[] = file.split(File.separator);
            int pi = 0;

            File dir = sourceDir;
            while (dir != null && dir.isDirectory() && pi < path.length) {
                if (path[pi].equals("..")) {
                    dir = dir.getParentFile();
                    pi++;
                    continue;
                }

                String[] children = dir.list();
                for (int i = 0; true; i++) {
                    if (i >= children.length) {
                        dir = null;
                        break;
                    }
                    if (children[i].equalsIgnoreCase(path[pi])) {
                        dir = new File(dir, children[i]);
                        pi++;
                        break;
                    }
                }
            }

            if (dir != null && dir.isFile() && pi == path.length) {
                return dir.getAbsolutePath();
            }
        }

        return file;
    }

    public void setSourceDir(String dir) {
        File f = new File(dir);
        setSourceDir(f);
    }

    public void setSourceDir(File f) {
        if (f.exists() && f.isDirectory()) {
            sourceDir = f;
        }
    }

    public void loadIdaLstFile(Address an_addr, File file) throws IOException {
        LineNumberReader linereader = new LineNumberReader(new FileReader(file));

        int pseg = -1, poff = -1;
        ArrayList sources = new ArrayList();
        ArrayList lines = new ArrayList();

        while (true) {
            String line = linereader.readLine();

            int seg;
            int off;

            if (line == null) {
                // will break after last line added
                seg = -1;
                off = -1;
            } else {
				if(line.charAt(4) != ':') {
					continue;
				}
                seg = Integer.parseInt(line.substring(0, 4), 16);
                off = Integer.parseInt(line.substring(5, 9), 16);
            }

            if (pseg != -1) {
                if (pseg != seg) {
                    sources.add(createSource(file.getPath(), pseg, lines));
                    lines.clear();
                } else if (poff != off) {
                    lines.add(new int[]{poff, linereader.getLineNumber() - 2});
                }
            }

            if (line == null) {
                break;
            }
            pseg = seg;
            poff = off;
        }

        Source[] source_array = new Source[sources.size()];
        sources.toArray(source_array);

        sourceList.add(source_array);
    }
}
