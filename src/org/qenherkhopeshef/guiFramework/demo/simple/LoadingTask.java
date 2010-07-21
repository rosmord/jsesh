/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.guiFramework.demo.simple;

import org.qenherkhopeshef.guiFramework.swingworker.QenherSwingWorker;

/**
 * @author rosmord
 */
public class LoadingTask extends QenherSwingWorker {

    public Object construct() {
       int i =0;
       for (; i < 200; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            update(new Integer(i));
       }
       return new Integer(i);
    }    
}
