/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.guiFramework.swingworker;


/**
 *
 * @author rosmord
 */
public interface WorkerListener<R,U> {
    /**
     * Method called (on the EDT) by the customized Worker to signal he has finished.
     */
    public void finished(QenherSwingWorker<R,U> source);

    /**
     * Method called on the EDT by the worker to signal a new stage of advancement.
     * Note that the updateData should be thread safe: either a copy of original data,
     * or synchronized.
     */
    public void update(U updateData);
}
