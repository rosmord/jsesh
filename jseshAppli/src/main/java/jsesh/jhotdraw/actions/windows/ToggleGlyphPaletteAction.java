package jsesh.jhotdraw.actions.windows;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.jhotdraw_7_6.app.action.ActionUtil;

@SuppressWarnings("serial")
public class ToggleGlyphPaletteAction extends AbstractApplicationAction {

	public static final String ID = "windows.toggleGlyphPalette";
	private JFrame palette;
	private Runnable doOnFirstDisplay;

	/**
	 * Create an action to display the hieroglyphic palette. The palette will be
	 * really created on its first display ?
	 * 
	 * @param app
	 * @param palette
	 * @param onFirstDisplay
	 *            (not used currently).
	 */
	public ToggleGlyphPaletteAction(Application app, JFrame palette,
			Runnable onFirstDisplay) {
		super(app);
		BundleHelper.getInstance().configure(this);
		this.palette = palette;
		this.doOnFirstDisplay = onFirstDisplay;
		palette.addWindowListener(new PaletteListener());
	}

	public void actionPerformed(ActionEvent e) {
		if (doOnFirstDisplay != null) {
			doOnFirstDisplay.run();
			doOnFirstDisplay = null;
		}
		if (palette.isVisible()) {
			palette.setVisible(false);
			putValue(ActionUtil.SELECTED_KEY, false);
		} else {
			palette.setVisible(true);
			putValue(ActionUtil.SELECTED_KEY, true);
		}
	}
	
	public class PaletteListener extends WindowAdapter implements
			WindowListener {
		@Override
		public void windowClosing(WindowEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					putValue(ActionUtil.SELECTED_KEY, false);
				}
			});
		}
	}

}
