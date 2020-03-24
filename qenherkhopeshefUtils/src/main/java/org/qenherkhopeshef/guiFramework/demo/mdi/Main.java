package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.splash.SplashScreen;

/**
 * "Dirty" application. 
 * We create it to explore the technical problem,
 * then we will refactor and generalize it.
 * 
 * Goals : a multi-platform application.
 * On mac: an hidden window will allow one to create new files.
 * On Pc/Linux: the application will quit if no window is opened.
 * @author rosmord
 *
 */
public class Main {

	private SplashScreen splashScreen= null;
	
	
	public void start()  {
		try {
			splashScreen= new SplashScreen("/org/qenherkhopeshef/guiFramework/splash/default.png");
			splashScreen.display();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					buildMainFrame();					
				}
				
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private void buildMainFrame() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");  
        System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false"); 
		JFrame mainFrame= new JFrame("hello world");
		mainFrame.setUndecorated(true);
		mainFrame.setSize(new Dimension(0,0));
		JMenuBar bar= new JMenuBar();
		bar.add(new JMenu("hello"));
		mainFrame.setJMenuBar(bar);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				splashScreen.closeSplash();
			}		
		});
		mainFrame.setVisible(true);
		mainFrame.setLocation(-100, -100);
	}

	public static void main(String[] args) {
		Main main= new Main();
		main.start();
	}
}
