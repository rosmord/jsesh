/*
 * @(#)DefaultMenuBuilder.java
 * 
 * Copyright (c) 2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app;

import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;

import org.jhotdraw_7_6.app.action.app.AboutAction;
import org.jhotdraw_7_6.app.action.app.AbstractPreferencesAction;
import org.jhotdraw_7_6.app.action.app.ExitAction;
import org.jhotdraw_7_6.app.action.edit.AbstractFindAction;
import org.jhotdraw_7_6.app.action.edit.AbstractFindNextAction;
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
import org.jhotdraw_7_6.app.action.file.ExportFileAction;
import org.jhotdraw_7_6.app.action.file.LoadDirectoryAction;
import org.jhotdraw_7_6.app.action.file.LoadFileAction;
import org.jhotdraw_7_6.app.action.file.NewFileAction;
import org.jhotdraw_7_6.app.action.file.NewWindowAction;
import org.jhotdraw_7_6.app.action.file.OpenDirectoryAction;
import org.jhotdraw_7_6.app.action.file.OpenFileAction;
import org.jhotdraw_7_6.app.action.file.PrintFileAction;
import org.jhotdraw_7_6.app.action.file.SaveFileAction;
import org.jhotdraw_7_6.app.action.file.SaveFileAsAction;

/**
 * {@code DefaultMenuBuilder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-11-14 Created.
 */
public class DefaultMenuBuilder implements MenuBuilder {

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link AbstractPreferencesAction}</li>
     * </ul>
     */
    public void addPreferencesItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(AbstractPreferencesAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link ExitAction}</li>
     * </ul>
     */
    public void addExitItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(ExitAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link ClearFileAction}</li>
     * </ul>
     */
    public void addClearFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(ClearFileAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link NewWindowAction}</li>
     * </ul>
     */
    public void addNewWindowItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(NewWindowAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link NewFileAction}</li>
     * </ul>
     */
    public void addNewFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(NewFileAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link LoadFileAction}</li>
     * <li>{@link LoadDirectoryAction}</li>
     * </ul>
     */
    public void addLoadFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(LoadFileAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(LoadDirectoryAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link OpenFileAction}</li>
     * <li>{@link OpenDirectoryAction}</li>
     * </ul>
     */
    public void addOpenFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(OpenFileAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(OpenDirectoryAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link CloseFileAction}</li>
     * </ul>
     */
    public void addCloseFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(CloseFileAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link SaveFileAction}</li>
     * <li>{@link SaveFileAsAction}</li>
     * </ul>
     */
    public void addSaveFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(SaveFileAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(SaveFileAsAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link ExportFileAction}</li>
     * </ul>
     */
    public void addExportFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(ExportFileAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link PrintFileAction}</li>
     * </ul>
     */
    public void addPrintFileItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(PrintFileAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Does nothing.
     */
    public void addOtherFileItems(JMenu m, Application app, View v) {
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link UndoAction}</li>
     * <li>{@link RedoAction}</li>
     * </ul>
     */
    public void addUndoItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(UndoAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(RedoAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link CutAction}</li>
     * <li>{@link CopyAction}</li>
     * <li>{@link PasteAction}</li>
     * <li>{@link DuplicateAction}</li>
     * <li>{@link DeleteAction}</li>
     * </ul>
     */
    public void addClipboardItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(CutAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(CopyAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(PasteAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(DuplicateAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(DeleteAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link SelectAllAction}</li>
     * <li>{@link ClearSelectionAction}</li>
     * </ul>
     */
    public void addSelectionItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(SelectAllAction.ID))) {
            m.add(a);
        }
        if (null != (a = am.get(ClearSelectionAction.ID))) {
            m.add(a);
        }
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link AbstractFindAction}</li>
     * </ul>
     */
    @Override
    public void addFindItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action find;
        if (null != (find = am.get(AbstractFindAction.ID))) {
            m.add(find);
        }
        Action findNext;
        if (null != (findNext = am.get(AbstractFindNextAction.ID))) {
            m.add(findNext);
        }
    }

    /**
     * Does nothing.
     */
    public void addOtherEditItems(JMenu m, Application app, View v) {
    }

    /**
     * Does nothing.
     */
    public void addOtherViewItems(JMenu m, Application app, View v) {
    }

    /**
     * Does nothing.
     */
    public void addOtherMenus(List<JMenu> m, Application app, View v) {
    }

    /**
     * Does nothing.
     */
    public void addOtherWindowItems(JMenu m, Application app, View v) {
    }

    /**
     * Does nothing.
     */
    public void addHelpItems(JMenu m, Application app, View v) {
    }

    /**
     * Adds items for the following actions to the menu:
     * <ul>
     * <li>{@link AboutAction}</li>
     * </ul>
     */
    public void addAboutItems(JMenu m, Application app, View v) {
        ActionMap am = app.getActionMap(v);
        Action a;
        if (null != (a = am.get(AboutAction.ID))) {
            m.add(a);
        }
    }
}
