/*
 * @(#)AbstractApplication.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its 
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app;

import java.awt.Container;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.action.file.ClearRecentFilesMenuAction;
import org.jhotdraw_7_6.app.action.file.LoadDirectoryAction;
import org.jhotdraw_7_6.app.action.file.LoadFileAction;
import org.jhotdraw_7_6.app.action.file.LoadRecentFileAction;
import org.jhotdraw_7_6.app.action.file.OpenRecentFileAction;
import org.jhotdraw_7_6.beans.AbstractBean;
import org.jhotdraw_7_6.gui.URIChooser;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.util.ResourceBundleUtil;
import org.jhotdraw_7_6.util.prefs.PreferencesUtil;
import org.qenherkhopeshef.jhotdrawChanges.Nullable;

/**
 * This abstract class can be extended to implement an {@link Application}.
 * <p>
 * {@code AbstractApplication} supports the command line parameter
 * {@code -open filename} to open views for specific URI's upon launch of
 * the application.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractApplication.java 722 2010-11-26 08:49:25Z rawcoder $
 */
public abstract class AbstractApplication extends AbstractBean implements Application {

    private LinkedList<View> views = new LinkedList<View>();
    private Collection<View> unmodifiableViews;
    private boolean isEnabled = true;
    protected ResourceBundleUtil labels;
    protected ApplicationModel model;
    private Preferences prefs;
    
    private View activeView;
    public final static String VIEW_COUNT_PROPERTY = "viewCount";
    private LinkedList<URI> recentFiles = new LinkedList<URI>();
    private final static int maxRecentFilesCount = 10;
    private ActionMap actionMap;
    private URIChooser openChooser;
    private URIChooser saveChooser;
    private URIChooser importChooser;
    private URIChooser exportChooser;

    /** Creates a new instance. */
    public AbstractApplication() {
    }

    
    public void init() {
        prefs = PreferencesUtil.userNodeForPackage((getModel() == null) ? getClass() : getModel().getClass());
        int count = prefs.getInt("recentFileCount", 0);
        for (int i = 0; i < count; i++) {
            String path = prefs.get("recentFile." + i, null);
            if (path != null) {
                try {
                    recentFiles.add(new URI(path));
                } catch (URISyntaxException ex) {
                    // Silently don't add this URI
                }
            }
        }
    }

    
    public void start(List<URI> uris) {
        if (uris.isEmpty()) {
            final View v = createView();
            add(v);
            v.setEnabled(false);
            show(v);

            // Set the start view immediately active, so that
            // ApplicationOpenFileAction picks it up on Mac OS X.
            setActiveView(v);

            v.execute(new Worker<Object>() {

                
                public Object construct() {
                    v.clear();
                    return null;
                }

                
                public void finished() {
                    v.setEnabled(true);
                }
            });
        } else {
            for (final URI uri : uris) {
                final View v = createView();
                add(v);
                v.setEnabled(false);
                show(v);

                // Set the start view immediately active, so that
                // ApplicationOpenFileAction picks it up on Mac OS X.
                setActiveView(v);

                v.execute(new Worker<Object>() {

                    
                    public Object construct() throws Exception {
                        v.read(uri, null);
                        return null;
                    }

                    
                    public void finished() {
                        v.setURI(uri);
                        v.setEnabled(true);
                    }
                });
            }
        }
    }

    
    public final View createView() {
        View v = basicCreateView();
        v.setActionMap(createViewActionMap(v));
        return v;
    }

    
    public void setModel(ApplicationModel newValue) {
        ApplicationModel oldValue = model;
        model = newValue;
        firePropertyChange("model", oldValue, newValue);
    }

    
    public ApplicationModel getModel() {
        return model;
    }

    protected View basicCreateView() {
        return model.createView();
    }

    /**
     * Sets the active view. Calls deactivate on the previously
     * active view, and then calls activate on the given view.
     * 
     * @param newValue Active view, can be null.
     */
    public void setActiveView( View newValue) {
        View oldValue = activeView;
        if (activeView != null) {
            activeView.deactivate();
        }
        activeView = newValue;
        if (activeView != null) {
            activeView.activate();
        }
        firePropertyChange(ACTIVE_VIEW_PROPERTY, oldValue, newValue);
    }

    /**
     * Gets the active view.
     * 
     * @return The active view can be null.
     */
    
    
    public View getActiveView() {
        return activeView;
    }


