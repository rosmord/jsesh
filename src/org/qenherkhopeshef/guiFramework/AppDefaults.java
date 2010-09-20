package org.qenherkhopeshef.guiFramework;

import javax.swing.KeyStroke;
import javax.swing.UIDefaults;

/**
 * Application defaults from Asserting Control Over the GUI: Commands, Defaults, and Resource Bundles
by Hans Muller
 *
 */
public class AppDefaults extends UIDefaults {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7289206292792859095L;
	
	public KeyStroke getKeyStroke(String key) {
        return KeyStroke.getKeyStroke(getString(key));
    }
	
    public Integer getKeyCode(String key) {
        KeyStroke ks = getKeyStroke(key);
        return (ks != null) 
            ? new Integer(ks.getKeyCode()) 
            : null;
    }
}