package org.qenherkhopeshef.swingUtils.errorHandler;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Displays an exception message in a window.
 * @author rosmord
 */
public class ErrorMessageHandler implements UncaughtExceptionHandler {

	/**
	 * Installs a new message handler which will receive all uncaught message and will
	 * allow users to send bug reports to the application author.
	 */
	public static void installMessageHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new ErrorMessageHandler());
	}
	
	public void uncaughtException(Thread arg0, Throwable arg1) {
		arg1.printStackTrace();
		new ErrorMessageDisplayer(arg1).display();
	}
}