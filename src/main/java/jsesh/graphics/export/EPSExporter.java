/*
 * Created on 4 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.graphics.export;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.qenherkhopeshef.graphics.eps.EncapsulatedPostscriptGraphics2D;


/**
 * Expert able to export the selection to an EPS file.
 * 
 * A bit simplistic right now. Should be improved.
 * 
 * @author S. Rosmorduc
 * 
 */
public class EPSExporter implements
		BaseGraphics2DFactory {

	

	private File exportFile;

	private Component frame;
	
	private Dimension2D scaledDimension;

	private String sourceMdC;

	// private MDCView view;
	

	public EPSExporter() {
		exportFile= new File(".");
		frame= null;
	}

	public void export(ExportData exportData) {
		try {
			sourceMdC= exportData.getExportedMdC();
			SelectionExporter selectionExporter = new SelectionExporter(
					exportData, this);
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
				return n.endsWith(".eps") || n.endsWith(".EPS")
						|| f.isDirectory();
			}

			public String getDescription() {
				return "EPS files";
			}

		});
		if (exportFile.isDirectory())
			chooser.setSelectedFile(new File(exportFile, "unnamed.eps"));
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
	 * @see jsesh.editorSoftware.PagedExporter#getOptionsTitle()
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

	public void setDimension(Dimension2D scaledDimensions) {
		this.scaledDimension= scaledDimensions;
	}
	
	public Graphics2D buildGraphics()
			throws IOException {
		return new EncapsulatedPostscriptGraphics2D(exportFile, scaledDimension, sourceMdC);
	}
	
	public void writeGraphics() throws IOException {
		// NO-OP. the file is written as it is created.
	}
}