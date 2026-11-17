package jsesh.mdcDisplayer.swing.palette;

import javax.swing.JDialog;

/**
 * A Dialogue for choosing hieroglyphs
 * @author rosmord
 *
 */
public class HieroglyphicPalette extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2799941055125716967L;
	
	HieroglyphPaletteListener hieroglyphPaletteListener;
	
	
	public void setHieroglyphPaletteListener(HieroglyphPaletteListener hieroglyphPaletteListener) {
		this.hieroglyphPaletteListener= hieroglyphPaletteListener;
	}
	
}
