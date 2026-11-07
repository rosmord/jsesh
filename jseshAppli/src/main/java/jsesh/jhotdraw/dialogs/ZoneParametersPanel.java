/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.dialogs;

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.FormLayoutHelper;
import jsesh.model.LayoutSetting;
import jsesh.model.TextLayoutSettings;
import jsesh.model.ZoneParameters;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import net.miginfocom.swing.MigLayout;

/// A panel to choose the settings of a [jsesh.model.Zone].
///
/// Each setting can be left to the value inherited from the enclosing text.
/// Lengths are shown in percent of the unit of the document (the height of a
/// quadrat), as in the `%zone[...]` syntax. The labels don't repeat it: a note
/// on top of the panel says it once. The shared layout settings are
/// listed from [LayoutSetting], so that a new one appears here.
///
/// The settings are in two sections: those of the zone itself (its direction,
/// size, scale and frame), and the layout of its text. The sections are side by
/// side when the screen is wide enough, one above the other otherwise; the
/// dialog showing the panel (see [jsesh.jhotdraw.utils.JSimpleDialog]) scrolls
/// when it is still too tall.
@SuppressWarnings("serial")
public class ZoneParametersPanel extends JPanel {

    /// A length which can be left unset.
    private static class OptionalLength {
        final JCheckBox enabled = new JCheckBox();
        final JSpinner value;

        /// @param initial the value proposed when the length gets enabled
        /// @param minimum the smallest value the user can enter
        /// @param step the increment of the spinner arrows
        OptionalLength(int initial, int minimum, int step) {
            value = new JSpinner(new SpinnerNumberModel(initial, minimum, 100000, step));
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

        JPanel panel() {
            JPanel p = new JPanel(new MigLayout("insets 0"));
            p.add(enabled);
            p.add(value);
            return p;
        }
    }

    // The first item of the combo boxes is null: inherited.
    private final JComboBox<TextOrientation> orientation = new JComboBox<>(
            new TextOrientation[] { null, TextOrientation.HORIZONTAL, TextOrientation.VERTICAL });
    private final JComboBox<TextDirection> direction = new JComboBox<>(
            new TextDirection[] { null, TextDirection.LEFT_TO_RIGHT, TextDirection.RIGHT_TO_LEFT });
    /// The editors of the shared length settings.
    private final Map<LayoutSetting, OptionalLength> lengths = new EnumMap<>(LayoutSetting.class);
    /// The editors of the shared yes/no settings.
    private final Map<LayoutSetting, JComboBox<Boolean>> flags = new EnumMap<>(LayoutSetting.class);
    private final OptionalLength scale = new OptionalLength(50, 1, 10);
    private final OptionalLength width = new OptionalLength(100, 1, 10);
    private final OptionalLength height = new OptionalLength(100, 1, 10);
    private final OptionalLength tabGap = new OptionalLength(10, 0, 1);
    private final OptionalLength separator = new OptionalLength(5, 1, 1);
    private final JCheckBox frame = new JCheckBox();

