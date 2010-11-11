/*
 * @(#)ApplicationModel.java
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

package org.jhotdraw_7_4_1.app;

import java.net.URI;
import java.util.*;
import javax.swing.*;

import org.jhotdraw_7_4_1.gui.URIChooser;
import org.qenherkhopeshef.jhotdrawChanges.StandardMenuBuilder;
/**
 * {@code ApplicationModel} provides meta-data for an {@link Application},
 * actions and factory methods for creating {@link View}s and toolbars.
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p><em>Framework</em><br>
 * The interfaces and classes listed below together with the {@code Action}
 * classes in the org.jhotddraw.app.action package define the contracts of a
 * framework for document oriented applications:<br>
 * Contract: {@link Application}, {@link ApplicationModel}, {@link View}.
 * <hr>
 *
 * @author Werner Randelshofer.
 * @version $Id: ApplicationModel.java 609 2010-01-11 19:06:35Z rawcoder $
 */
public interface ApplicationModel {
    /**
     * Returns the name of the application.
     */
    public String getName();
    /**
     * Returns the version of the application.
     */
    public String getVersion();
    /**
     * Returns the copyright of the application.
     */
    public String getCopyright();
    
    /**
     * Creates a new view for the application.
     */
    public View createView();

    /** Inits the application. */
    public void initApplication(Application a);
    /** Destroys the application. */
    public void destroyApplication(Application a);
    /** Inits the supplied view for the application. */
    public void initView(Application a, View v);
    /** Destroys the supplied view. */
    public void destroyView(Application a, View v);

    /** Creates an action map.
     * <p>
     * This method is invoked once for the application, and once for each
     * created view.
     * <p>
     * The application adds the created map to a hierarchy of action maps.
     * Thus actions created for the application are accessible from the
     * action maps of the views.
     *
     * @param a Application.
     * @param v The view for which the toolbars need to be created, or null
     * if the actions are shared by multiple views.
     */
    public ActionMap createActionMap(Application a, View v);
    /**
     * Creates tool bars.
     * <p>
     * Depending on the document interface of the application, this method
     * may be invoked only once for the application, or for each opened view.
     * <p>
     * @param a Application.
     * @param v The view for which the toolbars need to be created, or null
     * if the toolbars are shared by multiple views.
     */
    public List<JToolBar> createToolBars(Application a, View v);
    
    /**
     * Creates menus.
     * <p>
     * Depending on the document interface of the application, this method
     * may be invoked only once for the application, or for each opened view.
     * <p>
     * If this method creates a menu with the same title as a standard menu
     * created by {@code Application}, the menu created by this method is used.
     * This method can create a standard menu from scratch, or call one of the
     * createMenu-methods in {@code Application} and add additional items
     * to the menu.
     * 
     * @param a Application.
     * @param v The view for which the toolbars need to be created, or null
     * if the menus are shared by multiple views.
     */
    public List<JMenu> createMenus(Application a, View v);

    /**
     * Creates an open chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     */
    public URIChooser createOpenChooser(Application a, View v);
    /**
     * Creates an open chooser for directories.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     */
    public URIChooser createOpenDirectoryChooser(Application a, View v);
    /**
     * Creates a save chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     */
    public URIChooser createSaveChooser(Application a, View v);
    /**
     * Creates an import chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     */
    public URIChooser createImportChooser(Application a, View v);
    /**
     * Creates an export chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     */
    public URIChooser createExportChooser(Application a, View v);


    /**
     * Returns a menu builder which will be used to insert non-standard entries in standard menus. 
     * @return
     */
    public StandardMenuBuilder getStandardMenuBuilder();
}
