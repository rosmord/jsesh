/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.guiFramework.swingworker;

import javax.swing.SwingUtilities;

/**
 * A version of the SwingWorker1_5 where the notifications are made externally.
 * 
 * A WorkerListener is attached to this worker, and receive information.
 *   
 * <p>
 * Works in java 1.5.
 * 
 * <p>
 * The two type parameters are
 * <ol>
 * <li>the type of the object finally built
 * <li>the type of the information which is passed to the WorkerListener.
 * </ol>
 * 
 * Sample usage : one creates a class :
 * 
 * <pre>
 * public class LoadingTask extends QenherSwingWorker&lt;Integer, Integer&gt; {
 * 
 * 	public Integer construct() {
 * 		int i = 0;
 * 		for (; i &lt; 200; i++) {
 * 			try {
 * 				Thread.sleep(100);
 * 			} catch (InterruptedException ex) {
 * 				ex.printStackTrace();
 * 			}
 * 			// Notify about advance...
 * 			update(new Integer(i));
 * 		}
 * 		return new Integer(i);
 * 	}
 * }
 * </pre>
 * 
 * It is then possible to start() the worker, and the WorkerListener will
 * receive information.
 * 
 * @author rosmord
 * @param <R> type of the final result
 * @param <U> the update information to track progress.
 */
abstract public class QenherSwingWorker<R, U> extends SwingWorker1_5<R> {
	private WorkerListener<R, U> workerListener;

	public void finished() {
		if (workerListener != null)
			workerListener.finished(this);
	}

	/**
	 * Notify <em>On the EDT</em> the workerlistener of this Worker's advance,
	 * by calling the listener's own update method. Note that the updateData
	 * should be thread safe: either a copy of original data, or synchronized.
	 */
	public void update(U updateData) {
		if (workerListener != null)
			SwingUtilities.invokeLater(new UpdateRunnable(updateData));
	}

	public void setWorkerListener(WorkerListener<R, U> workerListener) {
		this.workerListener = workerListener;
	}

	public WorkerListener<R, U> getWorkerListener() {
		return workerListener;
	}

	private class UpdateRunnable implements Runnable {
		U data;

		public UpdateRunnable(U data) {
			this.data = data;
		}

		public void run() {
			workerListener.updateData(data);
		}

	}

}
