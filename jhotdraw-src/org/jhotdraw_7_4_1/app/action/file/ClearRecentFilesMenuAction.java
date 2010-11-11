/*
 * @(#)ClearRecentFilesMenuAction.java
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

package org.jhotdraw_7_4_1.app.action.file;

import java.awt.event.ActionEvent;
import java.beans.*;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;
import org.jhotdraw_7_4_1.util.*;

/**
 * Clears (empties) the Recent Files sub-menu in the File menu.
 * <p>
 * This action is called when the user selects the Clear Recent Files item in
 * the Recent Files sub-menu of the File menu. The action and the menu item
 * is automatically created by the application, when the
 * {@code ApplicationModel} provides a {@code LoadFileAction} or a
 * {@code OpenFileAction}.
 *
 *
 * @author Werner Randelshofer.
 * @version $Id: ClearRecentFilesMenuAction.java 609 2010-01-11 19:06:35Z rawcoder $
 */
public class ClearRecentFilesMenuAction extends AbstractApplicationAction {
    public final static String ID = "file.clearRecentFiles";
    
    private PropertyChangeListener applicationListener;
    
    /** Creates a new instance. */
    public ClearRecentFilesMenuAction(Application app) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, ID);
        updateEnabled();
    }

    /**
     * Installs listeners on the application object.
     */
    @Override protected void installApplicationListeners(Application app) {
        super.installApplicationListeners(app);
        if (applicationListener == null) {
            applicationListener = createApplicationListener();
        }
        app.addPropertyChangeListener(applicationListener);
    }
    private PropertyChangeListener createApplicationListener() {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == "recentFiles") { // Strings get interned
                    updateEnabled();
                }
            }
        };
    }
    /**
     * Installs listeners on the application object.
     */
    @Override protected void uninstallApplicationListeners(Application app) {
        super.uninstallApplicationListeners(app);
        app.removePropertyChangeListener(applicationListener);
    }
    
    public void actionPerformed(ActionEvent e) {
        getApplication().clearRecentURIs();
    }
    
    private void updateEnabled() {
        setEnabled(getApplication().getRecentURIs().size() > 0);
        
    }
    
}
