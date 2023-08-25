/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.export.rtf;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.DefaultComboBoxModel;

import jsesh.graphics.export.generic.ExportOptionPanel;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.swing.units.LengthUnit;
import jsesh.swing.units.UnitMediator;
import jsesh.swing.utils.FileButtonMapper;

/**
 * Presenter for the GUI interface for RTF export to files.
 */
public class RTFExporterPresenter {

    private File file;
    private RTFExportPreferences rtfPreferences;

    /**
     * @param file
     * @param preferences
     */
    public RTFExporterPresenter(File file, RTFExportPreferences preferences) {
        this.file = file;
        rtfPreferences = preferences;
    }

    /**
     * @param drawingSpecifications
     * @param model
     */
    public void exportModel(DrawingSpecification drawingSpecifications,
            TopItemList model) {

        RTFExporter exporter = new RTFExporter();
        exporter.setDrawingSpecifications(drawingSpecifications);
        exporter.setRtfPreferences(rtfPreferences);
        // exporter.setViewBuilder(new ViewBuilder()); (REMOVE ??)
        try {
            OutputStream out = new FileOutputStream(file);
            exporter.ExportModelTo(model, out);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * @return
     */
    public File getFile() {
        return file;
    }

    
    public ExportOptionPanel getOptionPanel(Component parent, String title) {
        return new RTFExportOptionPanel(parent, title);
    }

    private class RTFExportOptionPanel extends ExportOptionPanel {

        JRTFFileExportPreferences form;
        UnitMediator unitMediator= new UnitMediator();

        /**
         * @param parent
         * @param title
         */
        public RTFExportOptionPanel(Component parent, String title) {
            super(parent, title);
            form = new JRTFFileExportPreferences();

            form.getFileField().setValue(file);

            new FileButtonMapper(form.getBrowseButton(), form.getFileField());

            unitMediator.attachToComboBox(form.getUnitCB());
            unitMediator.managedTextField(form.getCadratHeightField());
            
            form.getCadratHeightField().setValue(rtfPreferences.getCadratHeight());


            form.getExportModeCB().setModel(new DefaultComboBoxModel(RTFExportGranularity.GRANULARITIES));
            form.getExportModeCB().setSelectedItem(RTFExportGranularity.GROUPED_CADRATS);
            add(form);
        }

        /* (non-Javadoc)
		 * @see jsesh.graphics.export.ExportOptionPanel#setOptions()
         */
        @Override
        public void setOptions() {
            file = (File) form.getFileField().getValue();
            rtfPreferences.setCadratHeight(getCadratHeight());
            rtfPreferences.setExportGranularity((RTFExportGranularity) form.getExportModeCB().getSelectedItem());
            rtfPreferences.setRespectOriginalTextLayout(false); // Normally false. If we want better handling, we will use something else than RTF.
        }

        /**
         * @return
         */
        private int getCadratHeight() {
            return (int) (((Number) form.getCadratHeightField().getValue()).doubleValue() * ((LengthUnit) form.getUnitCB().getSelectedItem()).getPointsValue());
        }
    }

    public RTFExportPreferences getRtfPreferences() {
        return rtfPreferences;
    }

    public void setRtfPreferences(RTFExportPreferences rtfPreferences) {
        this.rtfPreferences = rtfPreferences;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
