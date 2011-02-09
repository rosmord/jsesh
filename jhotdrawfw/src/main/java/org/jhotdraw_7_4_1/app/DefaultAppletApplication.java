/*
 * @(#)DefaultAppletApplication.java
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

package org.jhotdraw_7_4_1.app;

import java.awt.*;
import javax.swing.*;

/**
 * Default Application that can be run as an Applet.
 * <v>
 * FIXME - To be implemented.
 *
 * @author Werner Randelshofer
 * @version $Id: DefaultAppletApplication.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class DefaultAppletApplication extends AbstractApplication {
    private JApplet applet;
    private View view;
    
    /** Creates a new instance of DefaultAppletApplication */
    public DefaultAppletApplication(JApplet applet) {
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
