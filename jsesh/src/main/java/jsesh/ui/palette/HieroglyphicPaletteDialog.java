package jsesh.ui.palette;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import jsesh.utils.preferences.JSeshPreferencesRoot;

/**
 * A full palette display, with glyph informations. Should be integrated and
 * created by PalettePresenter.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */

@SuppressWarnings("serial")
public class HieroglyphicPaletteDialog extends JFrame {

	/// Maximum height of the palette, in pixels.
	private static final int DEFAULT_MAX_HEIGHT = 800;

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
		Rectangle saved = new Rectangle(x, y, width, height);
		if (width > 100 && height > 100 && isOnSomeScreen(saved)) {
			setLocation(x, y);
			setSize(width, height);
		} else {
			// pack() gives the full height of the sign list, which can be
			// huge (and crashes some compositors, e.g. WSLg).
			setLocationByPlatform(true);
		}
		limitSizeToScreen();
		setVisible(visible);
	}

	/// Checks that the rectangle intersects at least one screen (any monitor,
	/// including those with negative or large coordinates).
	private boolean isOnSomeScreen(Rectangle r) {
		for (GraphicsDevice device : GraphicsEnvironment
				.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (device.getDefaultConfiguration().getBounds().intersects(r))
				return true;
		}
		return false;
	}

	/// Makes sure the palette is never larger than the usable screen area.
	private void limitSizeToScreen() {
		Rectangle max = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();
		int w = Math.min(getWidth(), max.width);
		int h = Math.min(getHeight(), Math.min(max.height, DEFAULT_MAX_HEIGHT));
		setSize(w, h);
	}

	private Preferences getPreferences() {
		return JSeshPreferencesRoot.getPreferences();
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
