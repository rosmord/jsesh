/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 3 juin 2005
 *
 */
package jsesh.utilitysoftwares.demos;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.qenherkhopeshef.swingUtils.GraphicsUtils;

import com.lowagie.text.DocumentException;

/**
 * Simple test application for displaying external fonts.
 * 
 * @author Serge Rosmorduc
 */
public class SimpleExternalFontDisplayer extends JFrame {

	JTable fontDisplay;

	ExternalFontTableModel model;

	/**
	 * 
	 */
	public SimpleExternalFontDisplayer() {
		super("Simple font displayer");
		model = new ExternalFontTableModel();
		fontDisplay = new JTable(model);
		fontDisplay.setRowHeight(100);
		getContentPane().add(new JScrollPane(fontDisplay));
		pack();
		buildMenu();
	}

	/**
	 * 
	 */
	private void buildMenu() {
		JMenuBar bar = new JMenuBar();
		JMenu menu;

		menu = new JMenu("File");
		menu.add(new AbstractAction("Open") {

			public void actionPerformed(ActionEvent e) {
				JFileChooser choose = new JFileChooser(".");
				if (choose.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						loadFont(choose.getSelectedFile().getCanonicalPath());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "can't open "
								+ choose.getSelectedFile(), "Error",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		menu.add(new AbstractAction("Load system font") {

			public void actionPerformed(ActionEvent arg0) {
				loadSystemFont();
			}
		});
		bar.add(menu);

		setJMenuBar(bar);
	}

	private void loadFont(String fileName) throws IOException,
			DocumentException, FontFormatException {
		InputStream in = new FileInputStream(fileName);
		Font f = Font.createFont(Font.TRUETYPE_FONT, in);
		f = f.deriveFont(48f);
		in.close();
		setDisplayedFont(f);
	}

	private void setDisplayedFont(Font f) {
		model.clear();
		int max = Character.MAX_VALUE;
		for (int i = 0; i <= max; i++) {
			if (f.canDisplay((char) i)) {
				model.add(i, buildImage(f, i), "");
			}
		}
		fontDisplay.revalidate();
	}

	private void loadSystemFont() {
		List availableFonts = getAvailableFonts();
		Font result = (Font) JOptionPane.showInputDialog(null,
				"Choose a font", "Font selector", JOptionPane.QUESTION_MESSAGE,
				null, availableFonts.toArray(), null);
		if (result != null) {
			Font f = result.deriveFont(48f);
			setDisplayedFont(f);
		}
	}

	private List getAvailableFonts() {
		ArrayList result = new ArrayList();
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		Font[] fonts = ge.getAllFonts();
		int j = fonts.length;
		for (int i = 0; i < j; i++) {
			result.add(fonts[i]);
		}
		return result;
	}

	/**
	 * @param f
	 * @param i
	 * @return an imageIcon
	 */
	private ImageIcon buildImage(Font f, int i) {
		BufferedImage img = new BufferedImage(100, 100,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		GraphicsUtils.antialias(g);
		g.setFont(f);
		g.setBackground(Color.WHITE);
		g.setColor(Color.BLACK);
		g.clearRect(0, 0, 100, 100);
		g.drawString("" + (char) i, 10, 40);
		g.dispose();
		ImageIcon result = new ImageIcon(img);
		return result;
	}

	public static void main(String[] args) {
		SimpleExternalFontDisplayer disp = new SimpleExternalFontDisplayer();
		disp.setVisible(true);
		disp.addWindowListener(new WindowAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}
