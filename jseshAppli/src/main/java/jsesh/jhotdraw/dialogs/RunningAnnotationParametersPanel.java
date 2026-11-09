/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.dialogs;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.FormLayoutHelper;
import jsesh.model.RunningAnnotation;
import jsesh.sharedvocabulary.RunningAnnotationKind;
import net.miginfocom.swing.MigLayout;

/// A panel to choose the length of a new [RunningAnnotation] (haplography or
/// extended lacuna).
///
/// The annotation either fills the rest of its line (or of its tab cell),
/// which is the default, or has a fixed length in percent of a quadrat, as in
/// the `size` option of `%haplography[...]` and `%lacuna[...]`.
@SuppressWarnings("serial")
public class RunningAnnotationParametersPanel extends JPanel {

    private static final int DEFAULT_LENGTH = RunningAnnotation.DEFAULT_PERCENT;

    private final JCheckBox fill = new JCheckBox();
    private final JSpinner length = new JSpinner(new SpinnerNumberModel(DEFAULT_LENGTH, 1, 100000, 10));

    /// Builds a panel proposing to fill the line.
    public RunningAnnotationParametersPanel() {
        super(new MigLayout("insets 0, wrap 2", "[][grow, fill]"));
        BundleHelper labels = BundleHelper.getInstance();
        add(FormLayoutHelper.wrappingLabel(labels.getLabel("runningAnnotation.unitNote"),
                2 * FormLayoutHelper.LABEL_WIDTH), "span 2");
        add(FormLayoutHelper.wrappingLabel(labels.getLabel("runningAnnotation.fill")));
        add(fill);
        add(FormLayoutHelper.wrappingLabel(labels.getLabel("runningAnnotation.width")));
        add(length, "growx 0");
        fill.addActionListener(e -> length.setEnabled(!fill.isSelected()));
        fill.setSelected(true);
        length.setEnabled(false);
    }

    /// @param kind haplography or lacuna.
    /// @return a new annotation, with the length chosen by the user.
    public RunningAnnotation getAnnotation(RunningAnnotationKind kind) {
        if (fill.isSelected()) {
            return RunningAnnotation.filling(kind);
        }
        return new RunningAnnotation(kind, (Integer) length.getValue());
    }
}
