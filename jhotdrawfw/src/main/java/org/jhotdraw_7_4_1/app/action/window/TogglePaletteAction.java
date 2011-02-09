/*
 * @(#)TogglePaletteAction.java
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

package org.jhotdraw_7_4_1.app.action.window;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import org.jhotdraw_7_4_1.app.OSXApplication;
import org.jhotdraw_7_4_1.app.action.ActionUtil;

/**
 * TogglePaletteAction.
 * 
 * @author Werner Randelshofer.
 * @version $Id: TogglePaletteAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class TogglePaletteAction extends AbstractAction {
    private Window palette;
    private OSXApplication app;
    private WindowListener windowHandler;
    
    /** Creates a new instance. */
    public TogglePaletteAction(OSXApplication app, Window palette, String label) {
        super(label);
        this.app = app;
        
        windowHandler = new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                putValue(ActionUtil.SELECTED_KEY, false);
            }
        };
        
        putValue(ActionUtil.SELECTED_KEY, false);
        setPalette(palette);
    }
    
    public void putValue(String key, Object newValue) {
        super.putValue(key, newValue);
        if (key == ActionUtil.SELECTED_KEY) {
            if (palette != null) {
                boolean b = (Boolean) newValue;
                if (b) {
                    app.addPalette(palette);
                    palette.setVisible(true);
                } else {
                    app.removePalette(palette);
                    palette.setVisible(false);
                }
            }
        }
    }
    
    public void setPalette(Window newValue) {
        if (palette != null) {
            palette.removeWindowListener(windowHandler);
        }
        
        palette = newValue;
        
        if (palette != null) {
            palette.addWindowListener(windowHandler);
            if (getValue(ActionUtil.SELECTED_KEY) == Boolean.TRUE) {
                app.addPalette(palette);
                palette.setVisible(true);
            } else {
                app.removePalette(palette);
                palette.setVisible(false);
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (palette != null) {
            putValue(ActionUtil.SELECTED_KEY, ! palette.isVisible());
        }
    }
}
