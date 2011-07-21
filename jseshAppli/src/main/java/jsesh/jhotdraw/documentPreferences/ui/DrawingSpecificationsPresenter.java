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
package jsesh.jhotdraw.documentPreferences.ui;

import java.awt.Component;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

import jsesh.jhotdraw.Messages;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.ShadingStyle;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;

/**
 * Presenter for drawing preferences.
 * <p> Choice to make at some point: use a dialog or a frame. With a frame, we would
 * need some kind of listener.
 * The DrawingSpecification class should probably be much much much simpler.
 * <p> For now, we stick with a modal dialog.
 * @author rosmord
 * 
 */
public class DrawingSpecificationsPresenter {

	JDrawingSpecificationEditor form;

	/**
	 * @param string
	 */
	public DrawingSpecificationsPresenter() {
		form = new JDrawingSpecificationEditor();
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
		UnitMaintainter.linkUnitsToValueField(form.getUnitField(),
				form.getSmallFontBodyLimitField());
		

	}

	/**
	 * Update the given drawing specifications from the dialog.
	 * 
	 * @param drawingSpecifications
	 */
	public void updatePreferences(		
			DrawingSpecificationsImplementation drawingSpecifications) {
		drawingSpecifications.setCartoucheLineWidth(getLength(form
				.getCartoucheLineWidthField()));
		drawingSpecifications
				.setColumnSkip(getLength(form.getColumnSkipField()));
		drawingSpecifications.setLineSkip(getLength(form.getLineSkipField()));
		drawingSpecifications.setMaxCadratHeight(getLength(form
				.getMaxCadratHeightField()));
		drawingSpecifications.setMaxCadratWidth(getLength(form
				.getMaxCadratWidthField()));
		drawingSpecifications.setStandardSignHeight(getLength(form
				.getNormalSignHeightField()));
		double limit = ((Double) form.getSmallFontBodyLimitField().getValue())
				.doubleValue();
		drawingSpecifications.setSmallBodyScaleLimit(limit);
		if (form.getUseLinesForShadingCheckBox().isSelected()) {
			drawingSpecifications.setShadingStyle(ShadingStyle.LINE_HATCHING);
		} else {
			drawingSpecifications.setShadingStyle(ShadingStyle.GRAY_SHADING);
		}
	}

	private float getLength(JFormattedTextField field) {
		float pointValue = (float) (((Number) field.getValue()).doubleValue() * ((LengthUnit) form
				.getUnitField().getSelectedItem()).getPointsValue());
		return pointValue;
	}

	/**
	 * Copy the preference drawing implementations in the dialog.
	 * 
	 * @param drawingSpecification
	 */
	public void loadPreferences(DrawingSpecification drawingSpecification) {
		DrawingSpecification drawingSpecifications = drawingSpecification
				.copy();
		form.getCartoucheLineWidthField().setValue(
				new Double(drawingSpecifications.getCartoucheLineWidth()));
		form.getColumnSkipField().setValue(
				new Double(drawingSpecifications.getColumnSkip()));
		form.getLineSkipField().setValue(
				new Double(drawingSpecifications.getLineSkip()));
		form.getMaxCadratHeightField().setValue(
				new Double(drawingSpecifications.getMaxCadratHeight()));
		form.getMaxCadratWidthField().setValue(
				new Double(drawingSpecifications.getMaxCadratWidth()));
		form.getNormalSignHeightField()
				.setValue(
						new Double((int) drawingSpecifications
								.getStandardSignHeight()));
		form.getSmallFontBodyLimitField().setValue(
				new Double(drawingSpecifications.getSmallBodyScaleLimit()));
		form.getUseLinesForShadingCheckBox().setSelected(
				drawingSpecifications.getShadingStyle().equals(
						ShadingStyle.LINE_HATCHING));
	}


	public int  showDialog(Component parent) {
		form.addFocus();
		return JOptionPane.showOptionDialog(parent, form.getPanel(), Messages.getString("drawingPrefs.title"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, null, null);
	}
	

	public static void main(String[] args) {
		DrawingSpecificationsPresenter presenter = new DrawingSpecificationsPresenter();
		presenter.loadPreferences(new DrawingSpecificationsImplementation());
		presenter.showDialog(null);
		DrawingSpecificationsImplementation d= new DrawingSpecificationsImplementation();
		presenter.updatePreferences(d);
		System.out.println(d.getMaxCadratWidth());
	}
}
