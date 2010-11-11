/*
 * @(#)EmptyApplicationModel.java
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
package org.jhotdraw_7_4_1.app;

import java.util.*;
import javax.swing.*;

/**
 * EmptyApplicationModel.
 *
 * @author Werner Randelshofer.
 * @version $Id: EmptyApplicationModel.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class EmptyApplicationModel
        extends AbstractApplicationModel {

   
    public ActionMap createActionMap(Application a, View v) {
        return new ActionMap();
    }

   
    public List<JToolBar> createToolBars(Application app, View v) {
        return Collections.emptyList();
    }

   
    public List<JMenu> createMenus(Application a, View v) {
        LinkedList<JMenu> menus = new LinkedList<JMenu>();
        JMenu m;
        if ((m=createFileMenu(a,v))!=null) {
            menus.add(m);
        }
        if ((m=createEditMenu(a,v))!=null) {
            menus.add(m);
        }
        if ((m=createViewMenu(a,v))!=null) {
            menus.add(m);
        }
        if ((m=createWindowMenu(a,v))!=null) {
            menus.add(m);
        }
        if ((m=createHelpMenu(a,v))!=null) {
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
