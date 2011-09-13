package org.qenherkhopeshef.jhotdrawChanges;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

/**
 * Interface for mac-like applications, which manage active views.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public interface ActiveViewAwareApplication extends Application {
	/**
	 * Change the active view.
	 * @param view
	 */
	void setActiveView(View view);
	
	/**
	 * sets menus (and maybe other things) on external dialogs. 
	 * @param dialog
	 */
	void initSecondaryWindow(JFrame dialog);
}
