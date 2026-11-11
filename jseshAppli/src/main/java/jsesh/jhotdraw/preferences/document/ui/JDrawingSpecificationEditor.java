/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw.preferences.document.ui;

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import jsesh.jhotdraw.dialogs.ZoneParametersPanel;
import jsesh.jhotdraw.utils.FormLayoutHelper;
import jsesh.model.LayoutSetting;
import jsesh.resources.JSeshMessages;
import jsesh.ui.widgets.units.LengthUnit;
import net.miginfocom.swing.MigLayout;

/// The form of the drawing preferences of a document.
///
/// The unit of the document (the height of a quadrat) is the only absolute
/// length of the text: changing it scales the whole document. The other
/// lengths of the text are relative to it, with the same names as the
/// options of zones (see [LayoutSetting]). They are shown in percents of it by
/// default, which is how they are saved, but they can be shown in another unit
/// (see [DocumentLengthUnit]): so there are two unit choices, one for the unit
/// of the document, and one for the other lengths. The small body limit, a
/// legibility limit, is in absolute units too, like the unit of the document.
public class JDrawingSpecificationEditor {

	private final JPanel panel = new JPanel();

	private final JFormattedTextField documentUnitField = new JFormattedTextField();
	private final JComboBox<LengthUnit> lengthUnitField = new JComboBox<>();
	private final JFormattedTextField smallFontBodyLimitField = new JFormattedTextField();
	private final JComboBox<DocumentLengthUnit> lengthsUnitField = new JComboBox<>();
	private final Map<LayoutSetting, JFormattedTextField> lengthFields = new EnumMap<>(LayoutSetting.class);
	private final Map<LayoutSetting, JCheckBox> flagCheckBoxes = new EnumMap<>(LayoutSetting.class);
	private final JCheckBox useLinesForShadingCheckBox = new JCheckBox(
			JSeshMessages.getString("drawingPrefs.useLinesForShadingCheckBox"));
	private final JCheckBox underlineRedCheckBox = new JCheckBox(
			JSeshMessages.getString("drawingPrefs.underlineRedCheckBox"));

	public JDrawingSpecificationEditor() {
		for (LayoutSetting setting : LayoutSetting.values()) {
			switch (setting.kind()) {
				case LENGTH -> lengthFields.put(setting, LengthFieldsMediator.createField());
				case FLAG -> flagCheckBoxes.put(setting,
						new JCheckBox(JSeshMessages.getString(ZoneParametersPanel.labelKey(setting))));
				default -> {
					// The orientation and the direction are chosen elsewhere.
				}
			}
		}
		layout();
	}

	/**
	 * Requests the focus in one of the fields when the dialog is opened.
	 */
	public void addFocus() {
		panel.addAncestorListener(new AncestorListener() {
			public void ancestorRemoved(AncestorEvent event) {
			}

			public void ancestorMoved(AncestorEvent event) {
			}

			public void ancestorAdded(AncestorEvent event) {
				documentUnitField.requestFocusInWindow();
			}
		});
	}

	public JPanel getPanel() {
		return panel;
	}

	/// Places the fields in three sections: the unit, the lengths (relative to
	/// the unit, with their own unit choice), and the yes/no options. The lengths are beside the two
	/// others when the screen is wide enough, below them otherwise; the dialog
	/// scrolls when it is still too tall.
	private void layout() {
		JPanel unitSection = FormLayoutHelper.section(JSeshMessages.getString("drawingPrefs.section.unit"));
		addRow(unitSection, "drawingPrefs.documentUnit", documentUnitField, "w 50pt");
		addRow(unitSection, "drawingPrefs.units", lengthUnitField, "");
		addRow(unitSection, "drawingPrefs.smallFontBodyLimit", smallFontBodyLimitField, "w 50pt");

		JPanel optionSection = FormLayoutHelper.section(JSeshMessages.getString("drawingPrefs.section.options"));
		for (JCheckBox box : flagCheckBoxes.values()) {
			optionSection.add(box, "span, wrap");
		}
		optionSection.add(useLinesForShadingCheckBox, "span, wrap");
		optionSection.add(underlineRedCheckBox, "span, wrap");

		JPanel lengthSection = FormLayoutHelper.section(JSeshMessages.getString("drawingPrefs.section.lengths"));
		addRow(lengthSection, "drawingPrefs.lengthsUnit", lengthsUnitField, "");
		for (Map.Entry<LayoutSetting, JFormattedTextField> e : lengthFields.entrySet()) {
			addRow(lengthSection, ZoneParametersPanel.labelKey(e.getKey()), e.getValue(), "w 50pt");
		}

		JPanel left = new JPanel(new MigLayout("insets 0, fillx", "[grow, fill]"));
		left.add(unitSection, "wrap");
		left.add(optionSection, "wrap");
		int sideBySideWidth = left.getPreferredSize().width + lengthSection.getPreferredSize().width + 20;
		panel.setLayout(new MigLayout());
		panel.add(left, FormLayoutHelper.hasRoomFor(sideBySideWidth) ? "aligny top" : "aligny top, growx, wrap");
		panel.add(lengthSection, "aligny top, growx");
	}

	private static void addRow(JPanel section, String labelCode, Component field, String constraints) {
		section.add(FormLayoutHelper.wrappingLabel(JSeshMessages.getString(labelCode)));
		section.add(field, constraints + (constraints.isEmpty() ? "" : ", ") + "growx 0, wrap");
	}

	/// @return the unit of the document, in the current length unit.
	public JFormattedTextField getDocumentUnitField() {
		return documentUnitField;
	}

	/// @return the choice of the unit of the document and of the small body
	/// limit (points, mm...).
	public JComboBox<LengthUnit> getLengthUnitField() {
		return lengthUnitField;
	}

	/// @return the choice of the unit of the other lengths (percents of the
	/// unit of the document, points, mm...).
	public JComboBox<DocumentLengthUnit> getLengthsUnitField() {
		return lengthsUnitField;
	}

	public JFormattedTextField getSmallFontBodyLimitField() {
		return smallFontBodyLimitField;
	}

	/// @return the fields of the length settings, in the unit chosen with
	/// [#getLengthsUnitField()].
	public Map<LayoutSetting, JFormattedTextField> getLengthFields() {
		return lengthFields;
	}

	/// @return the check boxes of the yes/no settings.
	public Map<LayoutSetting, JCheckBox> getFlagCheckBoxes() {
		return flagCheckBoxes;
	}

	public JCheckBox getUseLinesForShadingCheckBox() {
		return useLinesForShadingCheckBox;
	}

	public JCheckBox getUnderlineRedCheckBox() {
		return underlineRedCheckBox;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JDrawingSpecificationEditor editor = new JDrawingSpecificationEditor();
			frame.add(editor.getPanel());
			frame.pack();
			frame.setVisible(true);
		});
	}
}
