/*
 * @(#)AbstractSaveUnsavedChangesAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
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
 * This abstract class can be extended to implement an {@code Action} that asks
 * to save unsaved changes of a {@link org.jhotdraw_7_6.app.View} before a destructive
 * action is performed.
 * <p>
 * If the view has no unsaved changes, method {@code doIt} is invoked immediately.
 * If unsaved changes are present, a dialog is shown asking whether the user
 * wants to discard the changes, cancel or save the changes before doing it.
 * If the user chooses to discard the changes, {@code doIt} is invoked immediately.
 * If the user chooses to cancel, the action is aborted.
 * If the user chooses to save the changes, the view is saved, and {@code doIt}
 * is only invoked after the view was successfully saved.
 *
 * @author  Werner Randelshofer
 * @version $Id: AbstractSaveUnsavedChangesAction.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public abstract class AbstractSaveUnsavedChangesAction extends AbstractViewAction {

    
    private Component oldFocusOwner;

    /** Creates a new instance. */
    public AbstractSaveUnsavedChangesAction(Application app,  View view) {
        super(app, view);
    }

    
    public void actionPerformed(ActionEvent evt) {
        final View v = getActiveView();
        if (v == null) {
            return;
        }
        if (v.isEnabled()) {
            final ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
            Window wAncestor = SwingUtilities.getWindowAncestor(v.getComponent());
            oldFocusOwner = (wAncestor == null) ? null : wAncestor.getFocusOwner();
            v.setEnabled(false);

            if (v.hasUnsavedChanges()) {
                URI unsavedURI = v.getURI();
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
                pane.putClientProperty("Quaqua.OptionPane.destructiveOption", 2);
                JSheet.showSheet(pane, v.getComponent(), new SheetListener() {

                    
                    public void optionSelected(SheetEvent evt) {
                        Object value = evt.getValue();
                        if (value == null || value.equals(labels.getString("file.saveBefore.cancelOption.text"))) {
                            v.setEnabled(true);
                        } else if (value.equals(labels.getString("file.saveBefore.dontSaveOption.text"))) {
                            doIt(v);
                            v.setEnabled(true);
                        } else if (value.equals(labels.getString("file.saveBefore.saveOption.text"))) {
                            saveView(v);
                        }
                    }
                });

            } else {
                doIt(v);
                v.setEnabled(true);
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

    protected void saveView(final View v) {
        if (v.getURI() == null) {
            URIChooser chooser = getChooser(v);
            //int option = fileChooser.showSaveDialog(this);
            JSheet.showSaveSheet(chooser, v.getComponent(), new SheetListener() {

                
                public void optionSelected(final SheetEvent evt) {
                    if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                        final URI uri;
                        if ((evt.getChooser() instanceof JFileURIChooser) && evt.getFileChooser().getFileFilter() instanceof ExtensionFileFilter) {
                            uri = ((ExtensionFileFilter) evt.getFileChooser().getFileFilter()).makeAcceptable(evt.getFileChooser().getSelectedFile()).toURI();
                        } else {
                            uri = evt.getChooser().getSelectedURI();
                        }
                        saveViewToURI(v, uri, evt.getChooser());
                    } else {
                        v.setEnabled(true);
                        if (oldFocusOwner != null) {
                            oldFocusOwner.requestFocus();
                        }
                    }
                }
            });
        } else {
            saveViewToURI(v, v.getURI(), null);
        }
    }

    protected void saveViewToURI(final View v, final URI uri,  final URIChooser chooser) {
        v.execute(new Worker() {

            
            protected Object construct() throws IOException {
                v.write(uri, chooser);
                return null;
            }

            
            protected void done(Object value) {
                v.setURI(uri);
                v.markChangesAsSaved();
                doIt(v);
            }

            
            protected void failed(Throwable value) {
                String message = (value.getMessage() != null) ? value.getMessage() : value.toString();
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
                JSheet.showMessageSheet(getActiveView().getComponent(),
                        "<html>" + UIManager.getString("OptionPane.css")
                        + "<b>" + labels.getFormatted("file.save.couldntSave.message", URIUtil.getName(uri)) + "</b><p>"
                        + ((message == null) ? "" : message),
                        JOptionPane.ERROR_MESSAGE);
            }

            
            protected void finished() {
                v.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        });
    }

    protected abstract void doIt(View p);
}
