/*
 * @(#)DefaultApplicationModel.java
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
package org.jhotdraw_7_6.app;

import java.util.*;
import javax.swing.*;

import org.jhotdraw_7_6.app.action.edit.ClearSelectionAction;
import org.jhotdraw_7_6.app.action.edit.CopyAction;
import org.jhotdraw_7_6.app.action.edit.CutAction;
import org.jhotdraw_7_6.app.action.edit.DeleteAction;
import org.jhotdraw_7_6.app.action.edit.DuplicateAction;
import org.jhotdraw_7_6.app.action.edit.PasteAction;
import org.jhotdraw_7_6.app.action.edit.RedoAction;
import org.jhotdraw_7_6.app.action.edit.SelectAllAction;
import org.jhotdraw_7_6.app.action.edit.UndoAction;
import org.jhotdraw_7_6.app.action.file.ClearFileAction;
import org.jhotdraw_7_6.app.action.file.CloseFileAction;
import org.jhotdraw_7_6.app.action.file.NewFileAction;
import org.jhotdraw_7_6.app.action.file.OpenFileAction;
import org.jhotdraw_7_6.app.action.file.SaveFileAction;
import org.jhotdraw_7_6.app.action.file.SaveFileAsAction;

/**
 * DefaultApplicationModel.
 *
 * @author Werner Randelshofer.
 * @version $Id: DefaultApplicationModel.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class DefaultApplicationModel
        extends AbstractApplicationModel {

    
    public void initView(Application a, View v) {
    }

    
    public ActionMap createActionMap(Application a, View v) {
        ActionMap m=new ActionMap();
        m.put(NewFileAction.ID, new NewFileAction(a));
        m.put(OpenFileAction.ID, new OpenFileAction(a));
        m.put(SaveFileAction.ID, new SaveFileAction(a,v));
        m.put(SaveFileAsAction.ID, new SaveFileAsAction(a,v));
        m.put(CloseFileAction.ID, new CloseFileAction(a,v));

        m.put(UndoAction.ID, new UndoAction(a,v));
        m.put(RedoAction.ID, new RedoAction(a,v));
        m.put(CutAction.ID, new CutAction());
        m.put(CopyAction.ID, new CopyAction());
        m.put(PasteAction.ID, new PasteAction());
        m.put(DeleteAction.ID, new DeleteAction());
        m.put(DuplicateAction.ID, new DuplicateAction());
        m.put(SelectAllAction.ID, new SelectAllAction());
        m.put(ClearSelectionAction.ID, new ClearSelectionAction());
        return m;
    }

    
    public List<JToolBar> createToolBars(Application app, View p) {
        return Collections.emptyList();
    }

    
    public List<JMenu> createMenus(Application a, View v) {
        LinkedList<JMenu> menus = new LinkedList<JMenu>();
        JMenu m;
        if ((m = createFileMenu(a, v)) != null) {
            menus.add(m);
        }
        if ((m = createEditMenu(a, v)) != null) {
            menus.add(m);
        }
        if ((m = createViewMenu(a, v)) != null) {
            menus.add(m);
        }
        if ((m = createWindowMenu(a, v)) != null) {
            menus.add(m);
        }
        if ((m = createHelpMenu(a, v)) != null) {
            menus.add(m);
        }
        return menus;
    }

    protected JMenu createFileMenu(Application app, View view) {
        return null;
    }

    protected JMenu createEditMenu(Application app, View view) {
        return null;
    }

    protected JMenu createViewMenu(Application app, View view) {
        return null;
    }

    protected JMenu createWindowMenu(Application app, View view) {
        return null;
    }

    protected JMenu createHelpMenu(Application app, View view) {
        return null;
    }
}
