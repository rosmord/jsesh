/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphicExport;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.DefaultComboBoxModel;

import jsesh.graphicExport.RTFExportPreferences.RTFExportGranularity;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.swingUtils.FileButtonMapper;

/**
 * Interface for RTF export to files.
 * 
 */
public class RTFExporterUI {
	File file;
	RTFExportPreferences rtfPreferences;

	/**
	 * @param file
	 * @param preferences
	 */
	public RTFExporterUI(File file, RTFExportPreferences preferences) {
		this.file = file;
		rtfPreferences = preferences;
	}

	/**
	 * @param drawingSpecifications
	 * @param model
	 */
	public void exportModel(DrawingSpecifications drawingSpecifications,
			TopItemList model) {
		
		RTFExporter exporter = new RTFExporter();
		
		exporter.setDrawingSpecifications(drawingSpecifications);
		exporter
				.setRtfPreferences(rtfPreferences);
		exporter.setViewBuilder(new SimpleViewBuilder());
		try {
			OutputStream out = new FileOutputStream(file);
			exporter.ExportModelTo(model, out);
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * @param currentOutputDirectory
	 * @param string
	 */
	public void setFile(File currentOutputDirectory, String fname) {
		this.file = new File(currentOutputDirectory, fname);
	}


	/**
	 * @return
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param frame
	 * @param string
	 * @return
	 */
	public ExportOptionPanel getOptionPanel(Component parent, String title) {
		return new RTFExportOptionPanel(parent,title);
	}
	
	private class RTFExportOptionPanel extends ExportOptionPanel {
		RTFFilePreferencesForm form;
		LengthUnit unit= LengthUnit.POINT;
		/**
		 * @param parent
		 * @param title
		 */
		public RTFExportOptionPanel(Component parent, String title) {
			super(parent, title);
			form= new RTFFilePreferencesForm();
            
			form.fileNameField.setValue(file);
			FileButtonMapper mapper= new FileButtonMapper(form.browse,form.fileNameField);
			
			LengthUnit.attachToCombobox(form.unitField, unit);
			
			form.cadratHeightField.setValue(unit.convert(rtfPreferences.getCadratHeight()));
			
			jsesh.mdcDisplayer.swing.units.UnitMaintainter.linkUnitsToValueField(form.unitField, form.cadratHeightField);
			
			form.exportModeField.setModel(new DefaultComboBoxModel(RTFExportGranularity.GRANULARITIES));
			form.exportModeField.setSelectedItem(RTFExportGranularity.GROUPED_CADRATS);
			add(form);
		}

		/* (non-Javadoc)
		 * @see jsesh.graphicExport.ExportOptionPanel#setOptions()
		 */
		public void setOptions() {
			file= (File) form.fileNameField.getValue();
			rtfPreferences.setCadratHeight(getCadratHeight());
			rtfPreferences.setExportGranularity((RTFExportGranularity) form.exportModeField.getSelectedItem());
		}

		/**
		 * @return
		 */
		private int getCadratHeight() {
			return (int)(((Double)form.cadratHeightField.getValue()).doubleValue() * ((LengthUnit)form.unitField.getSelectedItem()).getPointsValue());
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
