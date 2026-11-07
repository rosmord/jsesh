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
import java.text.ParseException;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;

import jsesh.document.DocumentPreferences;
import jsesh.jhotdraw.utils.JSimpleDialog;
import jsesh.model.LayoutSetting;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.resources.JSeshMessages;
import jsesh.ui.widgets.units.LengthUnit;
import jsesh.ui.widgets.units.UnitMediator;

/// Presenter for the drawing preferences of a document.
///
/// The style is edited through its [DocumentPreferences]: one unit (the height
/// of a quadrat), and the other lengths of the text relative to it. Each has
/// its own unit choice: the unit of the document is in absolute units
/// ([UnitMediator]), the other lengths are by default in percents of it, but
/// can be shown in an absolute unit ([LengthFieldsMediator]). They are saved
/// in percents in any case.
public class DrawingSpecificationsPresenter {

	JDrawingSpecificationEditor form;
	UnitMediator unitMediator = new UnitMediator();
	LengthFieldsMediator lengthsMediator = new LengthFieldsMediator();

	public DrawingSpecificationsPresenter() {
		form = new JDrawingSpecificationEditor();
		unitMediator.attachToComboBox(form.getLengthUnitField());
		unitMediator.managedTextField(form.getDocumentUnitField());
		unitMediator.managedTextField(form.getSmallFontBodyLimitField());
		lengthsMediator.attachToComboBox(form.getLengthsUnitField());
		for (JFormattedTextField field : form.getLengthFields().values()) {
			lengthsMediator.managedTextField(field);
		}
		// Lengths shown in an absolute unit follow the unit of the document.
		form.getDocumentUnitField().addPropertyChangeListener("value",
				e -> lengthsMediator.documentUnitChanged(documentUnitInPoints()));
	}

	private double documentUnitInPoints() {
		return unitMediator.getManagedFieldInPoints(form.getDocumentUnitField());
	}

	/// Copies a style in the dialog.
	///
	/// @param jseshStyle the style.
	public void loadPreferences(JSeshStyle jseshStyle) {
		DocumentPreferences prefs = DocumentPreferencesStyleConverter.toDocumentPreferences(jseshStyle);
		unitMediator.setCurrentUnit(LengthUnit.POINT);
		lengthsMediator.reset(prefs.unit());
		form.getDocumentUnitField().setValue(prefs.unit());
		form.getSmallFontBodyLimitField().setValue(prefs.smallBodyScaleLimit());
		for (Map.Entry<LayoutSetting, JFormattedTextField> e : form.getLengthFields().entrySet()) {
			lengthsMediator.setPercent(e.getValue(), prefs.percent(e.getKey()));
		}
		for (Map.Entry<LayoutSetting, JCheckBox> e : form.getFlagCheckBoxes().entrySet()) {
			e.getValue().setSelected(prefs.flag(e.getKey()));
		}
		form.getUseLinesForShadingCheckBox().setSelected(prefs.useLinesForShading());
	}

	/// Applies the preferences of the dialog to a style.
	///
	/// @param original the original style
	/// @return a modified style, with the new preferences.
	public JSeshStyle updatePreferences(JSeshStyle original) {
		DocumentPreferences prefs = DocumentPreferencesStyleConverter.toDocumentPreferences(original);
		// Validates what is typed in the unit first: the lengths are converted
		// through it.
		try {
			form.getDocumentUnitField().commitEdit();
		} catch (ParseException e) {
			// Invalid text: the last valid value is used.
		}
		double unit = documentUnitInPoints();
		lengthsMediator.documentUnitChanged(unit);
		if (unit > 0) {
			prefs = prefs.withUnit(unit);
		}
		double limit = unitMediator.getManagedFieldInPoints(form.getSmallFontBodyLimitField());
		if (limit >= 0) {
			prefs = prefs.withSmallBodyScaleLimit(limit);
		}
		for (Map.Entry<LayoutSetting, JFormattedTextField> e : form.getLengthFields().entrySet()) {
			double percent = lengthsMediator.getPercent(e.getValue());
			if (percent >= 0) {
				prefs = prefs.with(e.getKey(), percent);
			}
		}
		for (Map.Entry<LayoutSetting, JCheckBox> e : form.getFlagCheckBoxes().entrySet()) {
			prefs = prefs.with(e.getKey(), e.getValue().isSelected());
		}
		prefs = prefs.withUseLinesForShading(form.getUseLinesForShadingCheckBox().isSelected());
		return DocumentPreferencesStyleConverter.applyDocumentPreferences(prefs, original);
	}

	public int showDialog(Component parent) {
		JSimpleDialog dialog = new JSimpleDialog(parent, form.getPanel(),
				JSeshMessages.getString("drawingPrefs.title"));
		int result = dialog.show();
		dialog.dispose();
		return result;
	}
}
