package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.Component;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.qenherkhopeshef.guiFramework.PropertyHolder;
import org.qenherkhopeshef.guiFramework.SimpleApplicationFramework;

/**
 * Workflow for text edition.
 * 
 * @author rosmord
 */
public class TextEditorApplicationController implements PropertyHolder, MDIFrameController {

    /**
     * The empty "uncloseable" window
     */
    private MacEmptyWindow macEmptyWindow;
    private JMenu windowMenu;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);
    /**
     * The list of opened document editing sessions.
     */
    private List documentEditorSessionControllerList = new ArrayList();
    private String applicationName = "Simple text editor";

    public TextEditorApplicationController() {
        macEmptyWindow = new MacEmptyWindow(applicationName);
        JMenuBar menuBar= buildMenuBar(this);
        macEmptyWindow.setJMenuBar(menuBar);
    }

    /**
     * A "palette" containing some buttons.
     */
    /**
     * Open a new text. Simulate a looong operation, in order to show how such
     * things should be done.
     */
    public void openDocument() {
    }

    /**
     * Create a new document and the corresponding window.
     */
    public void newDocument() {
        DocumentEditorSessionController sessionController = new DocumentEditorSessionController(this);
        sessionController.getFrame().setJMenuBar(buildMenuBar(sessionController));
        sessionController.getFrame().setVisible(true);
        documentEditorSessionControllerList.add(sessionController);
        updateWindowMenu();
    }

    public void quitApplication() {
        // Ensure everything is ok
        // if there are "dirty" documents, propose to review them
        // depending on the answer, either
        //      cancel the close
        //      proceed to the review
        //      quit anyway.
        // Close the application.
        System.exit(0);
    }

    public void hideWindow() {

    }

    public void bringEverythingToFront() {

    }
    
    /**
     * Is there an available editing session to control ?
     *
     * @return
     */
    public boolean isCurrentEditorWorkflowAvailable() {
        return true;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public MacEmptyWindow getMacEmptyWindow() {
        return macEmptyWindow;
    }

    private JMenuBar buildMenuBar(PropertyHolder target) {
        SimpleApplicationFramework simpleApplicationFramework=
                new SimpleApplicationFramework("mdi8n", "menu.txt", target);
       return simpleApplicationFramework.getJMenuBar();
    }



    /**
     * Updates the "window" menu.
     * As a convention, the last element of the window menu should be
     * "bringEverythingToFront".
     */
    private void updateWindowMenu() {
        if (windowMenu == null) {
            return;
        }
        ArrayList items = new ArrayList();
        for (int i = 0; i < windowMenu.getMenuComponents().length; i++) {
            items.add(windowMenu.getMenuComponent(i));
            if ("bringEverythingToFront".equals(windowMenu.getMenuComponents()[i].getName())) {
                break;
            }
        }
        windowMenu.removeAll();
        for (int i = 0; i < items.size(); i++) {
            windowMenu.add((Component) items.get(i));
        }
        if (documentEditorSessionControllerList.size() > 0) {
            windowMenu.addSeparator();
            for (int i = 0; i < documentEditorSessionControllerList.size(); i++) {
                windowMenu.add(new JMenuItem(documentEditorSessionControllerList.get(i).toString()));
            }
        }
    }

    public Object getApplicationDelegate() {
        return this;
    }

    public boolean isDocumentEditor() {
        return false;
    }

    public void about() {
        
    }
}
