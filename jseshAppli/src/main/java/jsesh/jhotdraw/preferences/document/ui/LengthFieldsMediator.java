/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.preferences.document.ui;

import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

/// Manages the fields showing the lengths of a document (spacing, sizes of
/// quadrats...), and the combo box which chooses their unit.
///
/// This is the second unit of the drawing preferences. The first one (see
/// [jsesh.ui.widgets.units.UnitMediator]) is the unit of the document itself,
/// the height of a quadrat, in absolute units. The lengths are *relative* to
/// it: they are shown in percents of it by default, which is also how they are
/// stored, but the user may prefer an absolute unit (see
/// [DocumentLengthUnit]). The percents are the reference: changing the unit of
/// the *document* keeps them, and rescales the lengths shown in an absolute
/// unit.
public class LengthFieldsMediator {

	/// The height of a quadrat in a new document, in points.
	private static final double DEFAULT_DOCUMENT_UNIT = 18;

	private DocumentLengthUnit currentUnit = DocumentLengthUnit.PERCENT;
	private double documentUnit = DEFAULT_DOCUMENT_UNIT;
	private JComboBox<DocumentLengthUnit> managedCombobox;
	private final List<JFormattedTextField> fields = new ArrayList<>();

	/// Creates a field for a length: at most two decimals are shown, and only if
	/// they are needed.
	///
	/// The displayed text is rounded, but the value is not, unless the user
	/// edits the text: tabbing through a field leaves its value untouched.
	///
	/// @return a new field, with [Double] values.
	public static JFormattedTextField createField() {
		return new JFormattedTextField(new ExactNumberFormatter());
	}

	/// Adds a field to the managed fields.
	///
	/// @param field a field created by [#createField()].
	public void managedTextField(JFormattedTextField field) {
		fields.add(field);
	}

	/// Attaches this mediator to a combo box, for two ways linking.
	///
	/// @param unitField the combo box to fill with the units.
	public void attachToComboBox(JComboBox<DocumentLengthUnit> unitField) {
		unitField.setModel(new DefaultComboBoxModel<>(DocumentLengthUnit.values().toArray(new DocumentLengthUnit[0])));
		managedCombobox = unitField;
		managedCombobox.setSelectedItem(currentUnit);
		unitField.addItemListener(this::updatedCombobox);
	}

	private void updatedCombobox(ItemEvent e) {
		if (managedCombobox.getSelectedItem() != null && managedCombobox.getSelectedItem() != currentUnit) {
			setCurrentUnit((DocumentLengthUnit) managedCombobox.getSelectedItem());
		}
	}

	/// @return the unit in which the lengths are shown.
	public DocumentLengthUnit getCurrentUnit() {
		return currentUnit;
	}

	/// Chooses the unit in which the lengths are shown, and converts the
	/// fields.
	///
	/// @param newUnit the new unit.
	public void setCurrentUnit(DocumentLengthUnit newUnit) {
		DocumentLengthUnit oldUnit = currentUnit;
		if (oldUnit == newUnit) {
			return;
		}
		for (JFormattedTextField field : fields) {
			if (committedValue(field) instanceof Number n) {
				double percent = oldUnit.toPercent(n.doubleValue(), documentUnit);
				field.setValue(newUnit.fromPercent(percent, documentUnit));
			}
		}
		currentUnit = newUnit;
		if (managedCombobox != null && managedCombobox.getSelectedItem() != newUnit) {
			managedCombobox.setSelectedItem(newUnit);
		}
	}

	/// Goes back to percents, without converting the fields, for a new
	/// document unit. To use before filling the fields with [#setPercent].
	///
	/// @param newDocumentUnit the unit of the document, in points.
	public void reset(double newDocumentUnit) {
		currentUnit = DocumentLengthUnit.PERCENT;
		documentUnit = newDocumentUnit;
		if (managedCombobox != null && managedCombobox.getSelectedItem() != currentUnit) {
			managedCombobox.setSelectedItem(currentUnit);
		}
	}

	/// To call when the user changes the unit of the document (the height of a
	/// quadrat). The lengths keep their value in percents, so those which are
	/// shown in an absolute unit are rescaled.
	///
	/// @param newDocumentUnit the new unit of the document, in points. An
	/// invalid (not positive) value is ignored.
	public void documentUnitChanged(double newDocumentUnit) {
		if (!(newDocumentUnit > 0) || Math.abs(newDocumentUnit - documentUnit) <= documentUnit * 1e-9) {
			return;
		}
		if (!currentUnit.isPercent()) {
			for (JFormattedTextField field : fields) {
				if (committedValue(field) instanceof Number n) {
					double percent = currentUnit.toPercent(n.doubleValue(), documentUnit);
					field.setValue(currentUnit.fromPercent(percent, newDocumentUnit));
				}
			}
		}
		documentUnit = newDocumentUnit;
	}

	/// Shows a length in a field, in the current unit.
	///
	/// @param field a managed field.
	/// @param percent the length, in percents of the unit of the document.
	public void setPercent(JFormattedTextField field, double percent) {
		field.setValue(currentUnit.fromPercent(percent, documentUnit));
	}

	/// @param field a managed field.
	/// @return the length of the field, in percents of the unit of the
	///         document, or NaN if the field has no valid value.
	public double getPercent(JFormattedTextField field) {
		if (committedValue(field) instanceof Number n) {
			return currentUnit.toPercent(n.doubleValue(), documentUnit);
		}
		return Double.NaN;
	}

	/// The value of a field, taking into account a text the user typed and
	/// didn't validate yet.
	private static Object committedValue(JFormattedTextField field) {
		try {
			field.commitEdit();
		} catch (ParseException e) {
			// Invalid text: we keep the last valid value.
		}
		return field.getValue();
	}

	/// A formatter showing at most two decimals, which doesn't lose the other
	/// ones when the text has not been edited.
	private static final class ExactNumberFormatter extends NumberFormatter {

		private static final long serialVersionUID = 1L;

		private transient Double lastValue;
		private transient String lastText;

		ExactNumberFormatter() {
			super(createFormat());
			setValueClass(Double.class);
		}

		private static NumberFormat createFormat() {
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(2);
			format.setGroupingUsed(false);
			return format;
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			String text = super.valueToString(value);
			if (value instanceof Number n) {
				lastValue = n.doubleValue();
				lastText = text;
			}
			return text;
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			if (lastText != null && lastText.equals(text)) {
				return lastValue;
			}
			return super.stringToValue(text);
		}
	}
}
