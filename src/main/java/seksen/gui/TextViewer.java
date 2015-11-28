/*
 * TextViewer.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class TextViewer extends JPanel {
	static final Color HL_COLOR = Color.LIGHT_GRAY;

	protected JTextField filePath;
	protected JTextArea text_area;

	protected File loadedFile = null;

	private Highlighter.HighlightPainter painter;

	private File newFile = null;
	private int gotoLine = -1;

	public TextViewer() {
		super(new BorderLayout());

		filePath = new JTextField();
		filePath.setEditable(false);
		add(filePath, BorderLayout.NORTH);

		LineNumberPanel nr = new LineNumberPanel();
		nr.text_area.setEditable(false);
		add(nr, BorderLayout.WEST);
		add(nr.scrollPane, BorderLayout.CENTER);

		text_area = nr.text_area;

		painter = new DefaultHighlighter.DefaultHighlightPainter(HL_COLOR);
	}

	public boolean loadFile( String file ) throws IOException {
		File f = new File( file );
		if( f.exists() && f.isFile() && f.canRead() ) {
			if( !f.equals(loadedFile) ) {
				newFile = f;
				repaint();
			}
			return true;
		}
		return false;
	}

	public void gotoLine( int line ) {
		gotoLine = line;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		if(newFile != null) {
			File file = newFile;
			newFile = null;
			try {
				FileReader fr = new FileReader( file );
				text_area.read(fr, null);
				filePath.setText(file.getPath());
				loadedFile = file;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if( gotoLine >= 0 ) {
			try {
				int s = text_area.getLineStartOffset(gotoLine);
				int e = text_area.getLineEndOffset(gotoLine);

				text_area.setCaretPosition(s);

				Highlighter hl = text_area.getHighlighter();
				hl.removeAllHighlights();
				hl.addHighlight(s, e, painter);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			gotoLine = -1;
		}
		super.paint(g);
	}
}
