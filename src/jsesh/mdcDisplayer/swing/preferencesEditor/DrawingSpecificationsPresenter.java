/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.preferencesEditor;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;

/**
 * @author rosmord
 * 
 */
public class DrawingSpecificationsPresenter extends FormPresenter {


	DrawingSpecificationsForm form;

	/**
	 * @param string
	 */
	public DrawingSpecificationsPresenter(String title) {
		super(title);
		form = new DrawingSpecificationsForm();
		LengthUnit.attachToCombobox(form.unitField, LengthUnit.POINT);

		UnitMaintainter.linkUnitsToValueField(form.unitField,
				form.cartoucheLineWidthField);
		UnitMaintainter.linkUnitsToValueField(form.unitField,
				form.columnSkipField);
		UnitMaintainter.linkUnitsToValueField(form.unitField,
				form.lineSkipField);
		UnitMaintainter.linkUnitsToValueField(form.unitField,
				form.maxCadratHeightField);
		UnitMaintainter.linkUnitsToValueField(form.unitField,
				form.MaxCadratWidthField);
		UnitMaintainter.linkUnitsToValueField(form.unitField,
				form.normalSignHeightField);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#getPanel()
	 */
	public JComponent getPanel() {
		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#updatePreferences()
	 */
	public void updatePreferences(PreferencesFacade facade) {
		DrawingSpecifications drawingSpecifications = facade
		.getDrawingSpecifications();
	
		drawingSpecifications.setCartoucheLineWidth(getLength(form.cartoucheLineWidthField));
		drawingSpecifications.setColumnSkip(getLength(form.columnSkipField));
		drawingSpecifications.setLineSkip(getLength(form.lineSkipField));
		drawingSpecifications.setMaxCadratHeight(getLength(form.maxCadratHeightField));
		drawingSpecifications.setMaxCadratWidth(getLength(form.MaxCadratWidthField));
		drawingSpecifications.setStandardSignHeight(getLength(form.normalSignHeightField));
	}

	private float getLength(JFormattedTextField field) {
		float pointValue= (float)(((Number)field.getValue()).doubleValue() * ((LengthUnit)form.unitField.getSelectedItem()).getPointsValue());
		return pointValue;		
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#loadPreferences(jsesh.mdcDisplayer.swing.preferencesEditor.PreferencesFacade)
	 */
	public void loadPreferences(PreferencesFacade facade) {
		DrawingSpecifications drawingSpecifications = facade
				.getDrawingSpecifications();
		form.cartoucheLineWidthField.setValue(new Double(drawingSpecifications
				.getCartoucheLineWidth()));
		form.columnSkipField.setValue(new Double(drawingSpecifications
				.getColumnSkip()));
		form.lineSkipField.setValue(new Double(drawingSpecifications
				.getLineSkip()));
		form.maxCadratHeightField.setValue(new Double(drawingSpecifications
				.getMaxCadratHeight()));
		form.MaxCadratWidthField.setValue(new Double(drawingSpecifications.getMaxCadratWidth()));
		form.normalSignHeightField.setValue(new Double((int)drawingSpecifications.getStandardSignHeight()));
	}

}
