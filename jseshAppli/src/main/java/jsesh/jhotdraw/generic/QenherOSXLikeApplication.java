/*
 * @(#)QenherOSXApplication.java
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
package jsesh.jhotdraw.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jhotdraw_7_6.app.AbstractApplication;
import org.jhotdraw_7_6.app.ApplicationModel;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.ActionUtil;
import org.jhotdraw_7_6.app.action.app.AboutAction;
import org.jhotdraw_7_6.app.action.app.AbstractPreferencesAction;
import org.jhotdraw_7_6.app.action.app.ExitAction;
import org.jhotdraw_7_6.app.action.app.OpenApplicationAction;
import org.jhotdraw_7_6.app.action.app.OpenApplicationFileAction;
import org.jhotdraw_7_6.app.action.app.ReOpenApplicationAction;
import org.jhotdraw_7_6.app.action.edit.AbstractFindAction;
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
import org.jhotdraw_7_6.app.action.file.ClearRecentFilesMenuAction;
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
import org.jhotdraw_7_6.app.action.window.FocusWindowAction;
import org.jhotdraw_7_6.app.action.window.MaximizeWindowAction;
import org.jhotdraw_7_6.app.action.window.MinimizeWindowAction;
import org.jhotdraw_7_6.beans.Disposable;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.prefs.PreferencesUtil;

/**
 * {@code QenherOSXLikeApplication} handles the lifecycle of {@link View}s using a
 * Mac-like document interface, but for simple SDI applications (much like GIMP in linus or Windows).
 * <p>
 * An application consists of a screen menu bar and {@code JFrame}s for the
 * {@code View}s. The application also provides floating toolbars and palette
 * windows for the views.
 * <p>
 * The screen menu bar has the following standard menus:
 * <pre>
 * "Application-Name" File Window</pre>
 *
 * The first menu, is the <b>application menu</b>. It has the following standard
 * menu items: 
 * <pre>
 *  About "Application-Name" ({@link AboutAction#ID})
 *  -
 *  Preferences... ({@link AbstractPreferencesAction#ID})
 *  -
 *  Services
 *  -
 *  Hide "Application-Name"
 *  Hide Others
 *  Show All
 *  -
 *  Quit "Application-Name" ({@link ExitAction#ID})
 * </pre>
 *
 * The <b>file menu</b> has the following standard menu items:
 * <pre>
 *  New ({@link NewFileAction#ID}})
 *  Open... ({@link OpenFileAction#ID}})
 *  Open Recent &gt; "Filename" ({@link org.jhotdraw.app.action.file.OpenRecentFileAction#ID})
 *  -
 *  Close ({@link CloseFileAction#ID})
 *  Save ({@link SaveFileAction#ID})
 *  Save As... ({@link SaveFileAsAction#ID})
 *  -
 *  Print... ({@link PrintFileAction#ID})
 * </pre>
 *
 * The <b>window menu</b> has the following standard menu items:
 * <pre>
 *  Minimize ({@link MinimizeWindowAction#ID})
 *  Zoom ({@link MaximizeWindowAction#ID})
 *  -
 *  "Filename" ({@link FocusWindowAction#ID})
 * </pre>
 *
 * The menus provided by the {@code ApplicationModel} are inserted between
 * the file menu and the window menu. In case the application model supplies
 * a menu with the title "Help", it is inserted after the window menu.
 *
 * @author Serge Rosmorduc (after OXApplication by Werner Randelshofer)
 * @version $Id: QenherOSXApplication.java 608 2010-01-11 18:46:00Z rawcoder $
 */
public class QenherOSXLikeApplication extends AbstractApplication {

    private GenericPaletteHandler paletteHandler;
    private Preferences prefs;
    private LinkedList<Action> paletteActions;
 

    /** Creates a new instance. */
    public QenherOSXLikeApplication() {
    }

    @Override
    public void init() {
        super.init();
        prefs = PreferencesUtil.userNodeForPackage((getModel() == null) ? getClass() : getModel().getClass());
        initLookAndFeel();
        paletteHandler = new GenericPaletteHandler(this);

        initLabels();
        ApplicationModel m = getModel();

        paletteActions = new LinkedList<Action>();
        setActionMap(createModelActionMap(model));
        initPalettes(paletteActions);
        initScreenMenuBar();
        model.initApplication(this);
    }

