/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingUtilities;


/// A small window for choosing among a few alternatives, shown as pictures.
///
/// It is used to choose among the possibilities for the sign just entered (the
/// "list" counterpart of cycling with the space key), and among the ways of
/// inserting a group into another. The choices are laid out in a grid, as in the
/// shading menu, using the colors and border of the look and feel's popup menus.
/// The grid is navigated with the arrow keys (left and right move along a row, up
/// and down along a column), and the text is updated as the selection moves, so
/// that the user sees the actual result. When there are too many choices for the
/// screen, the grid gets scrollbars.
///
/// - Enter, or a click, validates the current choice;
/// - Escape restores the choice which was in the text when the list was opened;
/// - leaving the window (e.g. clicking in the editor) keeps the current choice.
///
/// @param <T> the type of the choices.
class ChoicePopup<T> {

    /// What the popup chooses from, and how it shows it.
    ///
    /// @param <T> the type of the choices.
    interface Choices<T> {
        /// The choices, in display order.
        List<T> items();

        /// Index of the choice currently in the text.
        int selectedIndex();

        /// Puts a choice in the text.
        void select(int index);

        /// The text shown below a choice.
        String label(T item);

        /// The picture of a choice.
        ///
        /// @return the picture, or null if the choice can't be drawn.
        Icon icon(T item);

        /// Should a choice be shown as less advisable (it stays selectable)?
        default boolean isDimmed(T item) {
            return false;
        }

        /// Puts back the choice which was in the text when the popup was opened.
        ///
        /// @param originalIndex the index of that choice, when the popup was opened.
        default void cancel(int originalIndex) {
            select(originalIndex);
        }
    }

    private static final int MAX_COLUMNS = 6;
    private static final int MAX_VISIBLE_ROWS = 8;
    /// The popup uses at most this part of the screen's width and height.
    private static final double MAX_SCREEN_RATIO = 0.8;
    private static final int CELL_PADDING = 3;

    private final JMDCEditor editor;
    private final Choices<T> choices;
    private final JWindow window;
    private final JList<T> list;
    private final JScrollPane scrollPane;
    private final int originalIndex;
    private boolean closed = false;
    /// True while the list is updated from the choices, which must then not be selected again.
    private boolean refreshing = false;

    /// @param editor the editor which owns the text.
    /// @param choices what to choose from.
    ChoicePopup(JMDCEditor editor, Choices<T> choices) {
        this(editor, choices, null);
    }

