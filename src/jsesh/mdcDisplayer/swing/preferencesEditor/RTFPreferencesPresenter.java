/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.preferencesEditor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
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
	
	private LengthUnit currentUnit;
	
	private RTFExportPreferences preferences;
	
	/**
	 * @param string
	 * @param i
	 */
	public RTFPreferencesPresenter(String title, RTFExportPreferences preferences) {
		super(title);
		form= new RTFPreferencesForm();
		this.preferences= preferences;
		currentUnit= LengthUnit.POINT;
		
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
		int pointValue= (int)(((Number)form.cadratHeightField.getValue()).doubleValue() * currentUnit.getPointsValue());
		return pointValue;		
	}
	
	private void setCadratHeightInPoints(double heightInPoints) {
		form.cadratHeightField.setValue(new Double(heightInPoints/currentUnit.getPointsValue()));
		
	}
	
	private void unitChanged() {		
		double height= getCadratHeightInPoints();
		currentUnit= (LengthUnit) form.unitField.getSelectedItem();
		setCadratHeightInPoints(height);
	}
	
	private RTFExportPreferences.RTFExportGranularity getExportGranularity() {
		RTFExportGranularity result= (RTFExportGranularity) form.exportModeField.getSelectedItem();
		if (result== null)
			result= RTFExportGranularity.ONE_PICTURE_PER_CADRAT;
		return result;
	}
	/**
	 * Callback to update the displayed value of the height field when the unit is changed.
	 */
	private class UnitMaintainterOLD implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (e.getItem() != null) {
					unitChanged();
				}
			}
		}
	}
}
