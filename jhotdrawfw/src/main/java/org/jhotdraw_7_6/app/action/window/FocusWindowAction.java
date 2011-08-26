/*
 * @(#)FocusWindowAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app.action.window;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Requests focus for a Frame.
 *
 * @author  Werner Randelshofer
 * @version $Id: FocusWindowAction.java 722 2010-11-26 08:49:25Z rawcoder $
 */
public class FocusWindowAction extends AbstractAction {

    public final static String ID = "window.focus";
     private View view;
    private PropertyChangeListener ppc;

    /** Creates a new instance. */
    public FocusWindowAction( View view) {
        this.view = view;
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        labels.configureAction(this, ID);
        //setEnabled(false);
        setEnabled(view != null);

        ppc = new PropertyChangeListener() {

            
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals(View.TITLE_PROPERTY)) {
                    putValue(Action.NAME, evt.getNewValue());
                }
            }
        };
        if (view != null) {
            view.addPropertyChangeListener(ppc);
        }
    }

    public void dispose() {
        setView(null);
    }

    public void setView( View newValue) {
        if (view != null) {
            view.removePropertyChangeListener(ppc);
        }
        view = newValue;
        if (view != null) {
            view.addPropertyChangeListener(ppc);
        }
    }

    
    public Object getValue(String key) {
        if (key == Action.NAME && view != null) {
            return getTitle();
        } else {
            return super.getValue(key);
        }
    }

    private String getTitle() {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        String title = labels.getString("unnamedFile");
        if (view != null) {
            URI uri = view.getURI();
            if (uri == null) {
                title = labels.getString("unnamedFile");
            } else {
                title = URIUtil.getName(uri);
            }
            if (view.hasUnsavedChanges()) {
                title += "*";
            }
            title = (labels.getFormatted("internalFrame.title", title,
                    view.getApplication() == null?"":view.getApplication().getName(), view.getMultipleOpenId()));
        }
        return title;

    }

    private JFrame getFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(
                view.getComponent());
    }

    private Component getRootPaneContainer() {
        return SwingUtilities.getRootPane(
                view.getComponent()).getParent();
    }

    
    public void actionPerformed(ActionEvent evt) {
        /*
        JFrame frame = getFrame();
        if (frame != null) {
        frame.setExtendedState(frame.getExtendedState() & ~Frame.ICONIFIED);
        frame.toFront();
        frame.requestFocus();
        JRootPane rp = SwingUtilities.getRootPane(view.getComponent());
        if (rp != null && (rp.getParent() instanceof JInternalFrame)) {
        ((JInternalFrame) rp.getParent()).toFront();
        }
        view.getComponent().requestFocus();
        } else {
        Toolkit.getDefaultToolkit().beep();
        }*/
        Component rpContainer = getRootPaneContainer();
        if (rpContainer instanceof Frame) {
            Frame frame = (Frame) rpContainer;
            frame.setExtendedState(frame.getExtendedState() & ~Frame.ICONIFIED);
            frame.toFront();
        } else if (rpContainer instanceof JInternalFrame) {
            JInternalFrame frame = (JInternalFrame) rpContainer;
            frame.toFront();
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e) {
                // Don't care.
            }
        }
        view.getComponent().requestFocusInWindow();
    }
}
