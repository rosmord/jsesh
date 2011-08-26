/*
 * @(#)AppletApplication.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package org.jhotdraw_7_6.app;

import java.awt.Component;

import javax.swing.ActionMap;
import javax.swing.JApplet;
import javax.swing.JMenu;

/**
 * {@code AppletApplication} handles the lifecycle of a single {@link View}
 * inside of a Java Applet.
 * <v>
 * FIXME - To be implemented.
 *
 * @author Werner Randelshofer
 * @version $Id: AppletApplication.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class AppletApplication extends AbstractApplication {
    private JApplet applet;
     private View view;
    
    /** Creates a new instance of AppletApplication */
    public AppletApplication(JApplet applet) {
        this.applet = applet;
    }
    
    
    public void init() {
        super.init();
        initLabels();
        setActionMap(model.createActionMap(this, null));
        model.initApplication(this);
    }
    
    public void show(View v) {
        this.view = v;
        applet.getContentPane().removeAll();
        applet.getContentPane().add(v.getComponent());
        v.start();
        v.activate();
    }

    
    public void hide(View v) {
        v.deactivate();
        v.stop();
        applet.getContentPane().removeAll();
        this.view = null;
    }

    
    public View getActiveView() {
        return view;
    }

    
    public boolean isSharingToolsAmongViews() {
        return false;
    }

    
    public Component getComponent() {
        return applet;
    }

    
    protected ActionMap createViewActionMap(View p) {
        return new ActionMap();
    }

    
    public JMenu createFileMenu(View v) {
        return null;
    }

    
    public JMenu createEditMenu(View v) {
        return null;
    }

    
    public JMenu createViewMenu(View v) {
        return null;
    }
    
    public JMenu createWindowMenu(View v) {
        return null;
    }
    
    public JMenu createHelpMenu(View v) {
        return null;
    }
}
