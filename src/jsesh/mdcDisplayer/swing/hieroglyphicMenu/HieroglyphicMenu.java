/*
 * Created on 27 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.hieroglyphicMenu;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;
import jsesh.hieroglyphs.HieroglyphicFontManager;
import jsesh.hieroglyphs.HieroglyphsManager;

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

	private String family;

	private HieroglyphicMenuListener hieroglyphicMenuListener = null;
	
	private boolean menuFilled= false;

	public HieroglyphicMenu(String title, String family, int ncols) {
		super(title);
		this.ncols = ncols;
		this.family= family;
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.out.println("menu selected");
			}
			
		});
		getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

			public void popupMenuCanceled(PopupMenuEvent e) {
				
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				fillMenu();
			}
			
		});
		addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent e) {
				flushMenu();
			}

			public void menuDeselected(MenuEvent e) {
				flushMenu();
			}

			public void menuSelected(MenuEvent e) {
			}

		});
	}

	private void fillMenu() {
	
			if (menuFilled)
				return;
			
			menuFilled= true;
			
			String codes[] = HieroglyphsManager.getInstance()
            .getCodesForFamily(family,true);

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
	
	private void flushMenu() {		
		removeAll();
		menuFilled= false;
	}

	/**
	 * Sets the listener which will be warned about actions in this menu.
	 * @param hieroglyphicMenuListener
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
	 * @return an icon for the glyph.
	 */
	public Icon createHieroglyphIcon(String code, int size, int border) {
		HieroglyphicFontManager manager = DefaultHieroglyphicFontManager
		.getInstance();
		jsesh.bzr.simple.ShapeChar glyph = manager.get(code);
		HieroglyphicBitmapBuilder builder= new HieroglyphicBitmapBuilder(20,30,false);
		builder.setBorder(2);
		builder.setFit(true);
		builder.setComponent(this);
		BufferedImage img = builder.buildSignBitmap(glyph);
		return new ImageIcon(img);
	}

	class HieroglyphAction extends AbstractAction {

		String code;

		/**
		 * Build an action that can insert a given glyph.
		 * @param code
		 * 
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