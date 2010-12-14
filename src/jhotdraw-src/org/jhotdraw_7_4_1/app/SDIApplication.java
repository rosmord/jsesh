/*
 * @(#)SDIApplication.java
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;

import org.jhotdraw_7_4_1.app.action.*;
import org.jhotdraw_7_4_1.app.action.app.AboutAction;
import org.jhotdraw_7_4_1.app.action.app.AbstractPreferencesAction;
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
import org.jhotdraw_7_4_1.app.action.window.MaximizeWindowAction;
import org.jhotdraw_7_4_1.app.action.window.MinimizeWindowAction;
import org.jhotdraw_7_4_1.app.action.window.ToggleVisibleAction;
import org.jhotdraw_7_4_1.net.URIUtil;
import org.jhotdraw_7_4_1.util.*;
import org.jhotdraw_7_4_1.util.prefs.*;

/**
 * {@code SDIApplication} handles the lifecycle of a {@link View}s
 * using a single document interface (SDI).
 * <p>
 * An application consists of independent {@code JFrame}s for each view.
 * Each JFrame contains a menu bar, toolbars and palette bars for
 * the views.
 * <p>
 * The life cycle of the application is tied to the {@code JFrame}s. Closing the
 * last {@code JFrame} quits the application.

 * SDIApplication handles the life cycle of a single document window
 * being presented in a JFrame. The JFrame provides all the functionality needed
 * to work with the document, such as a menu bar, tool bars and palette windows.
 * <p>
 * The life cycle of the application is tied to the JFrame. Closing the JFrame
 * quits the application.
 *
 * @author Werner Randelshofer
 * @version $Id: SDIApplication.java 604 2010-01-09 12:00:29Z rawcoder $
 */
public class SDIApplication extends AbstractApplication {

    private Preferences prefs;

    /** Creates a new instance. */
    public SDIApplication() {
    }

    
    public void launch(String[] args) {
        System.setProperty("apple.awt.graphics.UseQuartz", "false");
        super.launch(args);
    }

    
    public void init() {
        super.init();
        initLookAndFeel();
        prefs = PreferencesUtil.userNodeForPackage((getModel() == null) ? getClass() : getModel().getClass());
        initLabels();
        setActionMap(createModelActionMap(model));
        model.initApplication(this);
    }

    
    public void remove(View p) {
        super.remove(p);
        if (views().size() == 0) {
            stop();
        }
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

    @SuppressWarnings("unchecked")
    public void show(final View view) {
        if (!view.isShowing()) {
            view.setShowing(true);
            final JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            updateViewTitle(view, f);

            JPanel panel = (JPanel) wrapViewComponent(view);
            f.add(panel);
            f.setSize(new Dimension(600, 400));
            f.setJMenuBar(createMenuBar(view));

            PreferencesUtil.installFramePrefsHandler(prefs, "view", f);
            Point loc = f.getLocation();
            boolean moved;
            do {
                moved = false;
                for (Iterator i = views().iterator(); i.hasNext();) {
                    View aView = (View) i.next();
                    if (aView != view
                            && SwingUtilities.getWindowAncestor(aView.getComponent()) != null
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

            f.addWindowListener(new WindowAdapter() {

                public void windowClosing(final WindowEvent evt) {
                    getAction(view, CloseFileAction.ID).actionPerformed(
                            new ActionEvent(f, ActionEvent.ACTION_PERFORMED,
                            "windowClosing"));
                }

                
                public void windowClosed(final WindowEvent evt) {
                    view.stop();
                }

                
                public void windowGainedFocus(WindowEvent e) {
                    setActiveView(view);
                }
            });

            view.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals(View.HAS_UNSAVED_CHANGES_PROPERTY)
                            || name.equals(View.URI_PROPERTY)
                            || name.equals(View.TITLE_PROPERTY)
                            || name.equals(View.MULTIPLE_OPEN_ID_PROPERTY)) {
                        updateViewTitle(view, f);
                    }
                }
            });

            f.setVisible(true);
            view.start();
        }
    }

