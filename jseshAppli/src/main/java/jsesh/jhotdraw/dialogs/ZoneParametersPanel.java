/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.model.ZoneParameters;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// A panel to choose the settings of a [jsesh.model.Zone].
///
/// Each setting can be left to the value inherited from the enclosing text.
/// Sizes are shown in percent of the height of a quadrat, as in the
/// `%zone[...]` syntax.
@SuppressWarnings("serial")
public class ZoneParametersPanel extends JPanel {

    /// A length which can be left unset.
    private static class OptionalLength {
        final JCheckBox enabled = new JCheckBox();
        final JSpinner value;

        OptionalLength() {
            this(100);
        }

        /// @param initial the value proposed when the length gets enabled
        OptionalLength(int initial) {
            value = new JSpinner(new SpinnerNumberModel(initial, 1, 100000, 10));
            value.setEnabled(false);
            enabled.addActionListener(e -> value.setEnabled(enabled.isSelected()));
        }

        void set(int length) {
            enabled.setSelected(length != ZoneParameters.UNSET);
            value.setEnabled(enabled.isSelected());
            if (length != ZoneParameters.UNSET) {
                value.setValue(length);
            }
        }

        int get() {
            return enabled.isSelected() ? (Integer) value.getValue() : ZoneParameters.UNSET;
        }
    }

    // The first item of the combo boxes is null: inherited.
    private final JComboBox<TextOrientation> orientation = new JComboBox<>(
            new TextOrientation[] { null, TextOrientation.HORIZONTAL, TextOrientation.VERTICAL });
    private final JComboBox<TextDirection> direction = new JComboBox<>(
            new TextDirection[] { null, TextDirection.LEFT_TO_RIGHT, TextDirection.RIGHT_TO_LEFT });
    private final OptionalLength width = new OptionalLength();
    private final OptionalLength height = new OptionalLength();
    private final OptionalLength lineSkip = new OptionalLength(10);
    private final OptionalLength columnSkip = new OptionalLength(10);
    private final OptionalLength separator = new OptionalLength(5);
    private final JCheckBox frame = new JCheckBox();

    public ZoneParametersPanel(ZoneParameters parameters) {
        super(new GridBagLayout());
        BundleHelper labels = BundleHelper.getInstance();
        orientation.setRenderer((list, value, index, selected, focus) -> new JLabel(
                labels.getLabel(value == null ? "zone.inherited" : "zone.orientation." + value.name())));
        direction.setRenderer((list, value, index, selected, focus) -> new JLabel(
                labels.getLabel(value == null ? "zone.inherited" : "zone.direction." + value.name())));

        int row = 0;
        addRow(row++, "zone.orientation", orientation);
        addRow(row++, "zone.direction", direction);
        addRow(row++, "zone.width", width);
        addRow(row++, "zone.height", height);
        addRow(row++, "zone.lineSkip", lineSkip);
        addRow(row++, "zone.columnSkip", columnSkip);
        addRow(row++, "zone.separator", separator);
        addRow(row++, "zone.frame", frame);
        setParameters(parameters);
    }

    private void addRow(int row, String labelKey, java.awt.Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        add(new JLabel(BundleHelper.getInstance().getLabel(labelKey)), c);
        c.gridx = 1;
        add(component, c);
    }

    private void addRow(int row, String labelKey, OptionalLength length) {
        JPanel p = new JPanel();
        p.add(length.enabled);
        p.add(length.value);
        addRow(row, labelKey, p);
    }

    /// Shows some settings.
    public void setParameters(ZoneParameters p) {
        orientation.setSelectedItem(p.orientation());
        direction.setSelectedItem(p.direction());
        width.set(p.width());
        height.set(p.height());
        lineSkip.set(p.lineSkip());
        columnSkip.set(p.columnSkip());
        separator.set(p.separator());
        frame.setSelected(p.frame());
    }

    /// @return the settings chosen by the user.
    public ZoneParameters getParameters() {
        return new ZoneParameters((TextOrientation) orientation.getSelectedItem(),
                (TextDirection) direction.getSelectedItem(), width.get(), height.get(),
                lineSkip.get(), columnSkip.get(), separator.get(), frame.isSelected());
    }
}
