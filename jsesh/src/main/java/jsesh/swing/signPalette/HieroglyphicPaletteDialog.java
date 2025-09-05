package jsesh.swing.signPalette;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

/**
 * A full palette display, with glyph informations. Should be integrated and
 * created by PalettePresenter.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */

@SuppressWarnings("serial")
public class HieroglyphicPaletteDialog extends JFrame {

	private PalettePresenter presenter;

	public HieroglyphicPaletteDialog(PalettePresenter presenter) {
		super("Hieroglyphic Palette");
		this.presenter = presenter;
		getContentPane().add(presenter.createComplexPalette());
		setAlwaysOnTop(false);
		pack();
		initFromPreferences();
	}

	private void initFromPreferences() {
		Preferences prefs = getPreferences();
		int x = prefs.getInt("hieroglyphicPalette.x", 0);
		int y = prefs.getInt("hieroglyphicPalette.y", 0);
		int width = prefs.getInt("hieroglyphicPalette.width", 0);
		int height = prefs.getInt("hieroglyphicPalette.height", 0);
		boolean visible = prefs.getBoolean("hieroglyphicPalette.visible", true);
		boolean reasonableDefault = true;
		if (x <= 0 || y <= 0 || width <= 100 || height <= 100)
			reasonableDefault = false;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (x+width >= screenSize.width || y + height >= screenSize.height)
			reasonableDefault= false;
		if (reasonableDefault) {
			setLocation(x, y);
			setSize(width, height);
		}
		setVisible(visible);
	}

	private Preferences getPreferences() {
		return Preferences.userNodeForPackage(this.getClass());
	}

	public void setHieroglyphPaletteListener(
			HieroglyphPaletteListener hieroglyphPaletteListener) {
		presenter.setHieroglyphPaletteListener(hieroglyphPaletteListener);
	}

	/**
	 * Saves current position and the like in preferences.
	 */
	public void savePreferences() {
		Preferences prefs = getPreferences();
		prefs.putInt("hieroglyphicPalette.x", getLocation().x);
		prefs.putInt("hieroglyphicPalette.y", getLocation().y);
		prefs.putInt("hieroglyphicPalette.width", getWidth());
		prefs.putInt("hieroglyphicPalette.height", getHeight());
		prefs.putBoolean("hieroglyphicPalette.visible", isVisible());
	}
}
