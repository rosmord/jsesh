package jsesh.utilitysoftwares.demos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsesh.graphics.glyphs.bzr.BzrFontReader;
import jsesh.graphics.glyphs.bzr.simple.BzrSimpleFont;
import jsesh.graphics.glyphs.bzr.simple.BzrSimpleFontBuilder;
import jsesh.hieroglyphs.graphics.ShapeChar;
import jsesh.swing.utils.GraphicsUtils;

/**
 * DemoFontDisplayer.java
 * 
 * 
 * Created: Fri Jun 7 12:49:16 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC
 *         </a>
 * 
 */

public class DemoFontDisplayer extends JFrame {
	BzrSimpleFont font;

	int pos;

	class Canvas extends JPanel {
		public Canvas() {
			setSize(getPreferredSize());
			Canvas.this.setBackground(Color.white);
		}

		public Dimension getPreferredSize() {
			return new Dimension(600, 600);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			if (font != null) {
				GraphicsUtils.antialias(g);

				g2d.scale(2.0, 2.0);

				int n = 0;
				for (int i = 0; i < 256; i++) {
					ShapeChar c = font.getChar(i);
					if (c != null) {
						g2d.fill(c.getShape());
						g2d.translate(20.0, 0.0);
						n++;
						if (n % 10 == 0)
							g2d.translate(-200.0, 20.0);
					}
				}
			}
		}
	}

	

	private void loadFont() {
		FileDialog file = new FileDialog(this, "choose a bzr font");
		file.setVisible(true);
		System.out.println(file.getFile());
		BzrSimpleFontBuilder builder = new BzrSimpleFontBuilder();
		BzrFontReader reader = new BzrFontReader(builder);
		try {
			reader.read(new FileInputStream(new File(file.getDirectory(), file
					.getFile())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		font = builder.getFont();
		pos = -1;
	}

	public DemoFontDisplayer() {
		super("Font Display");
		font = null;
		pos = 0;
		setSize(300, 300);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowOpened(WindowEvent e) {
			}
		});
		setJMenuBar(createMenu());
		getContentPane().add(new JScrollPane(new Canvas()));
	}

	public static void main(String[] args) {

		DemoFontDisplayer f = new DemoFontDisplayer();
		f.setVisible(true);
	}

	protected JMenuBar createMenu() {
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(new AbstractAction("Load") {
			public void actionPerformed(ActionEvent e) {
				loadFont();
			}
		});
		
		menu.add(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mb.add(menu);
		return mb;
	}
} // DemoFontDisplayer
