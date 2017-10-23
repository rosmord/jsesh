/*
 * @(#)AbstractFindAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package org.jhotdraw_7_6.app.action.edit;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.ApplicationModel;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Perform the last search once more, advancing in the found item list.
 * <p>
 * This action is called when the user selects the Find Next item in the Edit
 * menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link ApplicationModel#initApplication}.
 *
 * @author Serge Rosmorduc
 */
public abstract class AbstractFindNextAction extends AbstractViewAction {
    public final static String ID = "edit.findNext";
    
    /** Creates a new instance.
     * @param app the application
     * @param view the view
     */
    public AbstractFindNextAction(Application app,  View view) {
        super(app, view);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        labels.configureAction(this, ID);
    }    
}
