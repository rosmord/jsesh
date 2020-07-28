/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jhotdrawChanges;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jhotdraw_7_6.app.AbstractApplication;

/**
 * Rewrite of Rewrite of OSXAdapter to use java.awt.desktop - will lead to a
 * Windows port later. No longer a static class...
 *
 * @author rosmord
 */
public class QenherOSXDesktopAdapter {

    private AbstractApplication application;

    public QenherOSXDesktopAdapter(AbstractApplication application) {
        this.application = application;
    }

    public void setAboutHandler(ActionListener aboutHandler) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().setAboutHandler(e -> {
                aboutHandler.actionPerformed(new ActionEvent(Desktop.getDesktop(), ActionEvent.ACTION_PERFORMED, "about"));
            });
        }
    }
    /**
     * The action listener will be called when the Quit menu item is selected
     * from the application menu.
     *
     * @param quitHandler
     */
    public void setQuitHandler(ActionListener quitHandler) {
        Desktop.getDesktop().setQuitHandler((e, response) -> quitHandler.actionPerformed(
                new ActionEvent(Desktop.getDesktop(), ActionEvent.ACTION_PERFORMED, "quit")
        ));
    }

    /**
     * Pass this method an {@code ActionListener} equipped to display
     * application options. They will be called when the Preferences menu item
     * is selected from the application menu.
     */
    public void setPreferencesHandler(ActionListener prefsHandler) {
        Desktop.getDesktop().setPreferencesHandler(e -> {
            if (prefsHandler != null) {
                prefsHandler.actionPerformed(
                        new ActionEvent(application, ActionEvent.ACTION_PERFORMED, "")
                );
            }
        });
    }

    /**
     * Pass this method an {@code ActionListener} equipped to handle document
     * events from the Finder.Documents are registered with the Finder via the
     * CFBundleDocumentTypes dictionary in the application bundle's
     * Info.plist.<p>
     * The filename is passed as the {@code actionCommand}.
     *
     * @param fileHandler
     */
    public void setOpenFileHandler(ActionListener fileHandler) {
        Desktop.getDesktop().setOpenFileHandler(
                e -> processFiles(fileHandler, e.getFiles())
        );
        Desktop.getDesktop().setOpenURIHandler(
                e -> JOptionPane.showMessageDialog(null, "got URI " + e.getURI() + "... don't know how to deal with it.")
        );
    }

    /**
     * Pass files to fileHandler.
     * Due to the current architecture of jhotdraw, file handlers are
     * event listeners, which expect the file paths as strings.
     * @param fileHandler
     * @param files 
     */
    private void processFiles(ActionListener fileHandler, List<File> files) {
        for (File f: files) {
            final ActionEvent actionEvent = new ActionEvent(application, ActionEvent.ACTION_FIRST, f.getPath());
            SwingUtilities.invokeLater(() -> fileHandler.actionPerformed(actionEvent));
        }
    }
}