    public ZoneParametersPanel(ZoneParameters parameters) {
        super(new MigLayout("insets 0"));
        BundleHelper labels = BundleHelper.getInstance();
        orientation.setRenderer((list, value, index, selected, focus) -> new JLabel(
                labels.getLabel(value == null ? "zone.inherited" : "zone.orientation." + value.name())));
        direction.setRenderer((list, value, index, selected, focus) -> new JLabel(
                labels.getLabel(value == null ? "zone.inherited" : "zone.direction." + value.name())));

        JPanel zoneSection = FormLayoutHelper.section(labels.getLabel("zone.section.zone"));
        addRow(zoneSection, "zone.orientation", orientation);
        addRow(zoneSection, "zone.direction", direction);
        addRow(zoneSection, "zone.scale", scale.panel());
        addRow(zoneSection, "zone.width", width.panel());
        addRow(zoneSection, "zone.height", height.panel());
        addRow(zoneSection, "zone.tabGap", tabGap.panel());
        addRow(zoneSection, "zone.separator", separator.panel());
        addRow(zoneSection, "zone.frame", frame);

        JPanel layoutSection = FormLayoutHelper.section(labels.getLabel("zone.section.layout"));
        for (LayoutSetting setting : LayoutSetting.values()) {
            switch (setting.kind()) {
                case LENGTH -> {
                    OptionalLength editor = new OptionalLength(
                            (int) Math.round((Double) setting.documentDefault()), 0, 1);
                    lengths.put(setting, editor);
                    addRow(layoutSection, labelKey(setting), editor.panel());
                }
                case FLAG -> {
                    JComboBox<Boolean> editor = new JComboBox<>(new Boolean[] { null, Boolean.TRUE, Boolean.FALSE });
                    editor.setRenderer((list, value, index, selected, focus) -> new JLabel(labels.getLabel(
                            value == null ? "zone.inherited" : value ? "layoutSetting.yes" : "layoutSetting.no")));
                    flags.put(setting, editor);
                    addRow(layoutSection, labelKey(setting), editor);
                }
                default -> {
                    // Orientation and direction are in the zone section.
                }
            }
        }

        int sideBySideWidth = zoneSection.getPreferredSize().width + layoutSection.getPreferredSize().width + 20;
        boolean sideBySide = FormLayoutHelper.hasRoomFor(sideBySideWidth);
        String placement = sideBySide ? "aligny top" : "aligny top, wrap";
        // The unit is the same for all the lengths: it is said once, on top,
        // rather than in each label. The note is as wide as the sections.
        int contentWidth = sideBySide ? sideBySideWidth
                : Math.max(zoneSection.getPreferredSize().width, layoutSection.getPreferredSize().width);
        add(FormLayoutHelper.wrappingLabel(labels.getLabel("zone.unitNote"), contentWidth - 20), "span, wrap");
        add(zoneSection, placement + ", growx");
        add(layoutSection, "aligny top, growx");
        setParameters(parameters);
    }

    /// @param setting a shared layout setting.
    /// @return the key of its label (shared with the document preferences).
    public static String labelKey(LayoutSetting setting) {
        return "layoutSetting." + setting.settingName();
    }

    private static void addRow(JPanel section, String labelKey, Component component) {
        section.add(FormLayoutHelper.wrappingLabel(BundleHelper.getInstance().getLabel(labelKey)));
        section.add(component, "growx 0, wrap");
    }

    /// Shows some settings.
    public void setParameters(ZoneParameters p) {
        TextLayoutSettings settings = p.settings();
        orientation.setSelectedItem(settings.orientation());
        direction.setSelectedItem(settings.direction());
        lengths.forEach((setting, editor) -> {
            Double value = settings.length(setting);
            editor.set(value == null ? ZoneParameters.UNSET : (int) Math.round(value));
        });
        flags.forEach((setting, editor) -> editor.setSelectedItem(settings.flag(setting)));
        scale.set(p.scale());
        width.set(p.width());
        height.set(p.height());
        tabGap.set(p.tabGap());
        separator.set(p.separator());
        frame.setSelected(p.frame());
    }

    /// @return the settings chosen by the user.
    public ZoneParameters getParameters() {
        TextLayoutSettings settings = TextLayoutSettings.INHERIT_ALL
                .withOrientation((TextOrientation) orientation.getSelectedItem())
                .withDirection((TextDirection) direction.getSelectedItem());
        for (Map.Entry<LayoutSetting, OptionalLength> e : lengths.entrySet()) {
            int value = e.getValue().get();
            settings = settings.withLength(e.getKey(), value == ZoneParameters.UNSET ? null : (double) value);
        }
        for (Map.Entry<LayoutSetting, JComboBox<Boolean>> e : flags.entrySet()) {
            settings = settings.withFlag(e.getKey(), (Boolean) e.getValue().getSelectedItem());
        }
        return new ZoneParameters(settings, scale.get(), width.get(), height.get(), tabGap.get(),
                separator.get(), frame.isSelected());
    }
}
