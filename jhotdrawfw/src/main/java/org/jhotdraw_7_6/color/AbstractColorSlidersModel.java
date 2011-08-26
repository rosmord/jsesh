/**
 * @(#)AbstractColorSlidersModel.java
 *
 * Copyright (c) 2008 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.color;

import java.util.LinkedList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jhotdraw_7_6.beans.AbstractBean;

/**
 * AbstractColorSlidersModel.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractColorSlidersModel.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public abstract class AbstractColorSlidersModel extends AbstractBean implements ColorSliderModel {

    /**
     * ChangeListener's listening to changes in this model.
     */
    protected LinkedList<ChangeListener> listeners;

    
    public void addChangeListener(ChangeListener l) {
        if (listeners == null) {
            listeners = new LinkedList<ChangeListener>();
        }
        listeners.add(l);
    }

    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public void fireStateChanged() {
        if (listeners != null) {
            ChangeEvent event = new ChangeEvent(this);
            for (ChangeListener l : listeners) {
                l.stateChanged(event);
            }
        }
    }
}
