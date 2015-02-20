/*
 * MemoryAccessGraph.java
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import seksen.hardware.Address;
import seksen.hardware.memory.MemoryAccessListener;
import seksen.hardware.memory.MemoryException;
import seksen.util.Hex;

public class MemoryAccessGraph extends JFrame implements MemoryAccessListener, ActionListener {
	private CpuFrame cpuFrame;
	private int[] accessCounters;
	private int refresh=0;
	private int maxAccess = 1;
	private AccessGraph accessGraph;

	public MemoryAccessGraph(CpuFrame cpuFrame) {
		super("Memory Access Graphic");
		this.cpuFrame = cpuFrame;
		accessCounters = new int[cpuFrame.machine.memory.getMaxAddr()/16];
		cpuFrame.machine.memory.addAccessListener(this);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		{
			Box box = new Box(BoxLayout.X_AXIS);
			JButton button = new JButton("H/2");
			button.addActionListener(this);
			box.add(button);
			button = new JButton("FitH");
			button.addActionListener(this);
			box.add(button);
			button = new JButton("H*2");
			button.addActionListener(this);
			box.add(button);
			button = new JButton("V/2");
			button.addActionListener(this);
			box.add(button);
			button = new JButton("FitV");
			button.addActionListener(this);
			box.add(button);
			button = new JButton("V*2");
			button.addActionListener(this);
			box.add(button);
			box.add(Box.createHorizontalGlue());
			button = new JButton("Clear");
			button.addActionListener(this);
			box.add(button);
			getContentPane().add(box,BorderLayout.NORTH);
		}

		accessGraph = new AccessGraph();
		JScrollPane scrollPane = new JScrollPane(accessGraph);
		scrollPane.getVerticalScrollBar().setUnitIncrement(0x1000);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(100);
		getContentPane().add(scrollPane);

		setSize(500, 200);
		setVisible(true);
	}

	@Override
	public void dispose() {
		cpuFrame.machine.memory.removeAccessListener(this);
		super.dispose();
	}

	public void readExecuteMemory(Address address, int size) throws MemoryException {
		increaseCounter(address, size);
	}

	public void readMemory(Address address, int size) throws MemoryException {
		increaseCounter(address, size);
	}

	public void writeMemory(Address address, int size) throws MemoryException {
		increaseCounter(address, size);
	}

	private void increaseCounter(Address address, int size){
		int adr = address.getLinearAddress();
		for(int a=0; a<size; a++){
			int slot = adr/16;
			accessCounters[slot]++;
			if(accessCounters[slot]>maxAccess){
				maxAccess = accessCounters[slot];
			}
			adr++;
		}
		refresh++;
		if((refresh&0xfff) == 0){
			accessGraph.repaint();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Dimension prefSize = accessGraph.getPreferredSize();
		JViewport viewport = ((JViewport)accessGraph.getParent());
		JScrollBar scrollbarh = ((JScrollPane)viewport.getParent()).getHorizontalScrollBar();
		int valueh = scrollbarh.getValue();
		JScrollBar scrollbarv = ((JScrollPane)viewport.getParent()).getVerticalScrollBar();
		int valuev = scrollbarv.getValue();

		if(command.equals("V/2")){
			prefSize.height /= 2;
			valuev /= 2;
		} else if(command.equals("FitV")){
			prefSize.height = getHeight();
		} else if(command.equals("V*2")){
			prefSize.height *= 2;
			valuev *= 2;
		} else if(command.equals("H/2")){
			prefSize.width /= 2;
			valueh /= 2;
		} else if(command.equals("FitH")){
			prefSize.width = getWidth();
		} else if(command.equals("H*2")){
			prefSize.width *= 2;
			valueh = valueh*2+viewport.getViewSize().height/2;
		} else if(command.equals("Clear")){
			Arrays.fill(accessCounters, 0);
			maxAccess = 0;
		}

		accessGraph.setPreferredSize(prefSize);
		accessGraph.revalidate();
		accessGraph.repaint();
		scrollbarh.setValue(valueh);
		scrollbarv.setValue(valuev);
	}

	class AccessGraph extends JComponent {

		public AccessGraph() {
			setToolTipText("");
		}

		public String getToolTipText(MouseEvent event) {
			int incx = accessCounters.length/getWidth();
			if(incx<1){
				incx = 1;
			}

			int index = event.getX()*incx;
			int max = 0;
			int maxindex=index;
			for(int b=0;b<incx;b++,index++){
				if(accessCounters[index]>max){
					max = accessCounters[index];
					maxindex = index;
				}
			}
			return Hex.toHexString(maxindex*16, 6)+' '+max;
		}

		public void setPreferredSize(Dimension preferredSize) {
			if(preferredSize.height > maxAccess ){
				preferredSize.height = maxAccess;
			}
			if(preferredSize.width > accessCounters.length ){
				preferredSize.width = accessCounters.length;
			}
			super.setPreferredSize(preferredSize);
		}

		public void paint(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.RED);

			int width = getWidth();
			int height = getHeight();

			int incx = accessCounters.length/width;
			if(incx<1){
				incx = 1;
			}

			int incy = maxAccess/height;
			if(incy<1){
				incy = 1;
			}

			Rectangle region = g.getClipBounds();
			int index = region.x*incx;
			int end = region.x+region.width;

			try{
				for(int a=region.x;a<end;a++){
					int max = 0;
					for(int b=0;b<incx;b++,index++){
						if(accessCounters[index]>max){
							max = accessCounters[index];
						}
					}
					if(max > incy){
						int h = max/incy;
						g.fillRect(a, height-h, 1, h);
					}
				}
			} catch(ArrayIndexOutOfBoundsException e){
			}
		}
	}
}
