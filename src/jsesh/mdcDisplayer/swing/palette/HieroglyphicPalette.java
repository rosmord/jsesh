package jsesh.mdcDisplayer.swing.palette;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A Dialogue for choosing hieroglyphs
 * @author rosmord
 * @deprecated
 */
public class HieroglyphicPalette {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2799941055125716967L;
	
	HieroglyphPaletteListener hieroglyphPaletteListener;
	
	JPanel panel;
	public HieroglyphicPalette() {
		panel= new JPanel();
		panel.add(new JTextField(10));
	}
	
	public void setHieroglyphPaletteListener(HieroglyphPaletteListener hieroglyphPaletteListener) {
		this.hieroglyphPaletteListener= hieroglyphPaletteListener;
	}
	
	public JPanel getPanel() {
		return panel;
	}
}
