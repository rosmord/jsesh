/*
 * @(#)MDIApplication.java
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jhotdraw_7_4_1.app.action.*;
import org.jhotdraw_7_4_1.app.action.app.AboutAction;
import org.jhotdraw_7_4_1.app.action.app.AbstractPreferencesAction;
import org.jhotdraw_7_4_1.app.action.app.ExitAction;
import org.jhotdraw_7_4_1.app.action.app.OpenApplicationFileAction;
import org.jhotdraw_7_4_1.app.action.edit.AbstractFindAction;
import org.jhotdraw_7_4_1.app.action.edit.ClearSelectionAction;
import org.jhotdraw_7_4_1.app.action.edit.CopyAction;
import org.jhotdraw_7_4_1.app.action.edit.CutAction;
import org.jhotdraw_7_4_1.app.action.edit.DeleteAction;
import org.jhotdraw_7_4_1.app.action.edit.DuplicateAction;
import org.jhotdraw_7_4_1.app.action.edit.PasteAction;
import org.jhotdraw_7_4_1.app.action.edit.RedoAction;
import org.jhotdraw_7_4_1.app.action.edit.SelectAllAction;
import org.jhotdraw_7_4_1.app.action.edit.UndoAction;
import org.jhotdraw_7_4_1.app.action.file.ClearFileAction;
import org.jhotdraw_7_4_1.app.action.file.ClearRecentFilesMenuAction;
import org.jhotdraw_7_4_1.app.action.file.CloseFileAction;
import org.jhotdraw_7_4_1.app.action.file.ExportFileAction;
import org.jhotdraw_7_4_1.app.action.file.LoadDirectoryAction;
import org.jhotdraw_7_4_1.app.action.file.LoadFileAction;
import org.jhotdraw_7_4_1.app.action.file.NewFileAction;
import org.jhotdraw_7_4_1.app.action.file.NewWindowAction;
import org.jhotdraw_7_4_1.app.action.file.OpenDirectoryAction;
import org.jhotdraw_7_4_1.app.action.file.OpenFileAction;
import org.jhotdraw_7_4_1.app.action.file.PrintFileAction;
import org.jhotdraw_7_4_1.app.action.file.SaveFileAction;
import org.jhotdraw_7_4_1.app.action.file.SaveFileAsAction;
import org.jhotdraw_7_4_1.app.action.window.ArrangeWindowsAction;
import org.jhotdraw_7_4_1.app.action.window.FocusWindowAction;
import org.jhotdraw_7_4_1.app.action.window.MaximizeWindowAction;
import org.jhotdraw_7_4_1.app.action.window.MinimizeWindowAction;
import org.jhotdraw_7_4_1.app.action.window.ToggleToolBarAction;
import org.jhotdraw_7_4_1.gui.*;
import org.jhotdraw_7_4_1.net.URIUtil;
import org.jhotdraw_7_4_1.util.*;
import org.jhotdraw_7_4_1.util.prefs.*;

/**
 * {@code MDIApplication} handles the lifecycle of {@link View}s
 * using a multiple document interface (MDI).
 * <p>
 * An application consists of a parent {@code JFrame} which holds a {@code JDesktopPane}.
 * The views reside in {@code JInternalFrame}s inside of the {@code JDesktopPane}. 
 * The parent frame also contains a menu bar, toolbars and palette windows for
 * the views.
 * <p>
 * The life cycle of the application is tied to the parent {@code JFrame}.
 * Closing the parent {@code JFrame} quits the application.
 *
 * The parent frame has the following standard menus:
 * <pre>
 * File Edit Window Help</pre>
 *
 * The <b>file menu</b> has the following standard menu items:
 * <pre>
 *  New ({@link NewFileAction#ID}})
 *  Open... ({@link OpenFileAction#ID}})
 *  Open Recent &gt; "Filename" ({@link org.jhotdraw_7_4_1.app.action.file.OpenRecentFileAction#ID}})
 *  -
 *  Close ({@link CloseFileAction#ID})
 *  Save ({@link SaveFileAction#ID})
 *  Save As... ({@link SaveFileAsAction#ID})
 *  -
 *  Print... ({@link PrintFileAction#ID})
 *  -
 *  Exit ({@link ExitAction#ID})
 * </pre>
 *
 * The <b>edit menu</b> has the following standard menu items:
 * <pre>
 *  Settings ({@link AbstractPreferencesAction#ID})
 * </pre>
 *
 * The <b>window menu</b> has the following standard menu items:
 * <pre>
 *  Minimize ({@link MinimizeWindowAction#ID})
 *  Maximize ({@link MaximizeWindowAction#ID})
 *  -
 *  "Filename" ({@link FocusWindowAction#ID})
 * </pre>
 *
 * The <b>help menu</b> has the following standard menu items:
 * <pre>
 *  About ({@link AboutAction#ID})
 * </pre>
 *
 * The menus provided by the {@code ApplicationModel} are inserted between
 * the file menu and the window menu. In case the application model supplies
 * a menu with the title "Edit" or "Help", the standard menu items are added
 * with a seperator to the end of the menu.
 *
 *
 *
 * @author Werner Randelshofer.
 * @version $Id: MDIApplication.java 608 2010-01-11 18:46:00Z rawcoder $
 */
