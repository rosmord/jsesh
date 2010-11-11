/*
 * @(#)ExportFileAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw_7_4_1.app.action.file;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.URI;

import org.jhotdraw_7_4_1.app.*;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;
import org.jhotdraw_7_4_1.gui.*;
import org.jhotdraw_7_4_1.gui.event.*;
import org.jhotdraw_7_4_1.net.URIUtil;
import org.jhotdraw_7_4_1.util.*;

/**
 * Presents a file chooser to the user and then exports the contents of the
 * active view to the chosen file.
 * <p>
 * This action requires that {@link ApplicationModel#createExportChooser}
 * creates an appropriate {@link URIChooser}.
 * <p>
 * This action is called when the user selects the Export item in the File
 * menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link ApplicationModel#initApplication}. 
 *
 * @author Werner Randelshofer
 * @version $Id: ExportFileAction.java 608 2010-01-11 18:46:00Z rawcoder $
 */
public class ExportFileAction extends AbstractViewAction {

    public final static String ID = "file.export";
    private Component oldFocusOwner;

    /** Creates a new instance. */
    public ExportFileAction(Application app, View view) {
        super(app, view);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, ID);
    }

    public void actionPerformed(ActionEvent evt) {
        final View view = (View) getActiveView();
        if (view.isEnabled()) {
            ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");

            oldFocusOwner = SwingUtilities.getWindowAncestor(view.getComponent()).getFocusOwner();
            view.setEnabled(false);

            URIChooser fileChooser = getApplication().getExportChooser(view);

            JSheet.showSheet(fileChooser, view.getComponent(), labels.getString("filechooser.export"), new SheetListener() {

                public void optionSelected(final SheetEvent evt) {
                    if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                        final URI uri = evt.getChooser().getSelectedURI();
                        if (evt.getChooser()instanceof JFileURIChooser) {
                        exportView(view, uri, evt.getChooser());
                        } else {
                        exportView(view, uri, null);
                        }
                    } else {
                        view.setEnabled(true);
                        if (oldFocusOwner != null) {
                            oldFocusOwner.requestFocus();
                        }
                    }
                }
            });
        }
    }

    protected void exportView(final View view, final URI uri,
            final URIChooser chooser) {
        view.execute(new Worker() {

            protected Object construct() throws IOException {
                view.write(uri,chooser);
                return null;
            }

            protected void failed(Throwable value) {
                System.out.flush();
                ((Throwable) value).printStackTrace();
                // FIXME localize this error messsage
                JSheet.showMessageSheet(view.getComponent(),
                        "<html>" + UIManager.getString("OptionPane.css") +
                        "<b>Couldn't export to the file \"" + URIUtil.getName(uri) + "\".<p>" +
                        "Reason: " + value,
                        JOptionPane.ERROR_MESSAGE);
            }

            protected void finished() {
                view.setEnabled(true);
                SwingUtilities.getWindowAncestor(view.getComponent()).toFront();
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        });
    }
}
