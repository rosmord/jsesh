package test;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.graphics.vectorClipboard.PictureFormat;
import org.qenherkhopeshef.graphics.vectorClipboard.SimpleClipGraphics;

/**
 * Quick and dirty Demo of JVectClipboard capabilities.
 * 
 * See the performCopy method for an example of clipboard vector copy.
 * 
 * @author rosmord
 * 
 */
public class JVectClipboardTest {

	private static final String DIRECT_EMF_WINDOWS_ONLY = "Direct EMF (Windows-only?). Should work with powerpoint, for instance.";
	private static final String STANDALONE_MACPICT = "Stand-alone Mac Pict (Mac only, very experimental- read doesn't work well at all)";
	private static final String PDF = "PDF picture (Mac only, works well with native mac applications (Mellel, word 2008)). doesn't work with ports.";

	private JComboBox box;

	public JVectClipboardTest() {
		JFrame frame = new JFrame();
		Container contentPanel= frame.getContentPane();
		frame.getContentPane().setLayout(
				new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
		JButton button = new JButton("copy");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performCopy();
			}
		});
		contentPanel.add(button);
		box = new JComboBox(new String[] { "MACPICT", "WMF", "EMF",
				DIRECT_EMF_WINDOWS_ONLY, STANDALONE_MACPICT ,PDF});
		contentPanel.add(box);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public void performCopy() {
		SimpleClipGraphics clipGraphics = new SimpleClipGraphics(300, 200);
		if ("WMF".equals(box.getSelectedItem())) {
			clipGraphics.setPictureFormat(PictureFormat.WMF);
		} else if ("EMF".equals(box.getSelectedItem())) {
			clipGraphics.setPictureFormat(PictureFormat.EMF);
		} else if (DIRECT_EMF_WINDOWS_ONLY.equals(box.getSelectedItem())) {
			clipGraphics.setPictureFormat(PictureFormat.DIRECT_EMF);
		} else if (STANDALONE_MACPICT.equals(box.getSelectedItem())) {
			clipGraphics.setPictureFormat(PictureFormat.DIRECT_PICT);
		} else if ("MACPICT".equals(box.getSelectedItem())){
			clipGraphics.setPictureFormat(PictureFormat.MACPICT);
		} else {
			clipGraphics.setPictureFormat(PictureFormat.PDF);
		}
		Graphics2D g = clipGraphics.getGraphics();
		g.setColor(Color.RED);
		g.fill(new Rectangle2D.Double(30, 30, 100, 100));
		java.awt.Font f = new java.awt.Font("Serif", java.awt.Font.PLAIN, 12);
		g.setFont(f);
		String test= "just a test";
		float x = (float) g.getFontMetrics().stringWidth(test);
		g.drawString(test, 5, 30);
		g.drawString(test, 5+x, 30);	
		g.dispose();
		clipGraphics.copyToClipboard();
	}

	public static void main(String[] args) throws InterruptedException,
			InvocationTargetException {
		System.out.println("Running test");
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				new JVectClipboardTest();
			}
		});
	}
}
