/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.preferencesEditor;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGraphicFormat;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;

/**
 * @author rosmord
 *
 */
public class RTFPreferencesPresenter extends FormPresenter {

    private JRTFPreferences form;
    private RTFExportPreferences preferences;

    /**
     * @param string
     * @param i
     */
    public RTFPreferencesPresenter(String title, RTFExportPreferences preferences) {
        super(title);
        form = new JRTFPreferences();
        this.preferences = preferences;

        // Fill with default values
        form.getExportModeField().setModel(new DefaultComboBoxModel(RTFExportPreferences.exportModes));
        LengthUnit.attachToCombobox(form.getUnitField(), LengthUnit.POINT);

        form.getCadratHeightField().setValue(new Double(preferences.getCadratHeight()));
        form.getExportModeField().setSelectedItem(RTFExportPreferences.RTFExportGranularity.ONE_PICTURE_PER_CADRAT);
        form.getExportModeField().setModel(new DefaultComboBoxModel(RTFExportGranularity.GRANULARITIES));

        UnitMaintainter.linkUnitsToValueField(form.getUnitField(), form.getCadratHeightField());

        DefaultComboBoxModel graphicComboBoxModel = new DefaultComboBoxModel(RTFExportPreferences.graphicFormatList);
        form.getGraphicFormatCB().setModel(graphicComboBoxModel);
        form.getGraphicFormatCB().setSelectedIndex(0);
    }

    /* (non-Javadoc)
     * @see jsesh.swing.preferencesEditor.FormPresenter#getPanel()
     */
    public JComponent getPanel() {
        return form;
    }

    /* (non-Javadoc)
     * @see jsesh.swing.preferencesEditor.FormPresenter#updatePreferences()
     */
    public void updatePreferences(PreferencesFacade facade) {
        preferences.setCadratHeight(getCadratHeightInPoints());
        preferences.setExportGranularity(getExportGranularity());
        preferences.setRespectOriginalTextLayout(getRespectLayoutMode());
        preferences.setExportGraphicFormat(getExportGraphicMode());
    }

    /* (non-Javadoc)
     * @see jsesh.swing.preferencesEditor.FormPresenter#loadPreferences(jsesh.swing.preferencesEditor.PreferencesFacade)
     */
    public void loadPreferences(PreferencesFacade facade) {
        form.getUnitField().setSelectedItem(facade.getPreferedUnit());
        form.getExportModeField().setSelectedItem(preferences.getExportGranularity());
        double heightInPoints = preferences.getCadratHeight();
        setCadratHeightInPoints(heightInPoints);
        form.getRespectLayoutModeField().setSelected(preferences.respectOriginalTextLayout());
        form.getGraphicFormatCB().setSelectedItem(preferences.getExportGraphicFormat());
    }

    private int getCadratHeightInPoints() {
        LengthUnit currentUnit = ((LengthUnit) form.getUnitField().getSelectedItem());
        int pointValue = (int) (((Number) form.getCadratHeightField().getValue()).doubleValue() * currentUnit.getPointsValue());
        return pointValue;
    }

    private RTFExportGraphicFormat getExportGraphicMode() {
        return (RTFExportGraphicFormat) form.getGraphicFormatCB().getSelectedItem();
    }

    private void setCadratHeightInPoints(double heightInPoints) {
        LengthUnit currentUnit = ((LengthUnit) form.getUnitField().getSelectedItem());
        form.getCadratHeightField().setValue(new Double(heightInPoints / currentUnit.getPointsValue()));

    }

    private RTFExportPreferences.RTFExportGranularity getExportGranularity() {
        RTFExportGranularity result = (RTFExportGranularity) form.getExportModeField().getSelectedItem();
        if (result == null) {
            result = RTFExportGranularity.ONE_PICTURE_PER_CADRAT;
        }
        return result;
    }

    private boolean getRespectLayoutMode() {
        return form.getRespectLayoutModeField().isSelected();
    }
}