    /**
     * Returns the view component. Eventually wraps it into
     * another component in order to provide additional functionality.
     */
    protected Component wrapViewComponent(View p) {
        JComponent c = p.getComponent();
        if (getModel() != null) {
            LinkedList<Action> toolBarActions = new LinkedList<Action>();

            int id = 0;
            for (JToolBar tb : new ReversedList<JToolBar>(getModel().createToolBars(this, p))) {
                id++;
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(tb, BorderLayout.NORTH);
                panel.add(c, BorderLayout.CENTER);
                c = panel;
                PreferencesUtil.installToolBarPrefsHandler(prefs, "toolbar." + id, tb);
                toolBarActions.addFirst(new ToggleVisibleAction(tb, tb.getName()));
            }
            p.getComponent().putClientProperty("toolBarActions", toolBarActions);
        }
        return c;
    }

    public void hide(View p) {
        if (p.isShowing()) {
            p.setShowing(false);
            JFrame f = (JFrame) SwingUtilities.getWindowAncestor(p.getComponent());
            f.setVisible(false);
            f.remove(p.getComponent());
            f.dispose();
        }
    }

    public void dispose(View p) {
        super.dispose(p);
        if (views().size() == 0) {
            stop();
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
        maybeAddSeparator(m);
        addAction(m, view, SaveFileAction.ID);
        addAction(m, view, SaveFileAsAction.ID);
        addAction(m, view, ExportFileAction.ID);
        addAction(m, view, PrintFileAction.ID);

        maybeAddSeparator(m);
        addAction(m, view, CloseFileAction.ID);

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
        maybeAddSeparator(m);
        addAction(m, view, AbstractPreferencesAction.ID);
        return (m.getPopupMenu().getComponentCount() == 0) ? null : m;
    }

    /**
     * Updates the title of a view and displays it in the given frame.
     * 
     * @param view The view.
     * @param f The frame.
     */
    protected void updateViewTitle(View view, JFrame f) {
        URI uri = view.getURI();
        String title;
        if (uri == null) {
            title = labels.getString("unnamedFile");
        } else {
            title = URIUtil.getName(uri);
        }
        if (view.hasUnsavedChanges()) {
            title += "*";
        }
        view.setTitle(labels.getFormatted("frame.title", title, getName(), view.getMultipleOpenId()));
        f.setTitle(view.getTitle());
    }

    public boolean isSharingToolsAmongViews() {
        return false;
    }

    public Component getComponent() {
        View p = getActiveView();
        return (p == null) ? null : p.getComponent();
    }

    public JMenu createWindowMenu(View view) {
        return null;
    }

    /**
     * Creates the window menu.
     * 
     * @param view The View
     * @return A JMenu or null, if the menu doesn't have any items.
     */
    @SuppressWarnings("unchecked")
    public JMenu createViewMenu(final View view) {
        Object object = view.getComponent().getClientProperty("toolBarActions");
        LinkedList<Action> viewActions = (LinkedList<Action>) object;
        ApplicationModel model = getModel();
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");

        JMenu m, m2;
        JMenuItem mi;
        JCheckBoxMenuItem cbmi;

        m = new JMenu();
        if (viewActions != null && viewActions.size() > 0) {
            m2 = (viewActions.size() == 1) ? m : new JMenu(labels.getString("toolBars"));
            labels.configureMenu(m, "view");
            for (Action a : viewActions) {
                cbmi = new JCheckBoxMenuItem(a);
                ActionUtil.configureJCheckBoxMenuItem(cbmi, a);
                m2.add(cbmi);
            }
            if (m2 != m) {
                m.add(m2);
            }
        }

        return (m.getPopupMenu().getComponentCount() > 0) ? m : null;
    }

    
    public JMenu createHelpMenu(View p) {
        ApplicationModel model = getModel();
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw_7_4_1.app.Labels");

        JMenu m;
        JMenuItem mi;

        m = new JMenu();
        labels.configureMenu(m, "help");
        m.add(getAction(p, AboutAction.ID));

        return m;
    }

    protected ActionMap createModelActionMap(ApplicationModel mo) {
        ActionMap rootMap = new ActionMap();
        rootMap.put(AboutAction.ID, new AboutAction(this));
        rootMap.put(ClearRecentFilesMenuAction.ID, new ClearRecentFilesMenuAction(this));

        ActionMap moMap = mo.createActionMap(this, null);
        moMap.setParent(rootMap);
        return moMap;
    }

    
    protected ActionMap createViewActionMap(View v) {
        ActionMap intermediateMap = new ActionMap();
        intermediateMap.put(CloseFileAction.ID, new CloseFileAction(this, v));

        ActionMap vMap = model.createActionMap(this, v);
        vMap.setParent(intermediateMap);
        intermediateMap.setParent(getActionMap(null));
        return vMap;
    }
}
