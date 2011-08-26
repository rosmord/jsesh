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

package org.jhotdraw_7_6.app.action.file;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.util.*;

/**
 * Creates a new view.
 * <p>
 * This action is called when the user selects the New item in the File
 * menu.
 * <p>
 * If you want this behavior in your application, you have to create it
 * and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}.
 * <p>
 * This action is designed for applications which do not automatically
 * create a new view for each opened file. This action goes together with
 * {@link ClearFileAction}, {@link LoadFileAction} and {@link CloseFileAction}.
 * It should not be used together with {@link NewFileAction}.
 *
 * @author Werner Randelshofer
 * @version $Id: NewWindowAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class NewWindowAction extends NewFileAction {
    public final static String ID = "file.newWindow";
    
    /** Creates a new instance. */
    public NewWindowAction(Application app) {
        super(app, ID);
    }
}
