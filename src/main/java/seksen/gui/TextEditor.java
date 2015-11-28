/*
 * TextEditor.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Erdem Guven
 */
public class TextEditor extends TextViewer implements ActionListener, MouseListener {

	protected JPopupMenu popup;

	public TextEditor() {
		text_area.setEditable(true);

		{
			popup = new JPopupMenu();

			// Create and add a menu item
			JMenuItem item = new JMenuItem("Save File");
			item.addActionListener(this);
			popup.add(item);

			item = new JMenuItem("Reload File");
			item.addActionListener(this);
			popup.add(item);
		}

		filePath.addMouseListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("Save File")) {
			FileWriter fw = null;
			try {
				fw = new FileWriter(loadedFile);
				text_area.write(fw);
				fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else if (command.equals("Reload File")) {
			loadedFile = null;
			try {
				loadFile(filePath.getText());
			} catch (IOException ex) {
				Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (loadedFile != null && e.isPopupTrigger()) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
