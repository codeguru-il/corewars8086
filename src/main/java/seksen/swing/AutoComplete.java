/*
 * AutoComplete.java
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
package seksen.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class AutoComplete extends JComboBox implements
		JComboBox.KeySelectionManager
{
	private String searchFor;

	private long lap;

	public class CBDocument extends PlainDocument {
		public void insertString(int offset, String str, AttributeSet a)
				throws BadLocationException
		{
			if (str == null) {
				return;
			}
			super.insertString(offset, str, a);
			if (!isPopupVisible() && str.length() != 0) {
				JTextField tf = (JTextField) getEditor().getEditorComponent();
				String text = tf.getText();
				ComboBoxModel aModel = getModel();
				String current;
				String best = null;

				for (int i = 0; i < aModel.getSize(); i++) {
					current = aModel.getElementAt(i).toString();
					if (current.toLowerCase().startsWith(
							text.toLowerCase()) &&
						(best == null || best.length() > current.length()))
					{
						best = current;
					}
				}

				if( best != null ) {
					tf.setText(best);
					tf.setSelectionStart(text.length());
					tf.setSelectionEnd(best.length());
				}
			}
		}
	}

	public void actionPerformed(ActionEvent evt)
	{
		String command = (String) getSelectedItem();
		if( command.length() > 0 ) {
			removeItem(command);
			insertItemAt(command,0);
			setSelectedItem(command);
		}
		super.actionPerformed(evt);
	}

	public AutoComplete(Object[] items) {
		super(items);
		init();
	}

	public AutoComplete() {
		super();
		init();
	}

	private void init() {
		lap = new java.util.Date().getTime();
		setKeySelectionManager(this);
		JTextField tf;
		if (getEditor() != null) {
			tf = (JTextField) getEditor().getEditorComponent();
			if (tf != null) {
				tf.setDocument(new CBDocument());
			}
		}
	}

	public int selectionForKey(char aKey, ComboBoxModel aModel) {
		long now = new java.util.Date().getTime();
		if (searchFor != null && aKey == KeyEvent.VK_BACK_SPACE
				&& searchFor.length() > 0)
		{
			searchFor = searchFor.substring(0, searchFor.length() - 1);
		} else {
			// System.out.println(lap);
			// Kam nie hier vorbei.
			if (lap + 1000 < now)
				searchFor = "" + aKey;
			else
				searchFor = searchFor + aKey;
		}
		lap = now;
		String current;
		for (int i = 0; i < aModel.getSize(); i++) {
			current = aModel.getElementAt(i).toString().toLowerCase();
			if (current.toLowerCase().startsWith(searchFor.toLowerCase()))
				return i;
		}
		return -1;
	}
}
