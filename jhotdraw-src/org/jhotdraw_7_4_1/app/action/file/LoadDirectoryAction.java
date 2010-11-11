/*
 * @(#)LoadDirectoryAction.java
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

package org.jhotdraw_7_4_1.app.action.file;

import org.jhotdraw_7_4_1.app.*;
import org.jhotdraw_7_4_1.gui.URIChooser;
import org.jhotdraw_7_4_1.util.ResourceBundleUtil;

/**
 * Les the user save unsaved changes of the active view, then presents
 * an {@code URIChooser} and then loads the selected URI into the active view.
 * <p>
 * This action is called when the user selects the Load Directory item in the File
 * menu. The menu item is automatically created by the application.
 * <p>
 * This action is designed for applications which do not automatically
 * create a new view for each opened file. This action goes together with
 * {@code ClearFileAction}, {@code NewWindowAction}, {@code LoadFileAction},
 * {@code LoadDirectoryAction} and {@code CloseFileAction}.
 * This action should not be used together with {@code OpenDirectoryAction}.
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee
 * @version $Id: LoadDirectoryAction.java 609 2010-01-11 19:06:35Z rawcoder $
 */
public class LoadDirectoryAction extends LoadFileAction {
    public final static String ID = "file.loadDirectory";

    /** Creates a new instance. */
    public LoadDirectoryAction(Application app, View view) {
        super(app, view);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        labels.configureAction(this, ID);
    }
    @Override
    protected URIChooser getChooser(View view) {
        return getApplication().getModel().createOpenDirectoryChooser(getApplication(), view);
    }
}
