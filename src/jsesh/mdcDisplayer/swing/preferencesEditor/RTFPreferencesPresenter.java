/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.preferencesEditor;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import jsesh.graphicExport.RTFExportPreferences;
import jsesh.graphicExport.RTFExportPreferences.RTFExportGranularity;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;

/**
 * @author rosmord
 *
 */
public class RTFPreferencesPresenter extends FormPresenter {

	private RTFPreferencesForm form;
	
	private RTFExportPreferences preferences;
	
	/**
	 * @param string
	 * @param i
	 */
	public RTFPreferencesPresenter(String title, RTFExportPreferences preferences) {
		super(title);
		form= new RTFPreferencesForm();
		this.preferences= preferences;
		
		// Fill with default values
		form.exportModeField.setModel(new DefaultComboBoxModel(RTFExportPreferences.exportModes));
		LengthUnit.attachToCombobox(form.unitField, LengthUnit.POINT);
		
		form.cadratHeightField.setValue(new Double(preferences.getCadratHeight()));
		form.exportModeField.setSelectedItem(RTFExportPreferences.RTFExportGranularity.ONE_PICTURE_PER_CADRAT);
		form.exportModeField.setModel(new DefaultComboBoxModel(RTFExportGranularity.GRANULARITIES));

		UnitMaintainter.linkUnitsToValueField(form.unitField, form.cadratHeightField);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#getPanel()
	 */
	public JComponent getPanel() {
		return form;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#updatePreferences()
	 */
	public void updatePreferences(PreferencesFacade facade) {
		preferences.setCadratHeight(getCadratHeightInPoints());
		preferences.setExportGranularity(getExportGranularity());
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#loadPreferences(jsesh.mdcDisplayer.swing.preferencesEditor.PreferencesFacade)
	 */
	public void loadPreferences(PreferencesFacade facade) {
		form.unitField.setSelectedItem(facade.getPreferedUnit());
		form.exportModeField.setSelectedItem(preferences.getExportGranularity());
		double heightInPoints= preferences.getCadratHeight();
		setCadratHeightInPoints(heightInPoints);
	}

	private int getCadratHeightInPoints() {
		LengthUnit currentUnit= ((LengthUnit)form.unitField.getSelectedItem());
		int pointValue= (int)(((Number)form.cadratHeightField.getValue()).doubleValue() * currentUnit.getPointsValue());
		return pointValue;		
	}
	
	private void setCadratHeightInPoints(double heightInPoints) {
		LengthUnit currentUnit= ((LengthUnit)form.unitField.getSelectedItem());
		form.cadratHeightField.setValue(new Double(heightInPoints/currentUnit.getPointsValue()));
		
	}
	
	
	private RTFExportPreferences.RTFExportGranularity getExportGranularity() {
		RTFExportGranularity result= (RTFExportGranularity) form.exportModeField.getSelectedItem();
		if (result== null)
			result= RTFExportGranularity.ONE_PICTURE_PER_CADRAT;
		return result;
	}
}
