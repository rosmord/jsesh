/*
 * @(#)MaximizeWindowAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package org.jhotdraw_7_6.app.action.window;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Maximizes the window of the active view.
 * 
 * @author Werner Randelshofer
 * @version $Id: MaximizeWindowAction.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class MaximizeWindowAction extends AbstractViewAction {
    public final static String ID = "window.maximize";
    
    /** Creates a new instance. */
    public MaximizeWindowAction(Application app,  View view) {
        super(app, view);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        labels.configureAction(this, ID);
    }
    
    private JFrame getFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(
                getActiveView().getComponent()
                );
    }
    
    
    public void actionPerformed(ActionEvent evt) {
        JFrame frame = getFrame();
        if (frame != null) {
            frame.setExtendedState(frame.getExtendedState() ^ Frame.MAXIMIZED_BOTH);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