    @Override
    public void launch(String[] args) {
        super.launch(args);
    }

    @Override
    public void configure(String[] args) {
    }

    protected void initLookAndFeel() {
        try {
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (UIManager.getString("OptionPane.css") == null) {
            UIManager.put("OptionPane.css", "<head>"
                    + "<style type=\"text/css\">"
                    + "b { font: 13pt \"Dialog\" }"
                    + "p { font: 11pt \"Dialog\"; margin-top: 8px }"
                    + "</style>"
                    + "</head>");
        }
    }

    public void dispose(View p) {
        FocusWindowAction a = (FocusWindowAction) getAction(p, FocusWindowAction.ID);
        if (a != null) {
            a.dispose();
        }
        super.dispose(p);
    }

    @Override
    public void addPalette(Window palette) {
        paletteHandler.addPalette(palette);
    }

    @Override
    public void removePalette(Window palette) {
        paletteHandler.removePalette(palette);
    }

    @Override
    public void addWindow(Window window, final View view) {
        if (window instanceof JFrame) {
            ((JFrame) window).setJMenuBar(createMenuBar(view));
        } else if (window instanceof JDialog) {
            // ((JDialog) window).setJMenuBar(createMenuBar(null));
        }

        paletteHandler.add(window, view);
    }

    @Override
    public void removeWindow(Window window) {
        if (window instanceof JFrame) {
            // We explicitly set the JMenuBar to null to facilitate garbage
            // collection
            ((JFrame) window).setJMenuBar(null);
        }
        paletteHandler.remove(window);
    }

    public void show(View view) {
        if (!view.isShowing()) {
            view.setShowing(true);
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            f.setSize(new Dimension(600, 400));
            updateViewTitle(view, f);

            PreferencesUtil.installFramePrefsHandler(prefs, "view", f);
            Point loc = f.getLocation();
            boolean moved;
            do {
                moved = false;
                for (Iterator i = views().iterator(); i.hasNext();) {
                    View aView = (View) i.next();
                    if (aView != view && aView.isShowing()
                            && SwingUtilities.getWindowAncestor(aView.getComponent()).
                            getLocation().equals(loc)) {
                        loc.x += 22;
                        loc.y += 22;
                        moved = true;
                        break;
                    }
                }
            } while (moved);
            f.setLocation(loc);

            FrameHandler frameHandler = new FrameHandler(f, view);
            addWindow(f, view);

            f.getContentPane().add(view.getComponent());
            f.setVisible(true);
            view.start();
        }
    }

    /**
     * Updates the title of a view and displays it in the given frame.
     * 
     * @param v The view.
     * @param f The frame.
     */
    protected void updateViewTitle(View v, JFrame f) {
        String title;
        URI uri = v.getURI();
        if (uri == null) {
            title = labels.getString("unnamedFile");
        } else {
            title = URIUtil.getName(uri);
        }
        v.setTitle(labels.getFormatted("frame.title", title, getName(), v.getMultipleOpenId()));
        f.setTitle(v.getTitle());

        // Adds a proxy icon for the file to the title bar
        // See http://developer.apple.com/technotes/tn2007/tn2196.html#WINDOW_DOCUMENTFILE
        if (uri != null && uri.getScheme() != null && uri.getScheme().equals("file")) {
            f.getRootPane().putClientProperty("Window.documentFile", new File(uri));
        } else {
            f.getRootPane().putClientProperty("Window.documentFile", null);
        }
    }

    public void hide(View p) {
        if (p.isShowing()) {
            JFrame f = (JFrame) SwingUtilities.getWindowAncestor(p.getComponent());
            f.setVisible(false);
            removeWindow(f);
            f.remove(p.getComponent());
            f.dispose();
        }
    }

    /**
     * Creates a menu bar.
     */
    protected JMenuBar createMenuBar(View v) {
        JMenuBar mb = new JMenuBar();

        // Get menus from application model
        JMenu fileMenu = null;
        JMenu editMenu = null;
        JMenu helpMenu = null;
        JMenu viewMenu = null;
        JMenu windowMenu = null;
        String fileMenuText = labels.getString("file.text");
        String editMenuText = labels.getString("edit.text");
        String viewMenuText = labels.getString("view.text");
        String windowMenuText = labels.getString("window.text");
        String helpMenuText = labels.getString("help.text");
        for (JMenu mm : getModel().createMenus(this, v)) {
            String text = mm.getText();
            if (text == null) {
                mm.setText("-null-");
            } else if (text.equals(fileMenuText)) {
                fileMenu = mm;
                continue;
            } else if (text.equals(editMenuText)) {
                editMenu = mm;
                continue;
            } else if (text.equals(viewMenuText)) {
                viewMenu = mm;
                continue;
            } else if (text.equals(windowMenuText)) {
                windowMenu = mm;
                continue;
            } else if (text.equals(helpMenuText)) {
                helpMenu = mm;
                continue;
            }
            mb.add(mm);
        }

        // Create missing standard menus
        if (fileMenu == null) {
            fileMenu = createFileMenu(v);
        }
        if (editMenu == null) {
            editMenu = createEditMenu(v);
        }
        if (viewMenu == null) {
            viewMenu = createViewMenu(v);
        }
        if (windowMenu == null) {
            windowMenu = createWindowMenu(v);
        }
        if (helpMenu == null) {
            helpMenu = createHelpMenu(v);
        }

        // Insert standard menus into menu bar
        if (fileMenu != null) {
            mb.add(fileMenu, 0);
        }
        if (editMenu != null) {
            mb.add(editMenu, Math.min(1, mb.getComponentCount()));
        }
        if (viewMenu != null) {
            mb.add(viewMenu, Math.min(2, mb.getComponentCount()));
        }
        if (windowMenu != null) {
            mb.add(windowMenu);
        }
        if (helpMenu != null) {
            mb.add(helpMenu);
        }

        return mb;
    }

    public JMenu createViewMenu(View view) {
        return null;
    }

    public JMenu createWindowMenu(View view) {
        ApplicationModel model = getModel();

        JMenu m;
        JMenuItem mi;

        m = new JMenu();
        JMenu windowMenu = m;
        labels.configureMenu(m, "window");
        m.addSeparator();

        new WindowMenuHandler(windowMenu, view);

        return m;
    }

    public JMenu createFileMenu(View view) {
        JMenuBar mb = new JMenuBar();
        JMenu m;

        m = new JMenu();
        labels.configureMenu(m, "file");
        addAction(m, view, ClearFileAction.ID);
        addAction(m, view, NewFileAction.ID);
        addAction(m, view, NewWindowAction.ID);

        getModel().getStandardMenuBuilder().afterFileNew(m, this,view);

        addAction(m, view, LoadFileAction.ID);
        addAction(m, view, OpenFileAction.ID);
        addAction(m, view, LoadDirectoryAction.ID);
        addAction(m, view, OpenDirectoryAction.ID);

        if (getAction(view, LoadFileAction.ID) != null ||//
                getAction(view, OpenFileAction.ID) != null ||//
                getAction(view, LoadDirectoryAction.ID) != null ||//
                getAction(view, OpenDirectoryAction.ID) != null) {
            m.add(createOpenRecentFileMenu(null));
        }
        getModel().getStandardMenuBuilder().afterFileOpen(m, this,view);

        maybeAddSeparator(m);

        addAction(m, view, CloseFileAction.ID);
        getModel().getStandardMenuBuilder().afterFileClose(m,this, view);

        addAction(m, view, SaveFileAction.ID);
        addAction(m, view, SaveFileAsAction.ID);
        addAction(m, view, ExportFileAction.ID);
        addAction(m, view, PrintFileAction.ID);
        getModel().getStandardMenuBuilder().atEndOfFileMenu(m,this, view);
        maybeAddSeparator(m);
        addAction(m, view, ExitAction.ID);
        return (m.getPopupMenu().getComponentCount() == 0) ? null : m;
    }

    public JMenu createEditMenu(View view) {
        JMenu m;
        JMenuItem mi;
        Action a;
        m = new JMenu();
        labels.configureMenu(m, "edit");
        addAction(m, view, UndoAction.ID);
        addAction(m, view, RedoAction.ID);

        maybeAddSeparator(m);

        addAction(m, view, CutAction.ID);
        addAction(m, view, CopyAction.ID);
        addAction(m, view, PasteAction.ID);
        addAction(m, view, DuplicateAction.ID);
        addAction(m, view, DeleteAction.ID);
        maybeAddSeparator(m);
        addAction(m, view, SelectAllAction.ID);
        addAction(m, view, ClearSelectionAction.ID);
        maybeAddSeparator(m);
        addAction(m, view, AbstractFindAction.ID);
        getModel().getStandardMenuBuilder().atEndOfEditMenu(m, this,view);
        return (m.getPopupMenu().getComponentCount() == 0) ? null : m;
    }

    public JMenu createHelpMenu(View p) {
        return null;
    }

    protected void initScreenMenuBar() {
    }

    protected void initPalettes(final LinkedList<Action> paletteActions) {
        SwingUtilities.invokeLater(new Worker<LinkedList<JFrame>>() {

            public LinkedList<JFrame> construct() {
                LinkedList<JFrame> palettes = new LinkedList<JFrame>();
                LinkedList<JToolBar> toolBars = new LinkedList<JToolBar>(getModel().createToolBars(QenherOSXLikeApplication.this, null));

                int i = 0;
                int x = 0;
                for (JToolBar tb : toolBars) {
                    i++;
                    tb.setFloatable(false);
                    tb.setOrientation(JToolBar.VERTICAL);
                    
                    JFrame d = new JFrame();

                    // Note: Client preconditions must be set before heavy-weight
                    // peers are created
                    d.getRootPane().putClientProperty("Window.style", "small");
                    d.getRootPane().putClientProperty("Quaqua.RootPane.isVertical", Boolean.FALSE);
                    d.getRootPane().putClientProperty("Quaqua.RootPane.isPalette", Boolean.TRUE);

                    tb.setFocusable(true);
                    d.setFocusable(true);
                    //d.setResizable(false);

                    d.getContentPane().setLayout(new BorderLayout());
                    d.getContentPane().add(tb, BorderLayout.CENTER);
                   // d.setAlwaysOnTop(true); // Suppress while debugging.
                    //d.setUndecorated(true);
                    //d.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                    
                    d.getRootPane().setFont(
                            new Font("Lucida Grande", Font.PLAIN, 11));
                    
                    d.pack();
                    //d.setFocusableWindowState(false);
                    PreferencesUtil.installPalettePrefsHandler(prefs, "toolbar." + i, d, x);
                    x += d.getWidth();

                    paletteActions.add(new QenherTogglePaletteAction(QenherOSXLikeApplication.this, d, tb.getName()));
                    palettes.add(d);
                    if (prefs.getBoolean("toolbar." + i + ".visible", true)) {
                        //addPalette(d);
                    }
                }
                return palettes;

            }

            @Override
            protected void done(LinkedList<JFrame> result) {
                @SuppressWarnings("unchecked")
                LinkedList<JFrame> palettes = (LinkedList<JFrame>) result;
                if (palettes != null) {
                    /*for (JFrame p : palettes) {
                    if (prefs.getBoolean("toolbar.", true))
                    addPalette(p);
                    }*/
                    firePropertyChange("paletteCount", 0, palettes.size());
                }
            }
        });
    }

    public boolean isSharingToolsAmongViews() {
        return true;
    }

   /**
    * It's not a MacOS X application, hence no "invisible" window.
    */
    public Component getComponent() {
       return null;
    }


    protected ActionMap createModelActionMap(ApplicationModel mo) {
        ActionMap rootMap = new ActionMap();
        rootMap.put(AboutAction.ID, new AboutAction(this));
        rootMap.put(ExitAction.ID, new ExitAction(this));
        rootMap.put(OpenApplicationAction.ID, new OpenApplicationAction(this));
        rootMap.put(OpenApplicationFileAction.ID, new OpenApplicationFileAction(this));
        rootMap.put(ReOpenApplicationAction.ID, new ReOpenApplicationAction(this));
        rootMap.put(ClearRecentFilesMenuAction.ID, new ClearRecentFilesMenuAction(this));
        rootMap.put(MaximizeWindowAction.ID, new MaximizeWindowAction(this, null));
        rootMap.put(MinimizeWindowAction.ID, new MinimizeWindowAction(this, null));

        ActionMap moMap = mo.createActionMap(this, null);
        moMap.setParent(rootMap);
        return moMap;
    }

    @Override
    protected ActionMap createViewActionMap(View v) {
        ActionMap intermediateMap = new ActionMap();
        intermediateMap.put(FocusWindowAction.ID, new FocusWindowAction(v));
        intermediateMap.put(MaximizeWindowAction.ID, new MaximizeWindowAction(this, v));
        intermediateMap.put(MinimizeWindowAction.ID, new MinimizeWindowAction(this, v));

        ActionMap vMap = model.createActionMap(this, v);
        vMap.setParent(intermediateMap);
        intermediateMap.setParent(getActionMap(null));
        return vMap;
    }

    /** Updates the menu items in the "Window" menu. */
    private class WindowMenuHandler implements PropertyChangeListener, Disposable {

        private JMenu windowMenu;
        private View view;

        public WindowMenuHandler(JMenu windowMenu, View view) {
            this.windowMenu = windowMenu;
            this.view = view;
            QenherOSXLikeApplication.this.addPropertyChangeListener(this);
            if (view != null) {
                view.addDisposable(this);
            }
            updateWindowMenu();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name == VIEW_COUNT_PROPERTY || name == "paletteCount") {
                updateWindowMenu();
            }
        }

        protected void updateWindowMenu() {
            JMenu m = windowMenu;
            JMenuItem mi;

            m.removeAll();
            ApplicationModel model = getModel();
            mi = m.add(getAction(view, MinimizeWindowAction.ID));
            mi.setIcon(null);
            mi = m.add(getAction(view, MaximizeWindowAction.ID));
            mi.setIcon(null);
            m.addSeparator();
            for (Iterator i = views().iterator(); i.hasNext();) {
                View pr = (View) i.next();
                if (getAction(pr, FocusWindowAction.ID) != null) {
                    mi = m.add(getAction(pr, FocusWindowAction.ID));
                }
            }
            if (paletteActions.size() > 0) {
                m.addSeparator();
                for (Action a : paletteActions) {
                    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                    ActionUtil.configureJCheckBoxMenuItem(cbmi, a);
                    cbmi.setIcon(null);
                    m.add(cbmi);
                }
            }
        }

        public void dispose() {
            windowMenu.removeAll();
            removePropertyChangeListener(this);
            view = null;
        }
    }

