/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.guiFramework.demo.mdi;

/**
 * Methods provided by all objects which control frames and have menus.
 * Mainly the Application Controller and the individual document controller.
 * @author rosmord
 */
public interface MDIFrameController {

    Object getApplicationDelegate();

    /**
     * Returns true if a specific document is being edited by this controller.
     * Normally always true or always false.
     * @return
     */
    boolean isDocumentEditor();

}
