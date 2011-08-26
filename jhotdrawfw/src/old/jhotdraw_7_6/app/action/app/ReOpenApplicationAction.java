/*
 * @(#)OSXOpenApplicationAction.java
 * 
 * Copyright (c) 2009-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 * 
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw_7_6.app.action.app;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;

/**
 * If all views are iconified, de-iconifies a view and brings it to the front.
 * <p>
 * This action is called when the Mac OS X Finder or another Mac OS X application
 * sends an Open Application request to the application.
 * <p>
 * This action is automatically created by {@code DefaultOSXApplication}
 * and put into the {@code ApplicationModel} before
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication} is called.
 *
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-12-18 Created.
 */
public class ReOpenApplicationAction extends AbstractApplicationAction {

    public final static String ID = "application.reOpen";

    /** Creates a new instance. */
    public ReOpenApplicationAction(Application app) {
        super(app);
        putValue(Action.NAME, "OSX ReOpen Application");
    }

    public void actionPerformed(ActionEvent e) {
        Application a = getApplication();
        if (a.getActiveView() == null && a.views().size() > 0) {
            View v = a.views().iterator().next();
            Component c = SwingUtilities.getRootPane(v.getComponent()).getParent();
            if (c instanceof JFrame) {
                JFrame f = (JFrame) c;
                if ((f.getExtendedState() & JFrame.ICONIFIED) != 0) {
                    f.setExtendedState(f.getExtendedState() ^ JFrame.ICONIFIED);
                }
                f.requestFocus();
            }
        }
    }
}
