/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.documentview;

import java.awt.Component;
import java.awt.Dimension;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.arrows.ArrowKeyMode;
import jsesh.ui.editor.arrows.LogicalArrowKeyPolicy;

/// The toolbar part which lets the user choose how the arrow keys read the text
/// (lines or columns, left-to-right or right-to-left), in a single combo box.
///
/// It is only visible when the editor uses a switchable
/// [LogicalArrowKeyPolicy]; it follows the editor's policy (property
/// [JMDCEditor#ARROW_KEY_POLICY_PROPERTY]). All the toolbar code for the arrow
/// keys lives here, so that changing the way arrows are handled doesn't touch
/// [JSeshViewComponent].
@SuppressWarnings("serial")
final class ArrowKeyControls extends JPanel {

    private final JMDCEditor editor;

    /// The four reading modes offered to the user: lines then columns, each
    /// left-to-right then right-to-left.
    private static final ArrowKeyMode[] MODES = {
        new ArrowKeyMode(TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL),
        new ArrowKeyMode(TextDirection.RIGHT_TO_LEFT, TextOrientation.HORIZONTAL),
        new ArrowKeyMode(TextDirection.LEFT_TO_RIGHT, TextOrientation.VERTICAL),
        new ArrowKeyMode(TextDirection.RIGHT_TO_LEFT, TextOrientation.VERTICAL)
    };

    private final JComboBox<ArrowKeyMode> modeComboBox = new JComboBox<>(MODES);

    /// True while the combo box is updated from the editor.
    private boolean updating;

    ArrowKeyControls(JMDCEditor editor) {
        this.editor = editor;
        BundleHelper bundle = BundleHelper.getInstance();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createRigidArea(new Dimension(16, 0)));
        add(new JLabel(bundle.getLabel("arrowMode.direction.label")));
        add(Box.createRigidArea(new Dimension(4, 0)));
        configure(modeComboBox, "arrowMode.mode.toolTipText",
                m -> "arrowMode.mode."
                + (m.orientation().isHorizontal() ? "horizontal" : "vertical")
                + (m.direction().isLeftToRight() ? ".ltr" : ".rtl"));
        add(modeComboBox);
        setMaximumSize(getPreferredSize());

        modeComboBox.addActionListener(e -> changeMode());
        editor.addPropertyChangeListener(JMDCEditor.ARROW_KEY_POLICY_PROPERTY, e -> updateFromEditor());
        updateFromEditor();
    }

    private <T> void configure(JComboBox<T> comboBox, String toolTipKey, Function<T, String> labelKey) {
        BundleHelper bundle = BundleHelper.getInstance();
        comboBox.setFocusable(false);
        comboBox.setToolTipText(bundle.getLabel(toolTipKey));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String text = value == null ? "" : bundle.getLabel(labelKey.apply((T) value));
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        comboBox.setMaximumSize(comboBox.getPreferredSize());
    }

    private void updateFromEditor() {
        if (editor.getArrowKeyPolicy() instanceof LogicalArrowKeyPolicy policy && policy.switchable()) {
            updating = true;
            try {
                modeComboBox.setSelectedItem(policy.mode());
            } finally {
                updating = false;
            }
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    private void changeMode() {
        if (updating || !(editor.getArrowKeyPolicy() instanceof LogicalArrowKeyPolicy policy)) {
            return;
        }
        editor.setArrowKeyPolicy(policy.withMode((ArrowKeyMode) modeComboBox.getSelectedItem()));
        editor.requestFocusInWindow();
    }
}
