/*
 * @(#)NewFileAction.java
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

import java.awt.event.*;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;
import org.jhotdraw_7_4_1.util.*;

/**
 * Creates a new view.
 * <p>
 * This action is called when the user selects the New item in the File
 * menu.
 * <p>
 * If you want this behavior in your application, you have to create it
 * and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_4_1.app.ApplicationModel#initApplication}.
 * <p>
 * This action is designed for applications which automatically create
 * a new view for each opened file. This action goes together with
 * {@link OpenFileAction} and {@link CloseFileAction}. It should
 * not be used together with {@link NewWindowAction}.
 *
 * @author Werner Randelshofer
 * @version $Id: NewFileAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class NewFileAction extends AbstractApplicationAction {
    public final static String ID = "file.new";
    
    /** Creates a new instance. */
    public NewFileAction(Application app) {
        this(app,ID);
    }
    public NewFileAction(Application app, String id) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, id);
    }
    
    public void actionPerformed(ActionEvent evt) {
        Application app = getApplication();
        final View newP = app.createView();
        int multiOpenId = 1;
        for (View existingP : app.views()) {
            if (existingP.getURI() == null) {
                multiOpenId = Math.max(multiOpenId, existingP.getMultipleOpenId() + 1);
            }
        }
        newP.setMultipleOpenId(multiOpenId);
        app.add(newP);
        newP.execute(new Runnable() {
            public void run() {
                newP.clear();
            }
        });
        app.show(newP);
    }
}
