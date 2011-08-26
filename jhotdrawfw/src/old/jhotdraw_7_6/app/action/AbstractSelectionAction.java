/*
 * @(#)AbstractSelectionAction.java
 * 
 * Copyright (c) 2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 * 
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw_7_6.app.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;

import org.jhotdraw_7_6.app.EditableComponent;
import org.jhotdraw_7_6.beans.WeakPropertyChangeListener;

/**
 * {@code AbstractSelectionAction} acts on the selection of a target component.
 * <p>
 * By default, the action is disabled when the target component is disabled or has
 * no selection.  * If the target component is null, updateEnabled does nothing.
 * You can change this behavior by overriding method {@code updateEnabled()}.
 * <p>
 * This action registers a {@link WeakPropertyChangeListener} on the component.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-01-10 Created.
 */
public abstract class AbstractSelectionAction extends AbstractAction {

    /** The target of the action or null if the action acts on the currently
     * focused component.
     */
    protected JComponent target;
    /** This variable keeps a strong reference on the property change listener. */
    private PropertyChangeListener propertyHandler;

    /** Creates a new instance which acts on the specified component.
     *
     * @param target The target of the action. Specify null for the currently
     * focused component.
     */
    public AbstractSelectionAction(JComponent target) {
        this.target = target;
        if (target != null) {
            // Register with a weak reference on the JComponent.
            propertyHandler = new PropertyChangeListener() {

                
                public void propertyChange(PropertyChangeEvent evt) {
                    String n = evt.getPropertyName();

                    if (n.equals("enabled")) {
                        updateEnabled();
                    } else if (n.equals(EditableComponent.SELECTION_EMPTY_PROPERTY)) {
                        updateEnabled();
                    }
                }
            };
            target.addPropertyChangeListener(new WeakPropertyChangeListener(propertyHandler));
        }
    }

    protected void updateEnabled() {
        if (target instanceof EditableComponent) {
            setEnabled(target.isEnabled() && !((EditableComponent) target).isSelectionEmpty());
        } else if (target != null) {
            setEnabled(target.isEnabled());
        }
    }
}
