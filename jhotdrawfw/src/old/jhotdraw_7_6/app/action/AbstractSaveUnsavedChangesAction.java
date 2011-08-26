/*
 * @(#)AbstractSaveUnsavedChangesAction.java
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
package org.jhotdraw_7_6.app.action;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.URI;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.gui.*;
import org.jhotdraw_7_6.gui.event.*;
import org.jhotdraw_7_6.io.*;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.*;

/**
 * This abstract class can be extended to implement an {@code Action} that asks
 * to save unsaved changes of a {@link org.jhotdraw_7_6.app.View} before the
 * the action is performed.
 * <p>
 * If the view has no unsaved changes, method doIt is invoked immediately.
 * If unsaved changes are present, a dialog is shown asking whether the user
 * wants to discard the changes, cancel or save the changes before doing it.
 * If the user chooses to discard the chanegs, toIt is invoked immediately.
 * If the user chooses to cancel, the action is aborted.
 * If the user chooses to save the changes, the view is saved, and doIt
 * is only invoked after the view was successfully saved.
 *
 * @author  Werner Randelshofer
 * @version $Id: AbstractSaveUnsavedChangesAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public abstract class AbstractSaveUnsavedChangesAction extends AbstractViewAction {

    private Component oldFocusOwner;

    /** Creates a new instance. */
    public AbstractSaveUnsavedChangesAction(Application app, View view) {
        super(app, view);
    }

    public void actionPerformed(ActionEvent evt) {
        final View p = getActiveView();
        if (p.isEnabled()) {
            final ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
            Window wAncestor = SwingUtilities.getWindowAncestor(p.getComponent());
            oldFocusOwner = (wAncestor == null) ? null : wAncestor.getFocusOwner();
            p.setEnabled(false);

            if (p.hasUnsavedChanges()) {
                URI unsavedURI = p.getURI();
                JOptionPane pane = new JOptionPane(
                        "<html>" + UIManager.getString("OptionPane.css") +//
                        "<b>" + labels.getFormatted("file.saveBefore.doYouWantToSave.message",//
                        (unsavedURI == null) ? labels.getString("unnamedFile") : URIUtil.getName(unsavedURI)) + "</b><p>" +//
                        labels.getString("file.saveBefore.doYouWantToSave.details"),
                        JOptionPane.WARNING_MESSAGE);
                Object[] options = { //
                    labels.getString("file.saveBefore.saveOption.text"),//
                    labels.getString("file.saveBefore.cancelOption.text"), //
                    labels.getString("file.saveBefore.dontSaveOption.text")//
                };
                pane.setOptions(options);
                pane.setInitialValue(options[0]);
                pane.putClientProperty("Quaqua.OptionPane.destructiveOption", new Integer(2));
                JSheet.showSheet(pane, p.getComponent(), new SheetListener() {

                    public void optionSelected(SheetEvent evt) {
                        Object value = evt.getValue();
                        if (value == null || value.equals(labels.getString("file.saveBefore.cancelOption.text"))) {
                            p.setEnabled(true);
                        } else if (value.equals(labels.getString("file.saveBefore.dontSaveOption.text"))) {
                            doIt(p);
                            p.setEnabled(true);
                        } else if (value.equals(labels.getString("file.saveBefore.saveOption.text"))) {
                            saveView(p);
                        }
                    }
                });

            } else {
                doIt(p);
                p.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        }
    }

    protected URIChooser getChooser(View view) {
        URIChooser chsr = (URIChooser) (view.getComponent()).getClientProperty("saveChooser");
        if (chsr == null) {
            chsr = getApplication().getModel().createSaveChooser(getApplication(), view);
            view.getComponent().putClientProperty("saveChooser", chsr);
        }
        return chsr;
    }

    protected void saveView(final View p) {
        if (p.getURI() == null) {
            URIChooser chooser = getChooser(p);
            //int option = fileChooser.showSaveDialog(this);
            JSheet.showSaveSheet(chooser, p.getComponent(), new SheetListener() {

                public void optionSelected(final SheetEvent evt) {
                    if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                        final URI uri;
                        if ((evt.getChooser() instanceof JFileURIChooser) && evt.getFileChooser().getFileFilter() instanceof ExtensionFileFilter) {
                            uri = ((ExtensionFileFilter) evt.getFileChooser().getFileFilter()).makeAcceptable(evt.getFileChooser().getSelectedFile()).toURI();
                        } else {
                            uri = evt.getChooser().getSelectedURI();
                        }
                        saveViewToURI(p, uri, evt.getChooser());
                    } else {
                        p.setEnabled(true);
                        if (oldFocusOwner != null) {
                            oldFocusOwner.requestFocus();
                        }
                    }
                }
            });
        } else {
            saveViewToURI(p, p.getURI(), null);
        }
    }

    protected void saveViewToURI(final View p, final URI uri, final URIChooser chooser) {
        p.execute(new Worker() {

            protected Object construct() throws IOException {
                p.write(uri, chooser);
                return null;
            }

            @Override
            protected void done(Object value) {
                p.setURI(uri);
                p.markChangesAsSaved();
                doIt(p);
            }

            @Override
            protected void failed(Throwable value) {
                String message;
                if ((value instanceof Throwable) && ((Throwable) value).getMessage() != null) {
                    message = ((Throwable) value).getMessage();
                } else {
                    message = value.toString();
                }
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
                JSheet.showMessageSheet(getActiveView().getComponent(),
                        "<html>" + UIManager.getString("OptionPane.css")
                        + "<b>" + labels.getFormatted("file.save.couldntSave.message", URIUtil.getName(uri)) + "</b><p>"
                        + ((message == null) ? "" : message),
                        JOptionPane.ERROR_MESSAGE);
            }

            @Override
            protected void finished() {
                p.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        });
    }

    protected abstract void doIt(View p);
}
