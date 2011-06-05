package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;

public class WindowsHelper {

	/**
	 * Gets the root frame for an application and a view
	 * @param application the application
	 * @param view the view
	 * @return a frame, or null if non could be found.
	 */
	public static  Frame getRootFrame(Application application, View view) {
		Frame result= null;
		try {
		Component component= null;
		if (view != null) {
			 component = SwingUtilities.getRoot((Component) view);
		} else if (application.getActiveView() != null) {
			 component = SwingUtilities.getRoot((Component) application.getActiveView());
		} 
		result= (Frame) component;
		} catch (Exception e) {
			// Those messages might be removed (see method documentation).
			System.err.println("problem getting window root. Will return null");
			e.printStackTrace();
		}
		return result;
	}
}
