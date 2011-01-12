/*
 * Created on 29 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.swing.hieroglyphicMenu;

/**
 * When a hieroglyph is selected in a menu, the corresponding HieroglyphicMenuListener is called.
 * @author S. Rosmorduc
 *
 */
public interface HieroglyphicMenuListener {
	
	/**
	 * Called when a hieroglyph is selected in the menu.
	 * @param code the hieroglyph's code
	 */
	void codeSelected(String code);

	/**
	 * Called when the mouse enter a hieroglyph's zone in the menu.
	 * @param code the hieroglyph's code
	 */
	void enter(String code);

	/**
	 * Called when the mouse exits a hieroglyph's zone in the menu.
	 * @param code the hieroglyph's code
	 */
	void exit(String code);
}
