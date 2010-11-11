/*
 * @(#)AboutAction.java
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
package org.jhotdraw_7_4_1.app.action.app;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

import org.jhotdraw_7_4_1.app.*;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;
import org.jhotdraw_7_4_1.util.*;

/**
 * Displays a dialog showing information about the application.
 * <p>
 * This action is called when the user selects the "About" menu item.
 * The menu item is automatically created by the application.
 * {@link OSXApplication} places the menu item in the "Application" menu,
 * {@link SDIApplication} and {@link MDIApplication} in the "Help" menu.
 * <p>
 * This action is automatically created by the application and put into the
 * {@code ApplicationModel} before {@link ApplicationModel#initApplication} is
 * called.
 *
 * @author  Werner Randelshofer
 * @version $Id: AboutAction.java 608 2010-01-11 18:46:00Z rawcoder $
 */
public class AboutAction extends AbstractApplicationAction {

    public final static String ID = "application.about";

    /** Creates a new instance. */
    public AboutAction(Application app) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, ID);
    }

    public void actionPerformed(ActionEvent evt) {
        Application app = getApplication();

        Component c = app.getComponent();

        // This ensures that we open the option pane on the center of the screen
        // on Mac OS X.
        if (c == null || c.getBounds().isEmpty()) {
            c = null;
        }


        JOptionPane.showMessageDialog(c,
                "<html>" + UIManager.getString("OptionPane.css")
                + "<p><b>" + app.getName() + (app.getVersion() == null ? "" : " " + app.getVersion()) + "</b><br>" + app.getCopyright().replace("\n", "<br>")
                + "<br><br>Running on"
                + "<br>  Java: " + System.getProperty("java.version")
                + ", " + System.getProperty("java.vendor")
                + "<br>  JVM: " + System.getProperty("java.vm.version")
                + ", " + System.getProperty("java.vm.vendor")
                + "<br>  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + ", " + System.getProperty("os.arch"),
                "About", JOptionPane.PLAIN_MESSAGE);
    }
}
