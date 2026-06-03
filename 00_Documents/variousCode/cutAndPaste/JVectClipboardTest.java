package jsesh.test.cutAndPaste;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.graphics.vectorClipboard.PictureFormat;
import org.qenherkhopeshef.graphics.vectorClipboard.SimpleClipGraphics;

/**
 * Quick and dirty Demo of JVectClipboard capabilities.
 *
 * See the performCopy method for an example of clipboard vector copy.
 * @author rosmord
 * 
 */
public class JVectClipboardTest {

	private JComboBox box;

	public JVectClipboardTest() {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(
				new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
		JButton button = new JButton("copy");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performCopy();
			}
		});
		frame.add(button);
		box = new JComboBox(new String[] {"MACPICT", "WMF", "EMF"});
		frame.add(box);
		frame.pack();
		frame.setVisible(true);

	}

	public void performCopy() {
		SimpleClipGraphics clipGraphics = new SimpleClipGraphics(300, 200);
		if ("WMF".equals(box.getSelectedItem())) {
			clipGraphics.setPictureFormat(PictureFormat.WMF);
		} else if ("EMF".equals(box.getSelectedItem())) {
			clipGraphics.setPictureFormat(PictureFormat.EMF);			
		} else {
			clipGraphics.setPictureFormat(PictureFormat.MACPICT);
		}
 		Graphics2D g = clipGraphics.getGraphics();
		g.setColor(Color.RED);
		g.fill(new Rectangle2D.Double(30, 30, 100, 100));
		g.dispose();
		clipGraphics.copyToClipboard();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new JVectClipboardTest();
			}
		});
	}
}
