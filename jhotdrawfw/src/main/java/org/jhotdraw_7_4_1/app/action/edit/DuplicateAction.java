/*
 * @(#)DuplicateAction.java
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

package org.jhotdraw_7_4_1.app.action.edit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jhotdraw_7_4_1.app.EditableComponent;
import org.jhotdraw_7_4_1.app.action.AbstractSelectionAction;
import org.jhotdraw_7_4_1.util.*;

/**
 * Duplicates the selected region.
 * <p>
 * This action acts on the last {@link org.jhotdraw_7_4_1.app.EditableComponent} /
 * {@code JTextComponent} which had the focus when the {@code ActionEvent}
 * was generated.
 * <p>
 * This action is called when the user selects the Duplicate item in the Edit
 * menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_4_1.app.ApplicationModel#initApplication}.
 *
 * @author Werner Randelshofer.
 * @version $Id: DuplicateAction.java 605 2010-01-10 11:14:33Z rawcoder $
 */
public class DuplicateAction extends AbstractSelectionAction {
    public final static String ID = "edit.duplicate";
    
    /** Creates a new instance which acts on the currently focused component. */
    public DuplicateAction() {
        this(null);
    }

    /** Creates a new instance which acts on the specified component.
     *
     * @param target The target of the action. Specify null for the currently
     * focused component.
     */
    public DuplicateAction(JComponent target) {
        super(target);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, ID);
    }
    
  
    public void actionPerformed(ActionEvent evt) {
        JComponent c = target;
        if (c == null && (KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getPermanentFocusOwner() instanceof JComponent)) {
            c = (JComponent) KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    getPermanentFocusOwner();
        }
        if (c != null && c.isEnabled()) {
            if (c instanceof EditableComponent) {
                ((EditableComponent) c).duplicate();
            } else {
                c.getToolkit().beep();
            }
        }
    }
}
