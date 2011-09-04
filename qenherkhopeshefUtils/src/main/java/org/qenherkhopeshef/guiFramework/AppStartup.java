package org.qenherkhopeshef.guiFramework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.qenherkhopeshef.guiFramework.splash.SplashMessageText;
import org.qenherkhopeshef.guiFramework.splash.SplashScreen;
import org.qenherkhopeshef.guiFramework.swingworker.SwingWorker1_5;
import org.qenherkhopeshef.swingUtils.errorHandler.ErrorMessageHandler;

/**
 * Skeleton for an application with a splash screen and a long startup.
 * <ul>
 * <li>the application startup object is created
 * <li>when run() is called, initApplicationData is run in a worker thread, and
 * the splash screen is displayed
 * <li>when the data is ready, startApplication can be called.
 * </ul>
 * Usable for JDK 1.5.
 * 
 * Normally, one extends this class to write an new application.
 * 
 * 
 * @author rosmord
 * 
 */
public abstract class AppStartup<InitData> {

	private PreparationWorker initWorker;
	private Timer tickTimer;
	private String imgPath = null;
	private ArrayList<SplashMessageText> messages = new ArrayList<SplashMessageText>();

	public void setSplashPicture(String imgPath) {
		this.imgPath = imgPath;
	}

	/**
	 * Starts the software in the correct thread.
	 * 
	 * Should be called from the main thread.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	public void run() throws InterruptedException, InvocationTargetException {
		// Catch all uncaught exception
		ErrorMessageHandler.installMessageHandler();
		// FontSizeHelper.fixFontSize();

		initWorker = new PreparationWorker();

		// Starts initialisation...

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				initWorker.start();
				// The initworker will start the application when ready...
			}
		});
	}

	/**
	 * Method called on the main thread to prepare the application data. This
	 * method is <em>not</em> called in the event dispatch thread.
	 * <p>
	 * It is suggested that you call setProgression from there if you want to
	 * display progression information.
	 * <p>
	 * If you prefer an automatic system, you can call setTickTimer before
	 * calling run.
	 * 
	 * @return
	 */
	public abstract InitData initApplicationData();

	/**
	 * Actual start of the application. called in the Event Dispatch Thread.
	 * 
	 * @param data
	 *            the data prepared by the application initialisation.
	 */
	public abstract void startApplication(InitData data);

	/**
	 * Ask for the initialisation progression to be drawn automatically.
	 * <p>
	 * It is not always easy to draw an accurate representation of the
	 * progression of a software, and in many case a simple animation may help
	 * the user to wait for completion.
	 * 
	 * Calling this function means that you won't call explicitely
	 * setProgression. Instead, the progression will be automatically increased
	 * (without any link to the actual progression, of course).
	 * 
	 * This is fine when the display is for instance a clock or something like
	 * that.
	 */
	public void setTickTimer() {
		tickTimer = new Timer(100, new ActionListener() {

			int progression = 0;

			public void actionPerformed(ActionEvent e) {
				setProgression(progression);
				progression++;
			}
		});
	}

	/**
	 * Call before starting the application.
	 * 
	 * @param message
	 */
	public void addSplashMessage(SplashMessageText message) {
		messages.add(message);
	}

	public void setProgression(int progression) {
		initWorker.setProgression(progression);
	}

	/**
	 * Class used for the preparation application.
	 * 
	 * @author rosmord
	 * 
	 */
	private class PreparationWorker extends SwingWorker1_5<InitData> {

		SplashScreen splashScreen;

		public PreparationWorker() {
			if (imgPath != null) {
				splashScreen = new SplashScreen(imgPath);
			} else {
				splashScreen = new SplashScreen();
			}
			for (SplashMessageText m : messages)
				splashScreen.addMessage(m);
			splashScreen.setClockDisplay();
		}

		@Override
		public void start() {
			splashScreen.display();
			if (tickTimer != null) {
				tickTimer.start();
			}
			super.start();
		}

		/**
		 * This method is safe to call from anywhere.
		 * 
		 * @param progression
		 */
		public void setProgression(int progression) {
			splashScreen.setProgression(progression);
		}

		@Override
		public InitData construct() {
			return initApplicationData();
		}

		@Override
		public void finished() {
			if (tickTimer != null) {
				tickTimer.stop();
			}
			splashScreen.closeSplash();
			startApplication(get());
		}
	}
}
