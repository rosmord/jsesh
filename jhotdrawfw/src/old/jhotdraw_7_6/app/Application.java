/*
 * @(#)Application.java
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

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import java.net.URI;

import org.jhotdraw_7_6.gui.URIChooser;

/**
 * An <em>application</em> handles the lifecycle of {@link View} objects and
 * provides windows to present them on screen.
 * <p>
 * An application owns a {@link ApplicationModel} which provides meta-data about
 * the application, actions and factory methods for creating the views.
 * <p>
 * Depending on the document interface style used by the application, an
 * application can handle multiple views at the same time, or only one.
 * <p>
 * Typical document interface styles are the Single Document Interface (SDI),
 * the Multiple Document Interface (MDI) and the Mac OS X Application Document
 * Interface (OSX). Typically, for each of these styles an implementation of
 * {@code Application} exists.
 * <p>
 * Typical usage of this class:
 * <pre>
 * public class MyMainClass {
 *     public static void main(String[] args) {
 *         Application app = new SDIApplication(); // or OSXApplication(), MDIApplication().
 *         DefaultApplicationModel model = new DefaultApplicationModel();
 *         model.setName("MyApplication");
 *         model.setVersion("1.0");
 *         model.setCopyright("Copyright 2006 (c) Werner Randelshofer. All Rights Reserved.");
 *         model.setViewClassName("org.jhotdraw_7_6.myapplication.MyView");
 *         app.setModel(model);
 *         app.launch(args);
 *     } 
 * </pre>
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
 * @author Werner Randelshofer
 * @version $Id: Application.java 607 2010-01-11 18:41:59Z rawcoder $
 */
public interface Application {

    /**
     * The property name of the activeView property.
     */
    public final static String ACTIVE_VIEW_PROPERTY = "activeView";
    /**
     * The property name of the recentURIs property.
     */
    public final static String RECENT_URIS_PROPERTY = "recentURIs";

    /**
     * Launches the application from the main method.
     * This method is typically invoked on the main Thread.
     * This will invoke configure() on the current thread and then 
     * init() and start() on the AWT Event Dispatcher Thread.
     */
    public void launch(String[] args);

    /**
     * Configures the application using the provided arguments array.
     */
    public void configure(String[] args);

    /**
     * Initializes the application.
     * <code>configure()</code> should have been invoked before the application
     * is inited. Alternatively an application can be configured using setter
     * methods.
     */
    public void init();

    /**
     * Starts the application.
     * This usually creates a new view, and adds it to the application.
     * <code>init()</code> must have been invoked before the application is started.
     */
    public void start();

    /**
     * Stops the application without saving any unsaved views.
     * <code>init()</code> must have been invoked before the application is stopped.
     */
    public void stop();
    /**
     * Destroys the application and calls System.exit(0).
     */
    public void destroy();

    /**
     * Creates a new view for this application and initializes it, by calling
     * {@link View#init}.
     */
    public View createView();

    /**
     * Adds a view to this application.
     * Fires a "documentCount" property change event.
     * Invokes method setApplication(this) on the view object.
     */
    public void add(View p);

    /**
     * Removes a view from this application and removes it from the users
     * view.
     * Fires a "documentCount" property change event.
     * Invokes method setApplication(null) on the view object.
     */
    public void remove(View p);

    /**
     * Shows a view.
     */
    public void show(View p);

    /**
     * Hides a view.
     */
    public void hide(View p);

    /**
     * This is a convenience method for removing a view and disposing it.
     */
    public void dispose(View p);

    /**
     * Returns a read only collection view of the views of this application.
     */
    public Collection<View> views();

    /**
     * Returns the active view. This is used for OSXApplication and 
     * MDIApplication which share actions among multiple View instances.
     * Active view may be become null, if the
     * application has no view.
     * <p>
     * This is a bound property. 
     */
    public View getActiveView();

    /**
     * Returns the enabled state of the application.
     */
    public boolean isEnabled();

    /**
     * Sets the enabled state of the application.
     *
     * The enabled state is used to prevent parallel invocation of actions
     * on the application. If an action consists of a sequential part and a
     * concurrent part, it must disable the application only for the sequential
     * part.
     *
     * Actions that act on the application must check in their actionPerformed
     * method whether the application is enabled.
     * If the application is disabled, they must do nothing. 
     * If the application is enabled, they must disable the application,
     * perform the action and then enable the application again.
     *
     * This is a bound property.
     */
    public void setEnabled(boolean newValue);

    /**
     * Adds a property change listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes a property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

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
     * Sets the application model.
     */
    public void setModel(ApplicationModel newValue);

    /**
     * Returns the application model.
     */
    public ApplicationModel getModel();

    /**
     * Returns true, if this application shares tools among multiple views.
     */
    public boolean isSharingToolsAmongViews();

    /**
     * Returns the application component. 
     * This may return null, if the application is not represented by a component
     * of its own on the user interface.
     */
    public Component getComponent();

    /**
     * Adds a palette window to the application.
     */
    public void addPalette(Window palette);

    /**
     * Removes a palette window from the application.
     */
    public void removePalette(Window palette);

    /**
     * Adds a (non-palette) window to the application.
     *
     * @param window The window.
     * @param view The View to which this window is associated, or null
     * if the window is associated to the application.
     */
    public void addWindow(Window window, View view);

    /**
     * Removes a (non-palette) window from the application.
     */
    public void removeWindow(Window window);

    /**
     * Returns the recently opened URIs.
     * By convention, this is an immutable list.
     */
    public java.util.List<URI> getRecentURIs();

    /**
     * Appends a URI to the list of recent URIs.
     * This fires a property change event for the property "recentURIs".
     */
    public void addRecentURI(URI uri);

    /**
     * Clears the list of recent URIs.
     * This fires a property change event for the property "recentURIs".
     */
    public void clearRecentURIs();

    /**
     * Creates a file menu for the specified view.
     * Returns null, if the menu is empty.
     */
    public JMenu createFileMenu(View v);

    /**
     * Creates an edit menu for the specified view.
     * Returns null, if the menu is empty.
     */
    public JMenu createEditMenu(View v);

    /**
     * Creates a view menu for the specified view.
     * Returns null, if the menu is empty.
     */
    public JMenu createViewMenu(View v);

    /**
     * Creates a window menu for the specified view.
     * Returns null, if the menu is empty.
     */
    public JMenu createWindowMenu(View v);

    /** 
     * Creates a help menu for the specified view.
     * Returns null, if the menu is empty.
     */
    public JMenu createHelpMenu(View v);

    /**
     * Gets the open chooser.
     *
     * @param v View or null for application-wide chooser.
     */
    public URIChooser getOpenChooser(View v);

    /**
     * Gets the save chooser.
     *
     * @param v View or null for application-wide chooser.
     */
    public URIChooser getSaveChooser(View v);

    /**
     * Gets the export chooser.
     *
     * @param v View or null for application-wide chooser.
     */
    public URIChooser getExportChooser(View v);
    /**
     * Gets the import chooser.
     *
     * @param v View or null for application-wide chooser.
     */
    public URIChooser getImportChooser(View v);

    /**
     * Gets the action map.
     *
     * @param v View or null for application-wide action map.
     */
    public ActionMap getActionMap(View v);
}