    /// @param editor the editor which owns the text.
    /// @param choices what to choose from.
    /// @param accessory a component shown beside the list, or null. It should not
    ///     be focusable: the keys are handled by the list.
    ChoicePopup(JMDCEditor editor, Choices<T> choices, JComponent accessory) {
        this.editor = editor;
        this.choices = choices;
        this.originalIndex = choices.selectedIndex();

        list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setBorder(BorderFactory.createEmptyBorder());
        list.setCellRenderer(new ChoiceRenderer());
        applyPopupColors(list);
        scrollPane = new JScrollPane(list);
        loadChoices();
        // Listen only once the initial selection is in place.
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !closed && !refreshing && list.getSelectedIndex() >= 0) {
                choices.select(list.getSelectedIndex());
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0 && list.getCellBounds(index, index).contains(e.getPoint())) {
                    close(false);
                }
            }
        });
        addKey("ENTER", "accept", () -> close(false));
        addKey("SPACE", "accept", () -> close(false));
        addKey("ESCAPE", "cancel", () -> close(true));

        window = new JWindow(SwingUtilities.getWindowAncestor(editor));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(list.getBackground());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (accessory == null) {
            window.setContentPane(scrollPane);
        } else {
            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(list.getBackground());
            content.add(scrollPane, BorderLayout.CENTER);
            content.add(accessory, BorderLayout.EAST);
            window.setContentPane(content);
        }
        window.getRootPane().setBorder(popupBorder());
        window.setFocusableWindowState(true);
        window.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                close(false);
            }
        });
        window.pack();
    }

    /// Fills the list with the choices, and sizes the grid for them.
    private void loadChoices() {
        refreshing = true;
        try {
            list.setListData(new Vector<>(choices.items()));
            Dimension cellSize = computeCellSize();
            list.setFixedCellWidth(cellSize.width);
            list.setFixedCellHeight(cellSize.height);
            scrollPane.setPreferredSize(
                    layoutGrid(cellSize, scrollPane.getVerticalScrollBar().getPreferredSize().width));
            int selected = choices.selectedIndex();
            if (selected >= 0) {
                list.setSelectedIndex(selected);
                list.ensureIndexIsVisible(selected);
            } else {
                list.clearSelection();
            }
        } finally {
            refreshing = false;
        }
    }

    /// Shows the choices again, after they (or the selected one) have changed
    /// otherwise than through the list.
    void refresh() {
        if (closed) {
            return;
        }
        loadChoices();
        window.pack();
    }

    /// Binds a key of the popup to an action.
    ///
    /// @param keyStroke the key, as accepted by [KeyStroke#getKeyStroke(String)].
    /// @param name a name for the action, unique in this popup.
    /// @param action what to do.
    void addKey(String keyStroke, String name, Runnable action) {
        installKey(list, keyStroke, name, action);
    }

    /// The size of a cell: large enough for the largest possibility.
    private Dimension computeCellSize() {
        ListCellRenderer<? super T> renderer = list.getCellRenderer();
        int width = 0;
        int height = 0;
        for (int i = 0; i < list.getModel().getSize(); i++) {
            Component c = renderer.getListCellRendererComponent(list, list.getModel().getElementAt(i), i, false, false);
            Dimension d = c.getPreferredSize();
            width = Math.max(width, d.width);
            height = Math.max(height, d.height);
        }
        return new Dimension(width, height);
    }

    /// Chooses the number of columns and visible rows of the grid, so that the
    /// popup stays within the screen.
    ///
    /// @param scrollBarWidth room to reserve when the grid needs a vertical scrollbar.
    /// @return the size the scroll pane should have.
    private Dimension layoutGrid(Dimension cellSize, int scrollBarWidth) {
        Rectangle screen = editor.getGraphicsConfiguration().getBounds();
        int count = list.getModel().getSize();
        int maxColumns = Math.max(1, Math.min(MAX_COLUMNS, (int) (screen.width * MAX_SCREEN_RATIO / cellSize.width)));
        int maxRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, (int) (screen.height * MAX_SCREEN_RATIO / cellSize.height)));
        // Spread the possibilities evenly: 7 possibilities make 2 rows of 4, not 6 + 1.
        int rows = (count + maxColumns - 1) / maxColumns;
        int columns = (count + rows - 1) / rows;
        // The preferred size of a wrapping list is computed from its visible row count.
        list.setVisibleRowCount(rows);
        int visibleRows = Math.min(rows, maxRows);
        int width = columns * cellSize.width;
        if (visibleRows < rows) {
            width += scrollBarWidth;
        }
        return new Dimension(width, visibleRows * cellSize.height);
    }

    /// The border of the look and feel's popup menus (or a plain one).
    private static Border popupBorder() {
        Border border = UIManager.getBorder("PopupMenu.border");
        return border != null ? border : BorderFactory.createLineBorder(Color.GRAY);
    }

    /// Gives the list the colors of the look and feel's popup menus.
    private static void applyPopupColors(JList<?> list) {
        Color background = UIManager.getColor("PopupMenu.background");
        Color foreground = UIManager.getColor("MenuItem.foreground");
        Color selectionBackground = UIManager.getColor("MenuItem.selectionBackground");
        Color selectionForeground = UIManager.getColor("MenuItem.selectionForeground");
        if (background != null) {
            list.setBackground(background);
        }
        if (foreground != null) {
            list.setForeground(foreground);
        }
        if (selectionBackground != null) {
            list.setSelectionBackground(selectionBackground);
        }
        if (selectionForeground != null) {
            list.setSelectionForeground(selectionForeground);
        }
    }

    /// Opens the list below the given area.
    ///
    /// @param area the area of interest, in the editor's coordinates (typically the cursor).
    void show(Rectangle area) {
        Point location = new Point(area.x, area.y + area.height);
        SwingUtilities.convertPointToScreen(location, editor);
        Dimension size = window.getSize();
        Rectangle screen = editor.getGraphicsConfiguration().getBounds();
        if (location.y + size.height > screen.y + screen.height) {
            // Not enough room below: try above the area.
            Point top = new Point(area.x, area.y);
            SwingUtilities.convertPointToScreen(top, editor);
            location.y = Math.max(screen.y, top.y - size.height);
        }
        location.x = Math.max(screen.x, Math.min(location.x, screen.x + screen.width - size.width));
        window.setLocation(location);
        window.setVisible(true);
        list.requestFocusInWindow();
    }

    /// Closes the list.
    ///
    /// @param restore if true, the choice which was in the text before opening the list is put back.
    private void close(boolean restore) {
        if (closed) {
            return;
        }
        if (restore) {
            choices.cancel(originalIndex);
        }
        closed = true;
        window.dispose();
        editor.requestFocusInWindow();
    }

    private static void installKey(JComponent component, String keyStroke, String name, Runnable action) {
        component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyStroke), name);
        component.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    /// Shows a choice as a picture with its label below.
    @SuppressWarnings("serial")
    private class ChoiceRenderer extends DefaultListCellRenderer {

        ChoiceRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            @SuppressWarnings("unchecked")
            T item = (T) value;
            setText(choices.label(item));
            setIcon(choices.icon(item));
            setEnabled(!choices.isDimmed(item));
            // The superclass sets its own border (for the focus), so this comes last.
            setBorder(BorderFactory.createEmptyBorder(CELL_PADDING, CELL_PADDING, CELL_PADDING, CELL_PADDING));
            return this;
        }
    }
}
