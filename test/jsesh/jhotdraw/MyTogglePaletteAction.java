/*
 * @(#)MyTogglePaletteAction.java
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

package jsesh.jhotdraw;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;

import org.jhotdraw_7_4_1.app.AbstractApplication;
import org.jhotdraw_7_4_1.app.action.ActionUtil;

/**
 * MyTogglePaletteAction.
 * 
 * @author Werner Randelshofer.
 * @version $Id: MyTogglePaletteAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class MyTogglePaletteAction extends AbstractAction {
    private Window palette;
    private AbstractApplication app;
    private WindowListener windowHandler;
    
    /** Creates a new instance. */
    public MyTogglePaletteAction(AbstractApplication myOSXApplication, Window palette, String label) {
        super(label);
        this.app = myOSXApplication;
        
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
    
    private void setPalette(Window newValue) {
        if (palette != null) {
            palette.removeWindowListener(windowHandler);
        }
        
        palette = newValue;
        
        if (palette != null) {
            palette.addWindowListener(windowHandler);
            if (getValue(ActionUtil.SELECTED_KEY) == Boolean.TRUE) {
                app.addPalette(palette);
                //palette.setVisible(true);
            } else {
                app.removePalette(palette);
                //palette.setVisible(false);
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (palette != null) {
            //putValue(ActionUtil.SELECTED_KEY, ! palette.isVisible());
        }
    }
}
