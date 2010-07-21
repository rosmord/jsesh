/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.guiFramework.demo.mdi;

import javax.swing.SwingUtilities;

import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

/**
 *
 * @author rosmord
 */
public class MainMacApplicationAdapter implements ApplicationListener {

    TextEditorApplicationController applicationController;

    public MainMacApplicationAdapter(TextEditorApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public void handleAbout(ApplicationEvent ev) {
        System.out.println("hello there");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                applicationController.about();
            }
        });
        ev.setHandled(true);
    }

    public void handleOpenApplication(ApplicationEvent arg0) {
    }

    public void handleOpenFile(ApplicationEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void handlePreferences(ApplicationEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void handlePrintFile(ApplicationEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void handleQuit(ApplicationEvent arg0) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                applicationController.quitApplication();
            }
        });
    }

    public void handleReOpenApplication(ApplicationEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
