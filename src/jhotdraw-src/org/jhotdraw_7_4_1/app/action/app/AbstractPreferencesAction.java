/*
 * @(#)AbstractPreferencesAction.java
 * 
 * Copyright (c) 2009-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 * 
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw_7_4_1.app.action.app;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;
import org.jhotdraw_7_4_1.util.ResourceBundleUtil;

/**
 * Displays a preferences dialog for the application.
 * <p>
 * This action is called when the user selects the Preferences item in the
 * Application menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create an action
 * with this ID and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_4_1.app.ApplicationModel#initApplication}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-09-06 Created.
 */
public abstract class AbstractPreferencesAction extends AbstractApplicationAction {

    public final static String ID = "application.preferences";

    /** Creates a new instance. */
    public AbstractPreferencesAction(Application app) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, ID);
    }
}
