/*
 * @(#)PreferencesUtil.java
 *
 * Copyright (c) 2005-2008 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.util.prefs;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * {@code PreferencesUtil} provides utility methods for {@code
 * java.util.prefs.Preferences}, and can be used as a proxy when the system
 * preferences are not available due to security restrictions.
 *
 * @author Werner Randelshofer
 * @version $Id: PreferencesUtil.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class PreferencesUtil
        extends Preferences {

    private HashMap<String, Object> map = new HashMap<String, Object>();
    private boolean isUserNode;
    private static HashMap<Package, Preferences> systemNodes;
    private static HashMap<Package, Preferences> userNodes;

    public PreferencesUtil(boolean isUserNode) {
        this.isUserNode = isUserNode;
    }

    
    public void put(String key, String value) {
        map.put(key, value);
    }

    
    public String get(String key, String def) {
        return (String) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public void remove(String key) {
        map.remove(key);
    }

    
    public void clear() throws BackingStoreException {
        map.clear();
    }

    
    public void putInt(String key, int value) {
        map.put(key, value);
    }

    
    public int getInt(String key, int def) {
        return (Integer) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public void putLong(String key, long value) {
        map.put(key, value);
    }

    
    public long getLong(String key, long def) {
        return (Long) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public void putBoolean(String key, boolean value) {
        map.put(key, value);
    }

    
    public boolean getBoolean(String key, boolean def) {
        return (Boolean) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public void putFloat(String key, float value) {
        map.put(key, value);
    }

    
    public float getFloat(String key, float def) {
        return (Float) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public void putDouble(String key, double value) {
        map.put(key, value);
    }

    
    public double getDouble(String key, double def) {
        return (Double) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public void putByteArray(String key, byte[] value) {
        map.put(key, value);
    }

    
    public byte[] getByteArray(String key, byte[] def) {
        return (byte[]) (map.containsKey(key) ? map.get(key) : def);
    }

    
    public String[] keys() throws BackingStoreException {
        return map.keySet().toArray(new String[map.keySet().size()]);
    }

    
    public String[] childrenNames() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public Preferences parent() {
        return null;
    }

    
    public Preferences node(String pathName) {
        return null;
    }

    
    public boolean nodeExists(String pathName) throws BackingStoreException {
        return false;
    }

    
    public void removeNode() throws BackingStoreException {
        // empty
    }

    
    public String name() {
        return "Dummy";
    }

    
    public String absolutePath() {
        return "Dummy";
    }

    
    public boolean isUserNode() {
        return isUserNode;
    }

    
    public String toString() {
        return "Dummy";
    }

    
    public void flush() throws BackingStoreException {
        clear();
    }

    
    public void sync() throws BackingStoreException {
        //
    }

    
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        //
    }

    
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        //
    }

    
    public void addNodeChangeListener(NodeChangeListener ncl) {
        //
    }

    
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        //
    }

    
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        //
    }

    
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        //
    }

    /** Gets the system node for the package of the class if
     * permitted, gets a proxy otherwise.
     *
     * @return system node or a proxy.
     */
    public static Preferences systemNodeForPackage(Class<?> c) {
        if (systemNodes != null) {
            if (!systemNodes.containsKey(c.getPackage())) {
                systemNodes.put(c.getPackage(), new PreferencesUtil(false));
            }
            return systemNodes.get(c.getPackage());
        }


        try {
            return Preferences.systemNodeForPackage(c);
        } catch (Throwable t) {
            if (systemNodes == null) {
                systemNodes = new HashMap<Package, Preferences>();
            }
            return systemNodeForPackage(c);
        }
    }

    /** Gets the user node for the package of the class if
     * permitted, gets a proxy otherwise.
     *
     * @return user node or a proxy.
     */
    public static Preferences userNodeForPackage(Class<?> c) {
        if (userNodes != null) {
            if (!userNodes.containsKey(c.getPackage())) {
                userNodes.put(c.getPackage(), new PreferencesUtil(false));
            }
            return userNodes.get(c.getPackage());
        }

        try {
            return Preferences.userNodeForPackage(c);
        } catch (Throwable t) {
            if (userNodes == null) {
                userNodes = new HashMap<Package, Preferences>();
            }
            return userNodeForPackage(c);
        }
    }

    public static void installPrefsHandler(Preferences prefs, String string, JTabbedPane tabbedPane) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Creates a new instance. */
    private PreferencesUtil() {
    }

    /**
     * Installs a frame preferences handler.
     * On first run, sets the window to its preferred size at the top left
     * corner of the screen.
     * On subsequent runs, sets the window the last size and location where
     * the user had placed it before.
     * <p>
     * If no preferences are stored yet for this window, a default size
     * of 400 x 300 pixels is used.
     *
     * @param prefs Preferences for storing/retrieving preferences values.
     * @param name Base name of the preference.
     * @param window The window for which to track preferences.
     */
    public static void installFramePrefsHandler(final Preferences prefs, final String name, Window window) {
        installFramePrefsHandler(prefs, name, window, new Dimension(400, 300));
    }

    /**
     * Installs a frame preferences handler.
     * On first run, sets the window to its preferred size at the top left
     * corner of the screen.
     * On subsequent runs, sets the window the last size and location where
     * the user had placed it before.
     *
     * @param prefs Preferences for storing/retrieving preferences values.
     * @param name Base name of the preference.
     * @param window The window for which to track preferences.
     * @param defaultSize This size is used when no prefences are stored yet for this window.
     *
     */
    public static void installFramePrefsHandler(final Preferences prefs, final String name, Window window, Dimension defaultSize) {
        GraphicsConfiguration conf = window.getGraphicsConfiguration();
        Rectangle screenBounds = conf.getBounds();
        Insets screenInsets = window.getToolkit().getScreenInsets(conf);

        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= screenInsets.left + screenInsets.right;
        screenBounds.height -= screenInsets.top + screenInsets.bottom;
        window.pack();
        Dimension preferredSize = window.getPreferredSize();

        boolean resizable = true;
        if (window instanceof Frame) {
            resizable = ((Frame) window).isResizable();
        } else if (window instanceof Dialog) {
            resizable = ((Dialog) window).isResizable();
        }
        Rectangle bounds;
        if (resizable) {
            bounds = new Rectangle(
                    prefs.getInt(name + ".x", 0),
                    prefs.getInt(name + ".y", 0),
                    Math.max(defaultSize.width, prefs.getInt(name + ".width", preferredSize.width)),
                    Math.max(defaultSize.height, prefs.getInt(name + ".height", preferredSize.height)));
        } else {
            bounds = new Rectangle(
                    prefs.getInt(name + ".x", 0),
                    prefs.getInt(name + ".y", 0),
                    window.getWidth(), window.getHeight());
        }
        if (!screenBounds.contains(bounds)) {
            bounds.x = screenBounds.x + (screenBounds.width - bounds.width) / 2;
            bounds.y = screenBounds.y + (screenBounds.height - bounds.height) / 3;
            Rectangle.intersect(screenBounds, bounds, bounds);
        }
        window.setBounds(bounds);

        window.addComponentListener(
                new ComponentAdapter() {

                    
                    public void componentMoved(ComponentEvent evt) {
                        prefs.putInt(name + ".x", evt.getComponent().getX());
                        prefs.putInt(name + ".y", evt.getComponent().getY());
                    }

                    
                    public void componentResized(ComponentEvent evt) {
                        prefs.putInt(name + ".width", evt.getComponent().getWidth());
                        prefs.putInt(name + ".height", evt.getComponent().getHeight());
                    }
                });

    }

    /**
     * Installs a palette preferences handler.
     * On first run, sets the palette to its preferred location at the top left
     * corner of the screen.
     * On subsequent runs, sets the palette the last location where
     * the user had placed it before.
     *
     * @param prefs Preferences for storing/retrieving preferences values.
     * @param name Base name of the preference.
     * @param window The window for which to track preferences.
     */
    public static void installPalettePrefsHandler(final Preferences prefs, final String name, Window window) {
        installPalettePrefsHandler(prefs, name, window, 0);
    }

    public static void installPalettePrefsHandler(final Preferences prefs, final String name, Window window, int x) {
        GraphicsConfiguration conf = window.getGraphicsConfiguration();
        Rectangle screenBounds = conf.getBounds();
        Insets screenInsets = window.getToolkit().getScreenInsets(conf);

        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= screenInsets.left + screenInsets.right;
        screenBounds.height -= screenInsets.top + screenInsets.bottom;

        Dimension preferredSize = window.getPreferredSize();

        Rectangle bounds = new Rectangle(
                prefs.getInt(name + ".x", x + screenBounds.x),
                prefs.getInt(name + ".y", 0 + screenBounds.y),
                preferredSize.width,
                preferredSize.height);

        if (!screenBounds.contains(bounds)) {
            bounds.x = screenBounds.x;
            bounds.y = screenBounds.y;
        }
        window.setBounds(bounds);

        window.addComponentListener(new ComponentAdapter() {

            
            public void componentMoved(ComponentEvent evt) {
                prefs.putInt(name + ".x", evt.getComponent().getX());
                prefs.putInt(name + ".y", evt.getComponent().getY());
            }
            /*
            public void componentResized(ComponentEvent evt) {
            prefs.putInt(name+".width", evt.getComponent().getWidth());
            prefs.putInt(name+".height", evt.getComponent().getHeight());
            }*/
        });
        window.addWindowListener(new WindowAdapter() {

            
            public void windowClosing(WindowEvent e) {
                prefs.putBoolean(name + ".visible", false);
            }

            
            public void windowOpened(WindowEvent e) {
                prefs.putBoolean(name + ".visible", true);
            }
        });
    }

    /**
     * Installs an intenal frame preferences handler.
     * On first run, sets the frame to its preferred size at the top left
     * corner of the desktop pane.
     * On subsequent runs, sets the frame the last size and location where
     * the user had placed it before.
     *
     * @param prefs Preferences for storing/retrieving preferences values.
     * @param name Base name of the preference.
     * @param window The window for which to track preferences.
     */
    public static void installInternalFramePrefsHandler(final Preferences prefs, final String name, JInternalFrame window, JDesktopPane desktop) {
        Rectangle screenBounds = desktop.getBounds();
        screenBounds.setLocation(0, 0);
        Insets screenInsets = desktop.getInsets();

        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= screenInsets.left + screenInsets.right;
        screenBounds.height -= screenInsets.top + screenInsets.bottom;

        Dimension preferredSize = window.getPreferredSize();
        Dimension minSize = window.getMinimumSize();

        Rectangle bounds = new Rectangle(
                prefs.getInt(name + ".x", 0),
                prefs.getInt(name + ".y", 0),
                Math.max(minSize.width, prefs.getInt(name + ".width", preferredSize.width)),
                Math.max(minSize.height, prefs.getInt(name + ".height", preferredSize.height)));

        if (!screenBounds.contains(bounds)) {
            bounds.x = screenBounds.x + (screenBounds.width - bounds.width) / 2;
            bounds.y = screenBounds.y + (screenBounds.height - bounds.height) / 2;
            Rectangle.intersect(screenBounds, bounds, bounds);
        }
        window.setBounds(bounds);

        window.addComponentListener(new ComponentAdapter() {

            
            public void componentMoved(ComponentEvent evt) {
                prefs.putInt(name + ".x", evt.getComponent().getX());
                prefs.putInt(name + ".y", evt.getComponent().getY());
            }

            
            public void componentResized(ComponentEvent evt) {
                prefs.putInt(name + ".width", evt.getComponent().getWidth());
                prefs.putInt(name + ".height", evt.getComponent().getHeight());
            }
        });

    }

    /**
     * Installs a toolbar preferences handler.
     * On first run, sets the toolbar to BorderLayout.TOP.
     * On subsequent runs, set the toolbar to the last BorderLayout location.
     *
     * @param prefs Preferences for storing/retrieving preferences values.
     * @param name Base name of the preference.
     * @param toolbar The JToolBar for which to track preferences.
     */
    public static void installToolBarPrefsHandler(final Preferences prefs, final String name, JToolBar toolbar) {
        new ToolBarPrefsHandler(toolbar, name, prefs);
    }

    /**
     * Installs a JTabbedPane preferences handler.
     * On first run, sets the JTabbedPane to its preferred tab.
     *
     * @param prefs Preferences for storing/retrieving preferences values.
     * @param name Base name of the preference.
     * @param tabbedPane The JTabbedPane for which to track preferences.
     */
    public static void installTabbedPanePrefsHandler(final Preferences prefs, final String name, final JTabbedPane tabbedPane) {
        int selectedTab = prefs.getInt(name, 0);
        try {
            tabbedPane.setSelectedIndex(selectedTab);
        } catch (IndexOutOfBoundsException e) {
        }
        tabbedPane.addChangeListener(new ChangeListener() {

            
            public void stateChanged(ChangeEvent e) {
                prefs.putInt(name, tabbedPane.getSelectedIndex());
            }
        });
    }
}
