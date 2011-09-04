package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.List;

/**
 * This object handle all windows in the application. It knows when a window is
 * closed or opened, for instance.
 * 
 * @author rosmord
 */
public class WindowRepository {
	List<Window> windowList;
	private MyWindowListener myWindowListener;

	public void addWindow(Window window) {
		windowList.add(window);
		window.addWindowFocusListener(myWindowListener);
		window.addWindowListener(myWindowListener);
		window.addWindowStateListener(myWindowListener);
	}

	private class MyWindowListener implements WindowFocusListener,
			WindowListener, WindowStateListener {

		public void windowGainedFocus(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowLostFocus(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowActivated(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowClosed(WindowEvent e) {
			Window window = e.getWindow();
			window.removeWindowFocusListener(myWindowListener);
			window.removeWindowListener(myWindowListener);
			window.removeWindowStateListener(myWindowListener);
			windowList.remove(window);
			// Update window menu ?
		}

		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub

		}

		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
		}

		// called when the window's state changes by virtue of being iconified,
		// maximized etc., the windowStateChanged method in the listener object
		// is invoked, and the WindowEvent is passed to it.
		
		public void windowStateChanged(WindowEvent e) {
			// TODO Auto-generated method stub

		}

	}
}
