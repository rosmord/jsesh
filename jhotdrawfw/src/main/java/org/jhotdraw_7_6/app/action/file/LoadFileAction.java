/*
 * @(#)LoadFileAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app.action.file;

import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractSaveUnsavedChangesAction;
import org.jhotdraw_7_6.gui.JFileURIChooser;
import org.jhotdraw_7_6.gui.JSheet;
import org.jhotdraw_7_6.gui.URIChooser;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.gui.event.SheetEvent;
import org.jhotdraw_7_6.gui.event.SheetListener;
import org.jhotdraw_7_6.gui.filechooser.ExtensionFileFilter;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Lets the user save unsaved changes of the active view, then presents
 * an {@code URIChooser} and loads the selected URI into the active view.
 * <p>
 * This action is called when the user selects the Load item in the File
 * menu. The menu item is automatically created by the application.
 * A Recent Files sub-menu is also automatically generated.
 * <p>
 * If you want this behavior in your application, you have to create it
 * and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}.
 * <p>
 * This action is designed for applications which do not automatically
 * create a new view for each opened file. This action goes together with
 * {@link ClearFileAction}, {@link NewWindowAction}, {@link LoadFileAction},
 * {@link LoadDirectoryAction} and {@link CloseFileAction}.
 * This action should not be used together with {@link OpenFileAction}.
 *
 * @author  Werner Randelshofer
 * @version $Id: LoadFileAction.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class LoadFileAction extends AbstractSaveUnsavedChangesAction {

    public final static String ID = "file.load";

    /** Creates a new instance. */
    public LoadFileAction(Application app,  View view) {
        super(app, view);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        labels.configureAction(this, ID);
    }

    
    protected URIChooser getChooser(View view) {
        URIChooser chsr = (URIChooser) (view.getComponent()).getClientProperty("loadChooser");
        if (chsr == null) {
            chsr = getApplication().getModel().createSaveChooser(getApplication(), view);
            view.getComponent().putClientProperty("loadChooser", chsr);
        }
        return chsr;
    }

    
    public void doIt(final View view) {
        URIChooser fileChooser = getChooser(view);
            Window wAncestor = SwingUtilities.getWindowAncestor(view.getComponent());
            final Component oldFocusOwner = (wAncestor == null) ? null : wAncestor.getFocusOwner();

                    JSheet.showOpenSheet(fileChooser, view.getComponent(), new SheetListener() {

            
                public void optionSelected(final SheetEvent evt) {
                    if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                        final URI uri;
                        if ((evt.getChooser() instanceof JFileURIChooser) && evt.getFileChooser().getFileFilter() instanceof ExtensionFileFilter) {
                            uri = ((ExtensionFileFilter) evt.getFileChooser().getFileFilter()).makeAcceptable(evt.getFileChooser().getSelectedFile()).toURI();
                        } else {
                            uri = evt.getChooser().getSelectedURI();
                        }
                        loadViewFromURI(view, uri, evt.getChooser());
                    } else {
                        view.setEnabled(true);
                        if (oldFocusOwner != null) {
                            oldFocusOwner.requestFocus();
                        }
                    }
                }
            });
    }

    public void loadViewFromURI(final View view, final URI uri, final URIChooser chooser) {
        view.setEnabled(false);

        // Open the file
        view.execute(new Worker() {

            
            protected Object construct() throws IOException {
                view.read(uri, chooser);
                return null;
            }

            
            protected void done(Object value) {
                view.setURI(uri);
                view.setEnabled(true);
                getApplication().addRecentURI(uri);
            }

            
            protected void failed(Throwable value) {
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
                JSheet.showMessageSheet(view.getComponent(),
                        "<html>" + UIManager.getString("OptionPane.css") +
                        "<b>" + labels.getFormatted("file.load.couldntLoad.message", URIUtil.getName(uri)) + "</b><p>" +
                        ((value == null) ? "" : value),
                        JOptionPane.ERROR_MESSAGE, new SheetListener() {

                    
                    public void optionSelected(SheetEvent evt) {
                        view.clear();
                        view.setEnabled(true);
                    }
                });
            }

        });
    }
}
