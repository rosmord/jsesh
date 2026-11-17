/*
 * Created on 4 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.graphicExport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jsesh.mdcDisplayer.draw.DrawingSpecifications;
import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.draw.MDCEditorKit;
import jsesh.mdcDisplayer.draw.SimpleViewBuilder;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;
import jsesh.wmfexport.WMFGraphics2D;

import org.freehep.graphics2d.VectorGraphics;

/**
 * Expert able to export the selection to an WMF file.
 * 
 * @author S. Rosmorduc
 *  
 */
public class WMFExporter extends AbstractAction {
	private MultiFileGraphics g;

	private HieroglyphicTextModel data;
	
	private double scale;

	private String type = "wmf";

	private File exportFile;

	private DrawingSpecifications drawingSpecifications;

	private CaretProvider caretProvider;
	
	private Component frame;
	
	//private MDCView view;

	/**
	 * @param data
	 * @param parent
	 * @param name
	 */
	public WMFExporter(HieroglyphicTextModel data, Component frame, CaretProvider caretProvider, String frameName, File baseDir) {
		super(frameName);
		this.data = data;
		this.scale = 1.0;
		this.frame= frame;
		this.caretProvider= caretProvider;
		// this.view = null;
		drawingSpecifications = MDCEditorKit.getBasicMDCEditorKit()
				.getDrawingSpecifications().copy();
		//drawingSpecifications.setTopMargin(drawingSpecifications.get);
		drawingSpecifications.setLeftMargin(0f);
		exportFile= baseDir;
	}

	private VectorGraphics getVectorGraphics(File file)
			throws IOException {
		WMFGraphics2D result= null;
		
		
	
		return result;
	}

	/**
	 * @return
	 */
	private Dimension getScaledDimensions(MDCView view) {
		double w = view.getWidth() * scale;
		double h = view.getHeight() * scale;
		return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getOptionsTitle()
	 */
	protected String getOptionsTitle() {
		return "type".toUpperCase() + " options";
	}

	public void actionPerformed(ActionEvent e) {
		try {
			
			JFileChooser chooser = new JFileChooser();

			Properties properties = new Properties();


			chooser.setFileFilter(new FileFilter () {

				public boolean accept(File f) {
					String n= f.getName();
					return n.endsWith(".wmf") || n.endsWith(".WMF") || f.isDirectory();
				}

				public String getDescription() {
					return "WMF files";
				}
				
			});
			if (exportFile.isDirectory())
			    chooser.setSelectedFile(new File(exportFile, "unnamed.wmf"));
			else
			    chooser.setSelectedFile(exportFile);
			int returnval = chooser.showSaveDialog(frame);

			if (returnval != JFileChooser.APPROVE_OPTION) {
				return;
			}

			exportFile = chooser.getSelectedFile();

			if (exportFile.exists()) {
				returnval = JOptionPane.showConfirmDialog(frame, "File "
						+ exportFile.getName()
						+ " exists. Do you want to continue ?", "File exists",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (returnval == JOptionPane.CANCEL_OPTION)
					return;
			}

			// Build the view :
			SimpleViewBuilder builder = new SimpleViewBuilder();
            builder.setDrawingSpecifications(drawingSpecifications);
            MDCCaret caret= caretProvider.getCaret();
            MDCView view = builder.buildView(data.getModel(), caret.getMin(), caret.getMax());
            
            // Build the wmf file and initialize it :
            VectorGraphics g = new WMFGraphics2D(exportFile, getScaledDimensions(view));
            g.setProperties(properties);
			g.setBackground(Color.WHITE);
			
            // Prepare to draw.
            ViewDrawer drawer = new ViewDrawer();
			drawer.setShadeAfter(false);
			
			// draw !
			g.startExport();
			drawer.draw(g, view, drawingSpecifications);
			g.endExport();
			g.dispose();
			
		} catch (HeadlessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Can't open file", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	
    /**
     * @return Returns the exportFile.
     */
    public File getExportFile() {
        return exportFile;
    }
    
    /**
     * @param exportFile The exportFile to set.
     */
    public void setExportFile(File exportFile) {
        this.exportFile = exportFile;
    }
}