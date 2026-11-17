package jsesh.graphicExport;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jsesh.graphics.generic.BaseGraphics2D;
import jsesh.graphics.svg.SVGGraphics2D;

/**
 * Quick hack because I really need to export SVG NOW.
 * I should generalize WMF Exporter to factorize common code.
 * @author rosmord
 *
 */
public class SVGExporter implements BaseGraphics2DFactory {



	private File exportFile;

	private Component frame;
	
	private Dimension scaledDimension;


	public SVGExporter() {
		exportFile= new File(".");
		frame= null;
	}

	public void export(ExportData exportData) {
		try {
			SelectionExporter selectionExporter = new SelectionExporter(
					exportData, this);
			selectionExporter.setClearBeforeDrawing(false);
			selectionExporter.exportSelection();
		} catch (HeadlessException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Can't open file", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public int askUser() {
		int returnval = JOptionPane.OK_OPTION;

		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new FileFilter() {

			public boolean accept(File f) {
				String n = f.getName();
				return n.endsWith(".svg") || n.endsWith(".svg")
						|| f.isDirectory();
			}

			public String getDescription() {
				return "SVG files";
			}

		});
		
		if (exportFile.isDirectory())
			chooser.setSelectedFile(new File(exportFile, "unnamed.svg"));
		else
			chooser.setSelectedFile(exportFile);
		returnval = chooser.showSaveDialog(frame);

		if (returnval == JFileChooser.APPROVE_OPTION) {

			exportFile = chooser.getSelectedFile();

			if (exportFile.exists()) {
				returnval = JOptionPane.showConfirmDialog(frame, "File "
						+ exportFile.getName()
						+ " exists. Do you want to continue ?", "File exists",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
			}
		}
		return returnval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getOptionsTitle()
	 */
	protected String getOptionsTitle() {
		return "type".toUpperCase() + " options";
	}


	/**
	 * @return Returns the exportFile.
	 */
	public File getExportFile() {
		return exportFile;
	}

	/**
	 * @param exportFile
	 *            The exportFile to set.
	 */
	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}

	public void setDimension(Dimension scaledDimensions) {
		this.scaledDimension= scaledDimensions;
	}
	
	public BaseGraphics2D buildGraphics()
			throws IOException {
		return new SVGGraphics2D(exportFile, scaledDimension);
	}
}