    /**
     * A method to distinguish multiple views of the same file/uri.
     * <p> the multipleOpenId of view will be computed to avoid
     * conflicts with other views displaying the same URI.</p>
     * @param view
     * @param uri
     */
    @Override
    public void fixMultipleOpenId(View view, URI uri) {
       		setEnabled(true);
       		view.setEnabled(false);

       		// If there is another view with the same URI we set the multiple open
       		// id of our view to max(multiple open id) + 1.
       		int multipleOpenId = 1;
       		for (View aView : views()) {
       			if (aView != view && aView.getURI() != null
       					&& aView.getURI().equals(uri)) {
       				multipleOpenId = Math.max(multipleOpenId,
       						aView.getMultipleOpenId() + 1);
       			}
       		}
       		view.setMultipleOpenId(multipleOpenId);
       		view.setEnabled(false);
    }

    public String getName() {
        return model.getName();
    }

    
    public String getVersion() {
        return model.getVersion();
    }

    
    public String getCopyright() {
        return model.getCopyright();
    }

    
    public void stop() {
        for (View p : new LinkedList<View>(views())) {
            dispose(p);
        }
    }

    
    public void destroy() {
        stop();
        model.destroyApplication(this);
        System.exit(0);
    }

    
    public void remove(View v) {
        hide(v);
        if (v == getActiveView()) {
            setActiveView(null);
        }
        int oldCount = views.size();
        views.remove(v);
        v.setApplication(null);
        firePropertyChange(VIEW_COUNT_PROPERTY, oldCount, views.size());
    }

    
    public void add(View v) {
        if (v.getApplication() != this) {
            int oldCount = views.size();
            views.add(v);
            v.setApplication(this);
            v.init();
            model.initView(this, v);
            firePropertyChange(VIEW_COUNT_PROPERTY, oldCount, views.size());
        }
    }

    protected abstract ActionMap createViewActionMap(View p);

    
    public void dispose(View view) {
        remove(view);
        model.destroyView(this, view);
        view.dispose();
    }

    
    public Collection<View> views() {
        if (unmodifiableViews == null) {
            unmodifiableViews = Collections.unmodifiableCollection(views);
        }
        return unmodifiableViews;
    }

    
    public boolean isEnabled() {
        return isEnabled;
    }

    
    public void setEnabled(boolean newValue) {
        boolean oldValue = isEnabled;
        isEnabled = newValue;
        firePropertyChange("enabled", oldValue, newValue);
    }

    public Container createContainer() {
        return new JFrame();
    }

    /** Launches the application.
     *
     * @param args This implementation supports the command-line parameter "-open"
     * which can be followed by one or more filenames or URI's.
     */
    
    public void launch(String[] args) {
        configure(args);

        // Get URI's from command line
        final List<URI> uris = getOpenURIsFromMainArgs(args);

        SwingUtilities.invokeLater(new Runnable() {

            
            public void run() {
                init();
                start(uris);
            }
        });
    }

