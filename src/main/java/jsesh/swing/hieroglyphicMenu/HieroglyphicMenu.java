/*
 * Created on 27 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.swing.hieroglyphicMenu;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;
import jsesh.hieroglyphs.ManuelDeCodage;

import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * A menu that displays hieroglyphs.
 * 
 * @author S. Rosmorduc
 * 
 */
public class HieroglyphicMenu extends JMenu {

	/**
	 * pseudo family for tall narrow signs.
	 */
	public static String TALL_NARROW = "TALL NARROW";

	/**
	 * pseudo family for low broad signs.
	 */
	public static String LOW_BROAD = "LOW BROAD";

	/**
	 * pseudo family for low narrow signs.
	 */
	public static String LOW_NARROW = "LOW NARROW";
	/**
	 * 
	 */
	private static final long serialVersionUID = -88729059102316882L;

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

	private boolean menuFilled = false;

	public HieroglyphicMenu(String title, String family, int ncols) {
		super(title);
		this.ncols = ncols;
		this.family = family;
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.out.println("menu selected");
			}

		});

		// The popup system doesn't work on the mac if the menu is application
		// level.
		// In fact it's not very useful anymore, as menus are not really
		// supposed to change.
		// Anyway.
		// if (PlatformDetection.getPlatform() != PlatformDetection.MACOSX )) {

		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX
				&& "true".equals(System
						.getProperty("apple.laf.useScreenMenuBar"))) {
			fillMenu();
		} else {
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

	}

	private void fillMenu() {

		if (menuFilled)
			return;

		menuFilled = true;

		List codes;
		if (family.equals(TALL_NARROW)) {
			codes = ManuelDeCodage.getInstance().getTallNarrowSigns();
		} else if (family.equals(LOW_BROAD)) {
			codes = ManuelDeCodage.getInstance().getLowBroadSigns();
		} else if (family.equals(LOW_NARROW)) {
			codes = ManuelDeCodage.getInstance().getLowNarrowSigns();
		} else {
			codes = ManuelDeCodage.getInstance()
					.getBasicGardinerCodesForFamily(family);
		}

		CodeSelector menuEnter = new CodeSelector();
		JPopupMenu pm = getPopupMenu();
		pm.setLayout(new GridLayout(0, ncols));
		for (int i = 0; i < codes.size(); i++) {
			Action a = new HieroglyphAction((String) codes.get(i));
			JMenuItem jm = new JMenuItem(a);
			jm.addMouseListener(menuEnter);
			add(jm);
		}
	}

	private void flushMenu() {
		removeAll();
		menuFilled = false;
	}

	/**
	 * Sets the listener which will be warned about actions in this menu.
	 * 
	 * @param hieroglyphicMenuListener
	 */
	public void setHieroglyphicMenuListener(
			HieroglyphicMenuListener hieroglyphicMenuListener) {
		this.hieroglyphicMenuListener = hieroglyphicMenuListener;
	}

	class HieroglyphAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3149637635116611380L;

		String code;

		/**
		 * Build an action that can insert a given glyph.
		 * 
		 * @param code
		 * 
		 */
		public HieroglyphAction(String code) {
			super(code, HieroglyphicBitmapBuilder.createHieroglyphIcon(code,
					size, border, HieroglyphicMenu.this));
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
		 * @see
		 * java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
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
		 * @see
		 * java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			if (hieroglyphicMenuListener != null) {
				JMenuItem item = (JMenuItem) e.getSource();
				HieroglyphAction action = (HieroglyphAction) item.getAction();
				hieroglyphicMenuListener.exit(action.code);
			}
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void mouseDragged(MouseEvent e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void mouseMoved(MouseEvent e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

}