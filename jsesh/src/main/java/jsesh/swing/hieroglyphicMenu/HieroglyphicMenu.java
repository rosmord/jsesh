/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * Created on 27 oct. 2004
 */
package jsesh.swing.hieroglyphicMenu;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import jsesh.hieroglyphs.data.ManuelDeCodage;
import jsesh.swing.utils.ImageIconFactory;

import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * A menu that displays hieroglyphs.
 * 
 * @author S. Rosmorduc
 * 
 */
@SuppressWarnings("serial")
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

		List<String> codes;
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
			Action a = new HieroglyphAction(codes.get(i));
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
		String code;

		/**
		 * Build an action that can insert a given glyph.
		 * 
		 * @param code
		 * 
		 */
		public HieroglyphAction(String code) {
			super(code, ImageIconFactory.getInstance().buildGlyphImage(code));
			/*super(code, HieroglyphicBitmapBuilder.createHieroglyphIcon(code,
					size, border, HieroglyphicMenu.this));*/		
			this.code = code;
		}

		public void actionPerformed(ActionEvent e) {
			if (hieroglyphicMenuListener != null)
				hieroglyphicMenuListener.codeSelected(code);
		}
	}

	class CodeSelector extends MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			if (hieroglyphicMenuListener != null) {
				JMenuItem item = (JMenuItem) e.getSource();
				HieroglyphAction action = (HieroglyphAction) item.getAction();
				hieroglyphicMenuListener.enter(action.code);
			}
		}

		public void mouseExited(MouseEvent e) {
			if (hieroglyphicMenuListener != null) {
				JMenuItem item = (JMenuItem) e.getSource();
				HieroglyphAction action = (HieroglyphAction) item.getAction();
				hieroglyphicMenuListener.exit(action.code);
			}
		}
	}
}