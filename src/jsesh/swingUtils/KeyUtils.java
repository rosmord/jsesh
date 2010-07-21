package jsesh.swingUtils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.qenherkhopeshef.utils.PlatformDetection;

public class KeyUtils {

	/**
	 * Build a "standard" shortcut keystroke suitable for the current platform.
	 * @param vk the basic key stroke (see {@link KeyEvent})
	 * @param mask the mask for additional modifiers, typically shift. {@link InputEvent}
	 * @return a correct KeyStroke.
	 */
	public static KeyStroke buildCommandShortCut(int vk, int mask) {
		return KeyStroke
				.getKeyStroke(vk, mask
						| java.awt.Toolkit.getDefaultToolkit()
								.getMenuShortcutKeyMask());
	}

	/**
	 * Build a "standard" shortcut keystroke suitable for the current platform.
	 * @param vk the basic key stroke (see {@link KeyEvent})
	 * @param mask the mask {@link InputEvent}
	 * @return a correct KeyStroke.
	 */
	public static KeyStroke buildCommandShortCut(int vk) {
		return KeyStroke.getKeyStroke(vk, java.awt.Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask());
	}

	/**
	 * Build a correct shortcut for redo, depending on the current platform.
	 * @return
	 */
	public static KeyStroke buildRedoShortCut() {
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			return buildCommandShortCut(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK);
		} else {
			return buildCommandShortCut(KeyEvent.VK_Y);
			
		}
	}

}
