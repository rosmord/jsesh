/*
 * @(#)EmptyApplicationModel.java
 *
 * Copyright (c) 2009-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app;

import java.util.Collections;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JToolBar;

/**
 * An {@link ApplicationModel} which neither creates {@code Action}s,
 * nor overrides the menu bars, nor creates tool bars.
 * <p>
 * The {@code createActionMap} method of this model returns an empty ActionMap.
 *
 * @author Werner Randelshofer.
 * @version $Id: EmptyApplicationModel.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class EmptyApplicationModel
        extends AbstractApplicationModel {

    /** Returns an empty ActionMap. */
    
    public ActionMap createActionMap(Application a,  View v) {
        return new ActionMap();
    }

    /** Returns an empty unmodifiable list. */
    
    public List<JToolBar> createToolBars(Application app,  View v) {
        return Collections.emptyList();
    }

    
    public MenuBuilder getMenuBuilder() {
        return new EmptyMenuBuilder();
    }
}
