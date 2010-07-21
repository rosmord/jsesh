/*
 * Created on 4 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.graphics.export;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;


/**
 * Expert able to export the selection to an Mac Pict file.
 * 
 * @author S. Rosmorduc
 * 
 */
public class MacPictExporter implements
		BaseGraphics2DFactory {

	

	private File exportFile;

	private Component frame;
	
	//private Dimension scaledDimension;

	private MacPictGraphics2D currentGraphics;
	/**
	 * List of possible suffixes, in lowercase.
	 */
	private String [] suffixes= {".pct", ".pict"};

	private String description= "Mac Pict files";
	
	private String defaultFileName= "unnamed.pct";
	
	public MacPictExporter() {
		exportFile= new File(".");
		frame= null;
	}

	public void export(ExportData exportData) {
		try {
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
				String n = f.getName().toLowerCase();
				boolean hasCorrectSuffix= false;
				for (int i=0; ! hasCorrectSuffix && i < suffixes.length; i++) {
					hasCorrectSuffix= n.endsWith(suffixes[i]); 
				}
				return hasCorrectSuffix
						|| f.isDirectory();
			}

			public String getDescription() {
				return description;
			}

		});
		
		if (exportFile.isDirectory())
			chooser.setSelectedFile(new File(exportFile, defaultFileName));
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

	public void setDimension(java.awt.geom.Dimension2D scaledDimensions) {
		//this.scaledDimension= scaledDimensions;
	}
	
	public Graphics2D buildGraphics()
			throws IOException {
		currentGraphics=  new MacPictGraphics2D(); 
		return currentGraphics;
	}
	
	public void writeGraphics() throws IOException {
		OutputStream outputStream= new BufferedOutputStream(new FileOutputStream(this.exportFile));
		currentGraphics.writeToStream(outputStream);
	}
}