    /** Updates the modifedState of the frame. */
    private class FrameHandler extends WindowAdapter implements PropertyChangeListener, Disposable {

        private JFrame frame;
        private View view;

        public FrameHandler(JFrame frame, View view) {
            this.frame = frame;
            this.view = view;
            view.addPropertyChangeListener(this);
            frame.addWindowListener(this);
            view.addDisposable(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name.equals(View.HAS_UNSAVED_CHANGES_PROPERTY)) {
                frame.getRootPane().putClientProperty("windowModified", new Boolean(view.hasUnsavedChanges()));
            } else if (name.equals(View.URI_PROPERTY) || name.equals(View.TITLE_PROPERTY)) {
                updateViewTitle(view, frame);
            }
        }

        @Override
        public void windowClosing(final WindowEvent evt) {
            getAction(view, CloseFileAction.ID).actionPerformed(
                    new ActionEvent(evt.getSource(), ActionEvent.ACTION_PERFORMED,
                    "windowClosing"));
        }

        @Override
        public void windowClosed(final WindowEvent evt) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
            if (view == getActiveView()) {
                setActiveView(null);
            }
            view.stop();
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            view.start();
        }

        public void dispose() {
            frame.removeWindowListener(this);
            view.removePropertyChangeListener(this);
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            setActiveView(view);
        }
    }

    private class QuitHandler {

        /** This method is invoked, when the user has selected the Quit menu item.
         *
         * @return Returns true if the application has no unsaved changes and
         * can be closed.
         */
        public boolean handleQuit() {
            return false;
        }
    }
}