public class MDIApplication extends AbstractApplication {

    private JFrame parentFrame;
    private JScrollPane scrollPane;
    private JMDIDesktopPane desktopPane;
    private Preferences prefs;
    private LinkedList<Action> toolBarActions;

    /** Creates a new instance. */
    public MDIApplication() {
    }

    
    public void init() {
        super.init();
        initLookAndFeel();
        prefs = PreferencesUtil.userNodeForPackage((getModel() == null) ? getClass() : getModel().getClass());
        initLabels();

        parentFrame = new JFrame(getName());
        parentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        parentFrame.setPreferredSize(new Dimension(600, 400));

        desktopPane = new JMDIDesktopPane();
        desktopPane.setTransferHandler(new DropFileTransferHandler());

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(desktopPane);
        toolBarActions = new LinkedList<Action>();

        setActionMap(createModelActionMap(model));
        parentFrame.getContentPane().add(
                wrapDesktopPane(scrollPane, toolBarActions));

        parentFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(final WindowEvent evt) {
                getAction(null, ExitAction.ID).actionPerformed(
                        new ActionEvent(parentFrame, ActionEvent.ACTION_PERFORMED, "windowClosing"));
            }
        });
        parentFrame.setJMenuBar(createMenuBar(null));

        PreferencesUtil.installFramePrefsHandler(prefs, "parentFrame", parentFrame);

        parentFrame.setVisible(true);
        model.initApplication(this);
    }

    protected ActionMap createModelActionMap(ApplicationModel mo) {
        ActionMap rootMap = new ActionMap();
        rootMap.put(AboutAction.ID, new AboutAction(this));
        rootMap.put(ExitAction.ID, new ExitAction(this));
        rootMap.put(ClearRecentFilesMenuAction.ID, new ClearRecentFilesMenuAction(this));

        rootMap.put(MaximizeWindowAction.ID, new MaximizeWindowAction(this, null));
        rootMap.put(MinimizeWindowAction.ID, new MinimizeWindowAction(this, null));

        rootMap.put(ArrangeWindowsAction.VERTICAL_ID, new ArrangeWindowsAction(desktopPane, Arrangeable.Arrangement.VERTICAL));
        rootMap.put(ArrangeWindowsAction.HORIZONTAL_ID, new ArrangeWindowsAction(desktopPane, Arrangeable.Arrangement.HORIZONTAL));
        rootMap.put(ArrangeWindowsAction.CASCADE_ID, new ArrangeWindowsAction(desktopPane, Arrangeable.Arrangement.CASCADE));

        ActionMap moMap = mo.createActionMap(this, null);
        moMap.setParent(rootMap);
        return moMap;
    }

    
    protected ActionMap createViewActionMap(View v) {
        ActionMap intermediateMap = new ActionMap();
        intermediateMap.put(FocusWindowAction.ID, new FocusWindowAction(v));

        ActionMap vMap = model.createActionMap(this, v);
        vMap.setParent(intermediateMap);
        intermediateMap.setParent(getActionMap(null));
        return vMap;
    }

    
    public void launch(String[] args) {
        super.launch(args);
    }

    public void configure(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "false");
        System.setProperty("com.apple.macos.useScreenMenuBar", "false");
        System.setProperty("apple.awt.graphics.UseQuartz", "false");
        System.setProperty("swing.aatext", "true");
    }

    protected void initLookAndFeel() {
        try {
            String lafName = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lafName);
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

    public void show(final View v) {
        if (!v.isShowing()) {
            v.setShowing(true);
            final JInternalFrame f = new JInternalFrame();
            f.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
            f.setClosable(getAction(v, CloseFileAction.ID) != null);
            f.setMaximizable(true);
            f.setResizable(true);
            f.setIconifiable(false);
            f.setSize(new Dimension(400, 400));
            updateViewTitle(v, f);

            PreferencesUtil.installInternalFramePrefsHandler(prefs, "view", f, desktopPane);
            Point loc = new Point(desktopPane.getInsets().left, desktopPane.getInsets().top);
            boolean moved;
            do {
                moved = false;
                for (Iterator i = views().iterator(); i.hasNext();) {
                    View aView = (View) i.next();
                    if (aView != v && aView.isShowing()
                            && SwingUtilities.getRootPane(aView.getComponent()).getParent().
                            getLocation().equals(loc)) {
                        Point offset = SwingUtilities.convertPoint(SwingUtilities.getRootPane(aView.getComponent()), 0, 0, SwingUtilities.getRootPane(aView.getComponent()).getParent());
                        loc.x += Math.max(offset.x, offset.y);
                        loc.y += Math.max(offset.x, offset.y);
                        moved = true;
                        break;
                    }
                }
            } while (moved);
            f.setLocation(loc);

            //paletteHandler.add(f, v);

            f.addInternalFrameListener(new InternalFrameAdapter() {

                
                public void internalFrameClosing(final InternalFrameEvent evt) {
                    getAction(v, CloseFileAction.ID).actionPerformed(
                            new ActionEvent(f, ActionEvent.ACTION_PERFORMED,
                            "windowClosing"));
                }

                
                public void internalFrameClosed(final InternalFrameEvent evt) {
                    v.stop();
                }
            });

            v.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name == View.HAS_UNSAVED_CHANGES_PROPERTY
                            || name == View.URI_PROPERTY) {
                        updateViewTitle(v, f);
                    }
                }
            });

            f.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("selected")) {
                        if (evt.getNewValue().equals(Boolean.TRUE)) {
                            setActiveView(v);
                        } else {
                            if (v == getActiveView()) {
                                setActiveView(null);
                            }
                        }
                    }
                }
            });

            //f.setJMenuBar(createMenuBar(v));

            f.getContentPane().add(v.getComponent());
            f.setVisible(true);
            desktopPane.add(f);
            if (desktopPane.getComponentCount() == 1) {
                try {
                    f.setMaximum(true);
                } catch (PropertyVetoException ex) {
                    // ignore veto
                }
            }
            f.toFront();
            try {
                f.setSelected(true);
            } catch (PropertyVetoException e) {
                // Don't care.
            }
            v.getComponent().requestFocusInWindow();
            v.start();
        }
    }

    public void hide(View v) {
        if (v.isShowing()) {
            JInternalFrame f = (JInternalFrame) SwingUtilities.getRootPane(v.getComponent()).getParent();
            f.setVisible(false);
            f.remove(v.getComponent());

            // Setting the JMenuBar to null triggers action disposal of
            // actions in the openRecentMenu and the windowMenu. This is
            // important to prevent memory leaks.
            f.setJMenuBar(null);

            desktopPane.remove(f);
            f.dispose();
        }
    }

    public boolean isSharingToolsAmongViews() {
        return true;
    }

    public Component getComponent() {
        return parentFrame;
    }

    /**
     * Returns the wrapped desktop pane.
     */
    protected Component wrapDesktopPane(Component c, LinkedList<Action> toolBarActions) {
        if (getModel() != null) {
            int id = 0;
            for (JToolBar tb : new ReversedList<JToolBar>(getModel().createToolBars(this, null))) {
                id++;
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(tb, BorderLayout.NORTH);
                panel.add(c, BorderLayout.CENTER);
                c = panel;
                PreferencesUtil.installToolBarPrefsHandler(prefs, "toolbar." + id, tb);
                toolBarActions.addFirst(new ToggleToolBarAction(tb, tb.getName()));
            }
        }
        return c;
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

    
    public JMenu createFileMenu(View view) {
        JMenuBar mb = new JMenuBar();
        JMenu m;

        m = new JMenu();
        labels.configureMenu(m, "file");
        addAction(m, view, ClearFileAction.ID);
        addAction(m, view, NewFileAction.ID);
        addAction(m, view, NewWindowAction.ID);

        getModel().getStandardMenuBuilder().afterFileNew(m, view);
        
        addAction(m, view, LoadFileAction.ID);
        addAction(m, view, OpenFileAction.ID);
        addAction(m, view, LoadDirectoryAction.ID);
        addAction(m, view, OpenDirectoryAction.ID);

        if (getAction(view, LoadFileAction.ID) != null ||//
                getAction(view, OpenFileAction.ID) != null ||//
                getAction(view, LoadDirectoryAction.ID) != null ||//
                getAction(view, OpenDirectoryAction.ID) != null) {
            m.add(createOpenRecentFileMenu(view));
        }
        getModel().getStandardMenuBuilder().afterFileOpen(m, view);

        maybeAddSeparator(m);
        addAction(m, view, CloseFileAction.ID);
        getModel().getStandardMenuBuilder().afterFileClose(m, view);

        addAction(m, view, SaveFileAction.ID);
        addAction(m, view, SaveFileAsAction.ID);
        addAction(m, view, ExportFileAction.ID);
        addAction(m, view, PrintFileAction.ID);
        getModel().getStandardMenuBuilder().atEndOfFileMenu(m, view);

        maybeAddSeparator(m);
        addAction(m, view, ExitAction.ID);

        return m;
    }

    /**
     * Updates the title of a view and displays it in the given frame.
     *
     * @param v The view.
     * @param f The frame.
     */
    protected void updateViewTitle(View v, JInternalFrame f) {
        URI uri = v.getURI();
        String title;
        if (uri == null) {
            title = labels.getString("unnamedFile");
        } else {
            title = URIUtil.getName(uri);
        }
        if (v.hasUnsavedChanges()) {
            title += "*";
        }
        v.setTitle(labels.getFormatted("internalFrame.title", title, getName(), v.getMultipleOpenId()));
        f.setTitle(v.getTitle());
    }

    public JMenu createViewMenu(View v) {
        return null;
    }

    public JMenu createWindowMenu(View view) {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");
        ApplicationModel mo = getModel();

        JMenu m;
        JMenuItem mi;

        m = new JMenu();
        JMenu windowMenu = m;
        labels.configureMenu(m, "window");
        addAction(m, view, ArrangeWindowsAction.CASCADE_ID);
        addAction(m, view, ArrangeWindowsAction.VERTICAL_ID);
        addAction(m, view, ArrangeWindowsAction.HORIZONTAL_ID);

        maybeAddSeparator(m);
        for (View pr : views()) {
            addAction(m, view, FocusWindowAction.ID);
        }
        if (toolBarActions.size() > 0) {
            maybeAddSeparator(m);
            for (Action a : toolBarActions) {
                JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                ActionUtil.configureJCheckBoxMenuItem(cbmi, a);
                addMenuItem(m, cbmi);
            }
        }

        addPropertyChangeListener(new WindowMenuHandler(windowMenu, view));

        return m;
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
        getModel().getStandardMenuBuilder().atEndOfEditMenu(m, view);
        maybeAddSeparator(m);
        addAction(m, view, AbstractPreferencesAction.ID);
        return (m.getPopupMenu().getComponentCount() == 0) ? null : m;
    }

    public JMenu createHelpMenu(View view) {
        ApplicationModel mo = getModel();

        JMenu m;
        JMenuItem mi;

        m = new JMenu();
        labels.configureMenu(m, "help");
        addAction(m, view, AboutAction.ID);
        return m;
    }

    /** Updates the menu items in the "Window" menu. */
    private class WindowMenuHandler implements PropertyChangeListener {

        private JMenu windowMenu;
        private View view;

        public WindowMenuHandler(JMenu windowMenu, View view) {
            this.windowMenu = windowMenu;
            this.view = view;
            MDIApplication.this.addPropertyChangeListener(this);
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
            ApplicationModel mo = getModel();
            m.removeAll();

            m.add(getAction(view, ArrangeWindowsAction.CASCADE_ID));
            m.add(getAction(view, ArrangeWindowsAction.VERTICAL_ID));
            m.add(getAction(view, ArrangeWindowsAction.HORIZONTAL_ID));

            m.addSeparator();
            for (Iterator i = views().iterator(); i.hasNext();) {
                View pr = (View) i.next();
                if (getAction(pr, FocusWindowAction.ID) != null) {
                    m.add(getAction(pr, FocusWindowAction.ID));
                }
            }
            if (toolBarActions.size() > 0) {
                m.addSeparator();
                for (Action a : toolBarActions) {
                    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(a);
                    ActionUtil.configureJCheckBoxMenuItem(cbmi, a);
                    m.add(cbmi);
                }
            }
        }
    }

    /** This transfer handler opens a new view for each dropped file. */
    private class DropFileTransferHandler extends TransferHandler {

        
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            Action a = getAction(null, OpenApplicationFileAction.ID);
            if (a == null) {
                return false;
            }
            for (DataFlavor f : transferFlavors) {
                if (f.isFlavorJavaFileListType()) {
                    return true;
                }
            }
            return false;
        }

        
        public boolean importData(JComponent comp, Transferable t) {
            Action a = getAction(null, OpenApplicationFileAction.ID);
            if (a == null) {
                return false;
            }
            try {
                @SuppressWarnings("unchecked")
                java.util.List<File> files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                for (final File f : files) {
                    a.actionPerformed(new ActionEvent(desktopPane, ActionEvent.ACTION_PERFORMED, f.toString()));
                }
                return true;
            } catch (UnsupportedFlavorException ex) {
                return false;
            } catch (IOException ex) {
                return false;
            }
        }
    }
}
