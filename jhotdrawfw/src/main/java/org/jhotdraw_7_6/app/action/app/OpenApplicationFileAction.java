/*
 * @(#)OpenApplicationFileAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app.action.app;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.jhotdraw_7_6.gui.JSheet;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.gui.event.SheetEvent;
import org.jhotdraw_7_6.gui.event.SheetListener;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Opens a file for which an open-request was sent to the application.
 * <p>
 * The file name is passed in the action command of the action event.
 * <p>
 * This action is called when the user drops a file on the dock icon of
 * {@code DefaultOSXApplication} or onto the desktop area of
 * {@code DefaultMDIApplication}. 
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}.
 * 
 * <p> Note (S. Rosmorduc) : as this action takes an actual argument, it's perhaps
 * not really a regular action. An improved framework might differentiate between
 * its aspects, with a finer grain for classes.
 *
 * @author  Werner Randelshofer
 * @version $Id: OpenApplicationFileAction.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class OpenApplicationFileAction extends AbstractApplicationAction {

    public final static String ID = "application.openFile";
    private JFileChooser fileChooser;
    private int entries;

    /** Creates a new instance. */
    public OpenApplicationFileAction(Application app) {
        super(app);
        putValue(Action.NAME, "OSX Open File");
    }

    /**
     * Opens a new view.
     * <p>
     * The file name is passed in the action command of the action event.
     *
     * @param evt action event.
     */
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        final Application app = getApplication();
        final String filename = evt.getActionCommand();

        if (app.isEnabled()) {
            app.setEnabled(false);
            // Search for an empty view
            View emptyView = app.getActiveView();
            if (emptyView == null
                    || emptyView.getURI() != null
                    || emptyView.hasUnsavedChanges()) {
                emptyView = null;
            }

            final View p;
            if (emptyView == null) {
                p = app.createView();
                app.add(p);
                app.show(p);
            } else {
                p = emptyView;
            }
            openView(p, new File(filename).toURI());
        }
    }

    protected void openView(final View view, final URI uri) {
        final Application app = getApplication();
        app.setEnabled(true);


        // If there is another view with the same URI we set the multiple open
        // id of our view to max(multiple open id) + 1.
        int multipleOpenId = 1;
        for (View aView : app.views()) {
            if (aView != view
                    && aView.getURI() != null
                    && aView.getURI().equals(uri)) {
                multipleOpenId = Math.max(multipleOpenId, aView.getMultipleOpenId() + 1);
            }
        }
        view.setMultipleOpenId(multipleOpenId);
        view.setEnabled(false);

        // Open the file
        view.execute(new Worker() {

            
            @Override
            protected Object construct() throws IOException {
                boolean exists = true;
                try {
                    File f = new File(uri);
                    exists = f.exists();
                } catch (IllegalArgumentException e) {
                    // The URI does not denote a file, thus we can not check whether the file exists.
                }
                if (exists) {
                    view.read(uri, null);
                    return null;
                } else {
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
                    throw new IOException(labels.getFormatted("file.open.fileDoesNotExist.message", URIUtil.getName(uri)));
                }
            }

            
            protected void done(Object value) {
                view.setURI(uri);
                Frame w = (Frame) SwingUtilities.getWindowAncestor(view.getComponent());
                if (w != null) {
                    w.setExtendedState(w.getExtendedState() & ~Frame.ICONIFIED);
                    w.toFront();
                }
                view.setEnabled(true);
                view.getComponent().requestFocus();
            }

            
            protected void failed(Throwable value) {
                value.printStackTrace();
                String message = value.getMessage() != null ? value.getMessage() : value.toString();

                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
                JSheet.showMessageSheet(view.getComponent(),
                        "<html>" + UIManager.getString("OptionPane.css")
                        + "<b>" + labels.getFormatted("file.open.couldntOpen.message", URIUtil.getName(uri)) + "</b><p>"
                        + (message == null ? "" : message),
                        JOptionPane.ERROR_MESSAGE, new SheetListener() {

                    
                    public void optionSelected(SheetEvent evt) {
                        view.setEnabled(true);
                    }
                });
            }
        });
    }
}
