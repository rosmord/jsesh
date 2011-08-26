/*
 * @(#)ToggleToolBarAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package org.jhotdraw_7_6.app.action.window;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;

import org.jhotdraw_7_6.app.action.ActionUtil;

/**
 * ToggleToolBarAction.
 * 
 * @author Werner Randelshofer
 * @version $Id: ToggleToolBarAction.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class ToggleToolBarAction extends AbstractAction {
    private JToolBar toolBar;
    private PropertyChangeListener propertyHandler;
    
    /** Creates a new instance. */
    public ToggleToolBarAction(JToolBar toolBar, String label) {
        super(label);
        
        propertyHandler = new PropertyChangeListener() {
            
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals("visible")) {
                    putValue(ActionUtil.SELECTED_KEY, evt.getNewValue());
                }
            }            
        };
        
        putValue(ActionUtil.SELECTED_KEY, true);
        setToolBar(toolBar);
    }
    
    
    public void putValue(String key, Object newValue) {
        super.putValue(key, newValue);
        if (key == ActionUtil.SELECTED_KEY) {
            if (toolBar != null) {
                toolBar.setVisible((Boolean) newValue);
            }
        }
    }
    
    public void setToolBar(JToolBar newValue) {
        if (toolBar != null) {
            toolBar.removePropertyChangeListener(propertyHandler);
        }
        
        toolBar = newValue;
 
        if (toolBar != null) {
            toolBar.addPropertyChangeListener(propertyHandler);
            putValue(ActionUtil.SELECTED_KEY, toolBar.isVisible());
        }
    }
    
    
    public void actionPerformed(ActionEvent e) {
        if (toolBar != null) {
            putValue(ActionUtil.SELECTED_KEY, ! toolBar.isVisible());
        }
    }
}
