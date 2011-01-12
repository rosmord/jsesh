/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.preferencesEditor;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.ShadingStyle;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;

/**
 * @author rosmord
 * 
 */
public class DrawingSpecificationsPresenter extends FormPresenter {


	JDrawingSpecification form;

	/**
	 * @param string
	 */
	public DrawingSpecificationsPresenter(String title) {
		super(title);
		form = new JDrawingSpecification();
		LengthUnit.attachToCombobox(form.getUnitField(), LengthUnit.POINT);

		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getCartoucheLineWidthField());
		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getColumnSkipField());
		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getLineSkipField());
		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getMaxCadratHeightField());
		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getMaxCadratWidthField());
		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getNormalSignHeightField());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.swing.preferencesEditor.FormPresenter#getPanel()
	 */
	public JComponent getPanel() {
		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.swing.preferencesEditor.FormPresenter#updatePreferences()
	 */
	public void updatePreferences(PreferencesFacade facade) {
		DrawingSpecification drawingSpecifications = facade
		.getDrawingSpecifications();
	
		drawingSpecifications.setCartoucheLineWidth(getLength(form.getCartoucheLineWidthField()));
		drawingSpecifications.setColumnSkip(getLength(form.getColumnSkipField()));
		drawingSpecifications.setLineSkip(getLength(form.getLineSkipField()));
		drawingSpecifications.setMaxCadratHeight(getLength(form.getMaxCadratHeightField()));
		drawingSpecifications.setMaxCadratWidth(getLength(form.getMaxCadratWidthField()));
		drawingSpecifications.setStandardSignHeight(getLength(form.getNormalSignHeightField()));
        double limit= ((Double)form.getSmallFontBodyLimitField().getValue()).doubleValue();
        drawingSpecifications.setSmallBodyScaleLimit(limit);
        if (form.getUseLinesForShadingCheckBox().isSelected()) {
        	drawingSpecifications.setShadingStyle(ShadingStyle.LINE_HATCHING);
        } else {
        	drawingSpecifications.setShadingStyle(ShadingStyle.GRAY_SHADING);
        }
	}

	private float getLength(JFormattedTextField field) {
		float pointValue= (float)(((Number)field.getValue()).doubleValue() * ((LengthUnit)form.getUnitField().getSelectedItem()).getPointsValue());
		return pointValue;		
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.swing.preferencesEditor.FormPresenter#loadPreferences(jsesh.swing.preferencesEditor.PreferencesFacade)
	 */
	public void loadPreferences(PreferencesFacade facade) {
		DrawingSpecification drawingSpecifications = facade
				.getDrawingSpecifications();
		form.getCartoucheLineWidthField().setValue(new Double(drawingSpecifications
				.getCartoucheLineWidth()));
		form.getColumnSkipField().setValue(new Double(drawingSpecifications
				.getColumnSkip()));
		form.getLineSkipField().setValue(new Double(drawingSpecifications
				.getLineSkip()));
		form.getMaxCadratHeightField().setValue(new Double(drawingSpecifications
				.getMaxCadratHeight()));
		form.getMaxCadratWidthField().setValue(new Double(drawingSpecifications.getMaxCadratWidth()));
		form.getNormalSignHeightField().setValue(new Double((int)drawingSpecifications.getStandardSignHeight()));
        form.getSmallFontBodyLimitField().setValue(new Double(drawingSpecifications.getSmallBodyScaleLimit()));
        form.getUseLinesForShadingCheckBox().setSelected(drawingSpecifications.getShadingStyle().equals(ShadingStyle.LINE_HATCHING));
	}

}