    /** Parses the arguments to the main method and returns a list of URI's
     * for which views need to be opened upon launch of the application.
     * <p>
     * This implementation supports the command-line parameter "-open"
     * which can be followed by one or more filenames or URI's.
     * <p>
     * This method is invoked from the {@code Application.launch} method.
     *
     * @param args Arguments to the main method.
     * @return A list of URI's parsed from the arguments. Returns an empty list
     * if no URI's shall be opened.
     */
    protected List<URI> getOpenURIsFromMainArgs(String[] args) {
        LinkedList<URI> uris = new LinkedList<URI>();
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-open")) {
                for (++i; i < args.length; ++i) {
                    if (args[i].startsWith("-")) {
                        break;
                    }
                    URI uri;
                    uri = new File(args[i]).toURI();
                    uris.add(uri);
                }
            }
        }
        return uris;
    }

    protected void initLabels() {
        labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_6.app.Labels");
    }

    
    public void configure(String[] args) {
    }

    
    public void removePalette(Window palette) {
    }

    
    public void addPalette(Window palette) {
    }

    
    public void removeWindow(Window window) {
    }

    
    public void addWindow(Window window,  View p) {
    }

    protected Action getAction( View view, String actionID) {
        return getActionMap(view).get(actionID);
    }

    /** Adds the specified action as a menu item to the supplied menu. */
    protected void addAction(JMenu m,  View view, String actionID) {
        addAction(m, getAction(view, actionID));
    }

    /** Adds the specified action as a menu item to the supplied menu. */
    protected void addAction(JMenu m, Action a) {
        if (a != null) {
            if (m.getClientProperty("needsSeparator") == Boolean.TRUE) {
                m.addSeparator();
                m.putClientProperty("needsSeparator", null);
            }
            JMenuItem mi;
            mi = m.add(a);
            mi.setIcon(null);
            mi.setToolTipText(null);
        }
    }

    /** Adds the specified action as a menu item to the supplied menu. */
    protected void addMenuItem(JMenu m, JMenuItem mi) {
        if (mi != null) {
            if (m.getClientProperty("needsSeparator") == Boolean.TRUE) {
                m.addSeparator();
                m.putClientProperty("needsSeparator", null);
            }
            m.add(mi);
        }
    }

    /** Adds a separator to the supplied menu. The separator will only
    be added, if the previous item is not a separator. */
    protected void maybeAddSeparator(JMenu m) {
        JPopupMenu pm = m.getPopupMenu();
        if (pm.getComponentCount() > 0 //
                && !(pm.getComponent(pm.getComponentCount() - 1) instanceof JSeparator)) {
            m.addSeparator();
        }
    }

    
    public java.util.List<URI> getRecentURIs() {
        return Collections.unmodifiableList(recentFiles);
    }

    
    public void clearRecentURIs() {
        @SuppressWarnings("unchecked")
        java.util.List<URI> oldValue = (java.util.List<URI>) recentFiles.clone();
        recentFiles.clear();
        prefs.putInt("recentFileCount", recentFiles.size());
        firePropertyChange("recentFiles",
                Collections.unmodifiableList(oldValue),
                Collections.unmodifiableList(recentFiles));
    }

    
    public void addRecentURI(URI uri) {
        @SuppressWarnings("unchecked")
        java.util.List<URI> oldValue = (java.util.List<URI>) recentFiles.clone();
        if (recentFiles.contains(uri)) {
            recentFiles.remove(uri);
        }
        recentFiles.addFirst(uri);
        if (recentFiles.size() > maxRecentFilesCount) {
            recentFiles.removeLast();
        }

        prefs.putInt("recentFileCount", recentFiles.size());
        int i = 0;
        for (URI f : recentFiles) {
            prefs.put("recentFile." + i, f.toString());
            i++;
        }

        firePropertyChange("recentFiles", oldValue, 0);
        firePropertyChange("recentFiles",
                Collections.unmodifiableList(oldValue),
                Collections.unmodifiableList(recentFiles));
    }

    protected JMenu createOpenRecentFileMenu( View view) {
        JMenuItem mi;
        JMenu m;

        m = new JMenu();
        labels.configureMenu(m, //
                (getAction(view, LoadFileAction.ID) != null || //
                getAction(view, LoadDirectoryAction.ID) != null) ?//
                "file.loadRecent" ://
                "file.openRecent"//
                );
        m.setIcon(null);
        m.add(getAction(view, ClearRecentFilesMenuAction.ID));

        new OpenRecentMenuHandler(m, view);
        return m;
    }

    /** Updates the menu items in the "Open Recent" file menu. */
    private class OpenRecentMenuHandler implements PropertyChangeListener, Disposable {

        private JMenu openRecentMenu;
        private LinkedList<Action> openRecentActions = new LinkedList<Action>();
        
        private View view;

        public OpenRecentMenuHandler(JMenu openRecentMenu,  View view) {
            this.openRecentMenu = openRecentMenu;
            this.view = view;
            if (view != null) {
                view.addDisposable(this);
            }
            updateOpenRecentMenu();
            addPropertyChangeListener(this);
        }

        
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name == "recentFiles") {
                updateOpenRecentMenu();
            }
        }

        /**
         * Updates the "File &gt; Open Recent" menu.
         */
        protected void updateOpenRecentMenu() {
            if (openRecentMenu.getItemCount() > 0) {
                JMenuItem clearRecentFilesItem = (JMenuItem) openRecentMenu.getItem(
                        openRecentMenu.getItemCount() - 1);
                openRecentMenu.remove(openRecentMenu.getItemCount() - 1);

                // Dispose the actions and the menu items that are currently in the menu
                for (Action action : openRecentActions) {
                    if (action instanceof Disposable) {
                        ((Disposable) action).dispose();
                    }
                }
                openRecentActions.clear();
                openRecentMenu.removeAll();

                // Create new actions and add them to the menu
                if (getAction(view, LoadFileAction.ID) != null || //
                        getAction(view, LoadDirectoryAction.ID) != null) {
                    for (URI f : getRecentURIs()) {
                        LoadRecentFileAction action = new LoadRecentFileAction(AbstractApplication.this, view, f);
                        openRecentMenu.add(action);
                        openRecentActions.add(action);
                    }
                } else {
                    for (URI f : getRecentURIs()) {
                        OpenRecentFileAction action = new OpenRecentFileAction(AbstractApplication.this, f);
                        openRecentMenu.add(action);
                        openRecentActions.add(action);
                    }
                }
                if (getRecentURIs().size() > 0) {
                    openRecentMenu.addSeparator();
                }

                // Add a separator and the clear recent files item.
                openRecentMenu.add(clearRecentFilesItem);
            }
        }

        
        public void dispose() {
            removePropertyChangeListener(this);
            // Dispose the actions and the menu items that are currently in the menu
            for (Action action : openRecentActions) {
                if (action instanceof Disposable) {
                    ((Disposable) action).dispose();
                }
            }
            openRecentActions.clear();
        }
    }

    
    public URIChooser getOpenChooser(View v) {
        if (v == null) {
            if (openChooser == null) {
                openChooser = model.createOpenChooser(this, null);
                List<URI> ruris = getRecentURIs();
                if (ruris.size() > 0) {
                    try {
                        openChooser.setSelectedURI(ruris.get(0));
                    } catch (IllegalArgumentException e) {
                        // Ignore illegal values in recent URI list.
                    }
                }
            }
            return openChooser;
        } else {
            URIChooser chooser = (URIChooser) v.getComponent().getClientProperty("openChooser");
            if (chooser == null) {
                chooser = model.createOpenChooser(this, v);
                v.getComponent().putClientProperty("openChooser", chooser);
                List<URI> ruris = getRecentURIs();
                if (ruris.size() > 0) {
                    try {
                        chooser.setSelectedURI(ruris.get(0));
                    } catch (IllegalArgumentException e) {
                        // Ignore illegal values in recent URI list.
                    }
                }
            }
            return chooser;
        }
    }

    
    public URIChooser getSaveChooser(View v) {
        if (v == null) {
            if (saveChooser == null) {
                saveChooser = model.createSaveChooser(this, null);
            }
            return saveChooser;
        } else {
            URIChooser chooser = (URIChooser) v.getComponent().getClientProperty("saveChooser");
            if (chooser == null) {
                chooser = model.createSaveChooser(this, v);
                v.getComponent().putClientProperty("saveChooser", chooser);
                try {
                    chooser.setSelectedURI(v.getURI());
                } catch (IllegalArgumentException e) {
                    // ignore illegal values
                }
            }
            return chooser;
        }
    }

    
    public URIChooser getImportChooser(View v) {
        if (v == null) {
            if (importChooser == null) {
                importChooser = model.createImportChooser(this, null);
            }
            return importChooser;
        } else {
            URIChooser chooser = (URIChooser) v.getComponent().getClientProperty("importChooser");
            if (chooser == null) {
                chooser = model.createImportChooser(this, v);
                v.getComponent().putClientProperty("importChooser", chooser);
            }
            return chooser;
        }
    }

    
    public URIChooser getExportChooser(View v) {
        if (v == null) {
            if (exportChooser == null) {
                exportChooser = model.createExportChooser(this, null);
            }
            return exportChooser;
        } else {
            URIChooser chooser = (URIChooser) v.getComponent().getClientProperty("exportChooser");
            if (chooser == null) {
                chooser = model.createExportChooser(this, v);
                v.getComponent().putClientProperty("exportChooser", chooser);
            }
            return chooser;
        }
    }

    /**
     * Sets the application-wide action map.
     */
    public void setActionMap(ActionMap m) {
        actionMap = m;
    }

    /**
     * Gets the action map.
     */
    
    public ActionMap getActionMap( View v) {
        return (v == null) ? actionMap : v.getActionMap();
    }
    
    public void recomputeWindowMenu() {
		firePropertyChange(VIEW_COUNT_PROPERTY, 0, 1);    	
};
}
