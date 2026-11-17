/*
 * Created on 27 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.hieroglyphicMenu;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import jsesh.bzr.simple.ShapeChar;
import jsesh.hieroglyphs.HieroglyphicFontManager;
import jsesh.utils.GraphicsUtils;

/**
 * A menu that displays hieroglyphs.
 * 
 * @author S. Rosmorduc
 *  
 */
public class HieroglyphicMenu extends JMenu {

	/**
	 * The dimensions of a basic icon
	 */
	private int size = 30;

	/**
	 * the dimensions of the margin around the glyphs.
	 */
	private int border = 2;

	private int ncols;

	private String codes[];

	private HieroglyphicMenuListener hieroglyphicMenuListener = null;

	public HieroglyphicMenu(String title, String codes[], int ncols) {
		super(title);
		this.ncols = ncols;
		this.codes = codes;
		addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
			}

			public void menuSelected(MenuEvent e) {
				fillMenu();
			}

		});
	}

	private void fillMenu() {
		if (codes != null) {
			CodeSelector menuEnter = new CodeSelector();
			JPopupMenu pm = getPopupMenu();
			pm.setLayout(new GridLayout(0, ncols));
			for (int i = 0; i < codes.length; i++) {
				Action a = new HieroglyphAction(codes[i]);
				JMenuItem jm = new JMenuItem(a);
				jm.addMouseListener(menuEnter);
				add(jm);
			}
		}
		codes = null;
	}

	/**
	 * Sets the listener which will be warned about actions in this menu.
	 */
	public void setHieroglyphicMenuListener(
			HieroglyphicMenuListener hieroglyphicMenuListener) {
		this.hieroglyphicMenuListener = hieroglyphicMenuListener;
	}
	
	/**
	 * Creates an icon for a particular hieroglyph.
	 * 
	 * @param code
	 * @param size :
	 *            the icon size
	 * @param border :
	 *            the size of the icon's border
	 * @return
	 */
	public Icon createHieroglyphIcon(String code, int size, int border) {
		HieroglyphicFontManager manager = HieroglyphicFontManager
				.getInstance();
		ShapeChar glyph = manager.get(code);
		ShapeChar A1 = manager.get("A1");
		// NOTE : creating images with new BufferedImage(...) is supposed not to
		// be the best way
		// methods like createCompatibleImage()... are in theory more efficient
		// because they create images that are both accelerated and can be
		// directly drawn into the target
		// device. However, it's a bit hard to get a graphicConfiguration object
		// in a clean way
		// and this leads to timing problems. Besides jdk1.5 tends to accelerate
		// all images.
		// Anyway, we will change our code only if it appears to be too slow.

		// Compute the scale
		float height = size - 2 * border;
		float scale = (float) (height / A1.getBbox().getHeight());
		BufferedImage img = new BufferedImage(size, size,
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = img.createGraphics();
		GraphicsUtils.antialias(g);
		g.setBackground(Color.WHITE);
		g.setColor(Color.BLACK);
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		glyph.draw(g, border, border, scale, scale, 0);
		g.dispose();
		return new ImageIcon(img);
	}

	class HieroglyphAction extends AbstractAction {

		String code;

		/**
		 * @param name
		 * @param icon
		 */
		public HieroglyphAction(String code) {
			super(code, createHieroglyphIcon(code, size, border));
			this.code = code;
		}

		public void actionPerformed(ActionEvent e) {
			if (hieroglyphicMenuListener != null)
				hieroglyphicMenuListener.codeSelected(code);
		}
	}

	class CodeSelector extends MouseAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			if (hieroglyphicMenuListener != null) {
				JMenuItem item = (JMenuItem) e.getSource();
				HieroglyphAction action = (HieroglyphAction) item.getAction();
				hieroglyphicMenuListener.enter(action.code);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			if (hieroglyphicMenuListener != null) {
				JMenuItem item = (JMenuItem) e.getSource();
				HieroglyphAction action = (HieroglyphAction) item.getAction();
				hieroglyphicMenuListener.exit(action.code);
			}
		}
	}

}