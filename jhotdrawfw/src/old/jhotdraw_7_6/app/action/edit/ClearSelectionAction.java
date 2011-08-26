/*
 * @(#)ClearSelectionAction.java
 *
 * Copyright (c) 2008 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw_7_6.app.action.edit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.jhotdraw_7_6.app.EditableComponent;
import org.jhotdraw_7_6.app.action.AbstractSelectionAction;
import org.jhotdraw_7_6.util.*;

/**
 * Clears (de-selects) the selected region.
 * <p>
 * This action acts on the last {@link org.jhotdraw_7_6.app.EditableComponent} /
 * {@code JTextComponent} which had the focus when the {@code ActionEvent}
 * was generated.
 * <p>
 * This action is called when the user selects the Clear Selection item in the
 * Edit menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}.
 *
 * @author Werner Randelshofer.
 * @version $Id: ClearSelectionAction.java 614 2010-01-13 21:55:10Z rawcoder $
 */
public class ClearSelectionAction extends AbstractSelectionAction {

    public final static String ID = "edit.clearSelection";

    /** Creates a new instance which acts on the currently focused component. */
    public ClearSelectionAction() {
        this(null);
    }

    /** Creates a new instance which acts on the specified component.
     *
     * @param target The target of the action. Specify null for the currently
     * focused component.
     */
    public ClearSelectionAction(JComponent target) {
        super(target);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
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
                ((EditableComponent) c).clearSelection();
            } else if (c instanceof JTextComponent) {
                JTextComponent tc = ((JTextComponent) c);
                tc.select(tc.getSelectionStart(), tc.getSelectionStart());
            } else {
                c.getToolkit().beep();
            }
        }
    }

    @Override
    protected void updateEnabled() {
        if (target != null) {
            setEnabled(target.isEnabled());
        }
    }
}
