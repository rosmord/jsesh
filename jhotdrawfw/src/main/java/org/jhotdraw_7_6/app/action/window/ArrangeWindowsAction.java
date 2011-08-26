/*
 * @(#)ArrangeWindowsAction.java
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

import javax.swing.AbstractAction;

import org.jhotdraw_7_6.gui.Arrangeable;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Changes the arrangement of an {@link Arrangeable} object.
 * <p>
 * If you want this behavior in your application, you have to create it
 * and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}.
 *
 * FIXME - Register as PropertyChangeListener on Arrangeable.
 *
 * @author Werner Randelshofer
 * @version $Id: ArrangeWindowsAction.java 719 2010-11-22 16:53:51Z rawcoder $
 */
public class ArrangeWindowsAction extends AbstractAction {
    public final static String VERTICAL_ID = "window.arrangeVertical";
    public final static String HORIZONTAL_ID = "window.arrangeHorizontal";
    public final static String CASCADE_ID = "window.arrangeCascade";
    private Arrangeable arrangeable;
    private Arrangeable.Arrangement arrangement;
    
    /** Creates a new instance. */
    public ArrangeWindowsAction(Arrangeable arrangeable, Arrangeable.Arrangement arrangement) {
        this.arrangeable = arrangeable;
        this.arrangement = arrangement;
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        String labelID;
        switch (arrangement) {
            case VERTICAL : labelID = VERTICAL_ID; break;
            case HORIZONTAL : labelID = HORIZONTAL_ID; break;
            case CASCADE :
            default :
                labelID = CASCADE_ID; break;
        }
        labels.configureAction(this, labelID);
    }
    
    
    public void actionPerformed(ActionEvent e) {
            arrangeable.setArrangement(arrangement);
    }
}
