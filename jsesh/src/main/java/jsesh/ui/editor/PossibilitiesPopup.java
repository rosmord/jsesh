/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
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

import jsesh.glyphs.signdata.Possibility;
import jsesh.render.draw.MDCIconFactory;

/// A small window showing the possibilities for the sign just entered.
///
/// This is the "list" counterpart of cycling with the space key. The
/// possibilities are laid out in a grid, as in the shading menu, using the
/// colors and border of the look and feel's popup menus. The grid is
/// navigated with the arrow keys (left and right move along a row, up and down
/// along a column), and the text is updated as the selection moves, so that
/// the user sees the actual result. When there are too many possibilities for
/// the screen, the grid gets scrollbars.
///
/// - Enter, or a click, validates the current choice;
/// - Escape restores the possibility which was in the text when the list was opened;
/// - leaving the window (e.g. clicking in the editor) keeps the current choice.
///
/// Use [JMDCEditorWorkflow#prepareChoiceOfPossibility()] before opening the list.
class PossibilitiesPopup {

    private static final int MAX_COLUMNS = 6;
    private static final int MAX_VISIBLE_ROWS = 8;
    /// The popup uses at most this part of the screen's width and height.
    private static final double MAX_SCREEN_RATIO = 0.8;
    private static final int CELL_PADDING = 3;

    private final JMDCEditor editor;
    private final JMDCEditorWorkflow workflow;
    private final JWindow window;
    private final JList<Possibility> list;
    private final int originalIndex;
    private boolean closed = false;

    /// @param editor the editor which owns the text.
    /// @param workflow the editor's workflow; the possibilities have been prepared
    ///     with [JMDCEditorWorkflow#prepareChoiceOfPossibility()].
    /// @param iconFactory used to draw the possibilities.
    PossibilitiesPopup(JMDCEditor editor, JMDCEditorWorkflow workflow, MDCIconFactory iconFactory) {
        this.editor = editor;
        this.workflow = workflow;
        this.originalIndex = workflow.getSelectedPossibilityIndex();

        list = new JList<>(workflow.getPossibilityChoices().toArray(new Possibility[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setBorder(BorderFactory.createEmptyBorder());
        list.setCellRenderer(new PossibilityRenderer(iconFactory));
        applyPopupColors(list);
        Dimension cellSize = computeCellSize();
        list.setFixedCellWidth(cellSize.width);
        list.setFixedCellHeight(cellSize.height);
        JScrollPane scrollPane = new JScrollPane(list);
        Dimension gridSize = layoutGrid(cellSize, scrollPane.getVerticalScrollBar().getPreferredSize().width);
        list.setSelectedIndex(originalIndex);
        list.ensureIndexIsVisible(originalIndex);
        // Listen only once the initial selection is in place.
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !closed && list.getSelectedIndex() >= 0) {
                workflow.selectPossibility(list.getSelectedIndex());
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
        installKey(list, "ENTER", "accept", () -> close(false));
        installKey(list, "SPACE", "accept", () -> close(false));
        installKey(list, "ESCAPE", "cancel", () -> close(true));

        window = new JWindow(SwingUtilities.getWindowAncestor(editor));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(list.getBackground());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(gridSize);
        window.setContentPane(scrollPane);
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

    /// The size of a cell: large enough for the largest possibility.
    private Dimension computeCellSize() {
        ListCellRenderer<? super Possibility> renderer = list.getCellRenderer();
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
    /// @param restore if true, the possibility chosen before opening the list is put back.
    private void close(boolean restore) {
        if (closed) {
            return;
        }
        if (restore) {
            workflow.selectPossibility(originalIndex);
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

    /// Shows a possibility as a picture with its code below.
    @SuppressWarnings("serial")
    private static class PossibilityRenderer extends DefaultListCellRenderer {
        private final MDCIconFactory iconFactory;

        PossibilityRenderer(MDCIconFactory iconFactory) {
            this.iconFactory = iconFactory;
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Possibility possibility = (Possibility) value;
            setText(possibility.getCode());
            setIcon(iconFor(possibility));
            // The superclass sets its own border (for the focus), so this comes last.
            setBorder(BorderFactory.createEmptyBorder(CELL_PADDING, CELL_PADDING, CELL_PADDING, CELL_PADDING));
            return this;
        }

        private Icon iconFor(Possibility possibility) {
            try {
                return iconFactory.buildImage(possibility.getCode());
            } catch (RuntimeException e) {
                // A glossary entry which can't be drawn is still listed by its code.
                return null;
            }
        }
    }
}
