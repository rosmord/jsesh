/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import jsesh.sharedvocabulary.InsertionDirection;

/// Direct control of an insertion: a 3×3 grid of directions, and steppers for the
/// enlargement of the main group's frame.
///
/// The grid is shown as the text is written: in right-to-left text, the "start"
/// column is on the right. Its cells match the numeric keypad (7 is the top left
/// cell). Directions where the inserted group doesn't fit are dimmed, but can be chosen.
///
/// The enlargements change by steps of [#STEP] percent. A stepper is disabled when
/// its enlargement has no effect for the current direction.
///
/// The components are not focusable, so that the keys stay with the list of the popup.
@SuppressWarnings("serial")
class InsertionCompass extends JPanel {

    /// What the user asks for.
    interface Listener {
        /// A direction was clicked.
        void directionChosen(InsertionDirection direction);

        /// An enlargement stepper was clicked.
        ///
        /// @param deltaDw change of the horizontal enlargement, in percent.
        /// @param deltaDh change of the vertical enlargement, in percent.
        void enlargementChanged(int deltaDw, int deltaDh);
    }

    /// Step of the enlargement steppers, in percent.
    static final int STEP = 5;

    /// The directions in the grid, row by row, for left-to-right text.
    private static final InsertionDirection[][] GRID = {
            { InsertionDirection.TOP_START, InsertionDirection.TOP, InsertionDirection.TOP_END },
            { InsertionDirection.START, InsertionDirection.CENTER, InsertionDirection.END },
            { InsertionDirection.BOTTOM_START, InsertionDirection.BOTTOM, InsertionDirection.BOTTOM_END } };

    /// Arrows for the grid cells, row by row: they point to the center, as the
    /// inserted group comes in from that side.
    private static final String[][] ARROWS = {
            { "↘", "↓", "↙" },
            { "→", "•", "←" },
            { "↗", "↑", "↖" } };

    private final boolean rightToLeft;
    private final Map<InsertionDirection, JToggleButton> directionButtons = new EnumMap<>(InsertionDirection.class);
    private final ButtonGroup directionGroup = new ButtonGroup();
    private final JLabel dwLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel dhLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton dwMinus;
    private final JButton dwPlus;
    private final JButton dhMinus;
    private final JButton dhPlus;
    private final Color normalForeground;
    private final Color dimmedForeground;

    /// @param rightToLeft true if the text is written from right to left.
    /// @param background the background of the popup.
    /// @param listener notified of the user's actions.
    InsertionCompass(boolean rightToLeft, Color background, Listener listener) {
        super(new GridBagLayout());
        this.rightToLeft = rightToLeft;
        setBackground(background);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));

        JPanel grid = new JPanel(new GridLayout(3, 3, 2, 2));
        grid.setOpaque(false);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                InsertionDirection direction = directionAt(row, column);
                JToggleButton button = new JToggleButton(ARROWS[row][column]);
                prepare(button);
                button.addActionListener(e -> listener.directionChosen(direction));
                directionGroup.add(button);
                directionButtons.put(direction, button);
                grid.add(button);
            }
        }
        normalForeground = directionButtons.get(InsertionDirection.CENTER).getForeground();
        Color disabled = UIManager.getColor("Label.disabledForeground");
        dimmedForeground = disabled != null ? disabled : Color.GRAY;

        dwMinus = stepper("−", "dw", () -> listener.enlargementChanged(-STEP, 0));
        dwPlus = stepper("+", "dw", () -> listener.enlargementChanged(STEP, 0));
        dhMinus = stepper("−", "dh", () -> listener.enlargementChanged(0, -STEP));
        dhPlus = stepper("+", "dh", () -> listener.enlargementChanged(0, STEP));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.insets = new Insets(0, 0, 6, 0);
        add(grid, c);
        addStepperRow(1, "↔", dwMinus, dwLabel, dwPlus);
        addStepperRow(2, "↕", dhMinus, dhLabel, dhPlus);
    }

    /// The direction of a cell of the grid, as shown.
    ///
    /// @param row 0 for the top row.
    /// @param column 0 for the left column.
    InsertionDirection directionAt(int row, int column) {
        return GRID[row][rightToLeft ? 2 - column : column];
    }

    /// Shows the state of the current insertion.
    ///
    /// @param current the direction of the insertion in the text.
    /// @param choices the choice for each direction (with the same groups), or null if there is none.
    /// @param dw the horizontal enlargement, in percent.
    /// @param dh the vertical enlargement, in percent.
    void update(InsertionDirection current, Function<InsertionDirection, InsertionChoice> choices, int dw, int dh) {
        for (Map.Entry<InsertionDirection, JToggleButton> e : directionButtons.entrySet()) {
            InsertionDirection direction = e.getKey();
            JToggleButton button = e.getValue();
            InsertionChoice choice = choices.apply(direction);
            button.setEnabled(choice != null);
            boolean fits = choice != null && choice.fits();
            button.setForeground(fits ? normalForeground : dimmedForeground);
            button.setToolTipText(choice == null ? direction.getCode()
                    : direction.getCode() + " " + Math.round(choice.scale() * 100) + "%");
        }
        JToggleButton selected = directionButtons.get(current);
        if (selected != null) {
            selected.setSelected(true);
        } else {
            directionGroup.clearSelection();
        }
        boolean usesDw = current != null && current.usesDw();
        boolean usesDh = current != null && current.usesDh();
        dwLabel.setText(dw + "%");
        dhLabel.setText(dh + "%");
        dwLabel.setEnabled(usesDw);
        dhLabel.setEnabled(usesDh);
        dwMinus.setEnabled(usesDw && dw > 0);
        dwPlus.setEnabled(usesDw && dw < JMDCEditorWorkflow.MAX_INSERTION_ENLARGEMENT);
        dhMinus.setEnabled(usesDh && dh > 0);
        dhPlus.setEnabled(usesDh && dh < JMDCEditorWorkflow.MAX_INSERTION_ENLARGEMENT);
    }

    private void addStepperRow(int row, String symbol, JButton minus, JLabel value, JButton plus) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(1, 1, 1, 1);
        c.gridx = 0;
        add(new JLabel(symbol), c);
        c.gridx = 1;
        add(minus, c);
        c.gridx = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        // Room for "200%".
        value.setPreferredSize(new JLabel("000%").getPreferredSize());
        add(value, c);
        c.gridx = 3;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        add(plus, c);
    }

    private static JButton stepper(String text, String toolTip, Runnable action) {
        JButton button = new JButton(text);
        prepare(button);
        button.setToolTipText(toolTip);
        button.addActionListener(e -> action.run());
        return button;
    }

    private static void prepare(AbstractButton button) {
        button.setFocusable(false);
        button.setMargin(new Insets(1, 4, 1, 4));
    }
}
