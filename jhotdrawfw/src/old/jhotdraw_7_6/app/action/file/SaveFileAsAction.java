/*
 * @(#)SaveFileAsAction.java
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
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.util.*;

/**
 * Presents an {@code URIChooser} and then saves the active view to the
 * specified location.
 * <p>
 * This action is called when the user selects the Save As item in the File
 * menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create it
 * and put it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw_7_6.app.ApplicationModel#initApplication}.
 *
 * @author  Werner Randelshofer
 * @version $Id: SaveFileAsAction.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class SaveFileAsAction extends SaveFileAction {
    public final static String ID = "file.saveAs";

    /** Creates a new instance. */
    public SaveFileAsAction(Application app, View view) {
        super(app, view, true);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
        labels.configureAction(this, ID);
    }
}