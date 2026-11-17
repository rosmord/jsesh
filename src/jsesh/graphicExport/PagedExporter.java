/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphicExport;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jsesh.mdcDisplayer.draw.ViewPrinter;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.MultiPageDocument;
import org.freehep.util.export.ExportFileType;

abstract public class PagedExporter extends AbstractAction
{
	protected Component parent;
	protected HieroglyphicTextModel data;
	//protected PageFormat pageFormat;
	protected Properties properties;
	
	double scale;

	public PagedExporter(HieroglyphicTextModel data, Component parent, String name)
	{
		super(name);
		this.data = data;
		this.parent = parent;
		//pageFormat= new PageFormat();
		properties= new Properties();
		
		scale= 1.0;
	}

	public void actionPerformed(ActionEvent e)
	{

		try
		{
			JFileChooser chooser = new JFileChooser();

			ExportFileType export = getExportFileType();

	
			JPanel panel = export.createOptionPanel(properties);

			int rc =
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					getOptionsTitle(),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (rc == JOptionPane.OK_OPTION)
			{
				export.applyChangedOptions(panel, properties);
			} else
			{
				return;
			}
			
			
			chooser.setFileFilter(export.getFileFilter());

			int returnval = chooser.showSaveDialog(parent);

			if (returnval != JFileChooser.APPROVE_OPTION)
			{
				return;
			}

			File outputFile = chooser.getSelectedFile();
			
			if (! export.fileHasValidExtension(outputFile)) {
			    export.adjustFilename(outputFile,properties);
			}

			if (outputFile.exists())
			{
				returnval =
					JOptionPane.showConfirmDialog(
						parent,
						"File "
							+ outputFile.getName()
							+ " exists. Do you want to continue ?",
						"File exists",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (returnval == JOptionPane.CANCEL_OPTION)
					return;
			}

			VectorGraphics g = getVectorGraphics(outputFile);
			MultiPageDocument pager = getPager();

			pager.setMultiPage(true);

			g.setProperties(properties);



			g.startExport();

			Dimension d= getDimension();

			PageFormat pageFormat= new PageFormat();
			Paper p= new Paper();
			p.setSize(d.getWidth(), d.getHeight());
			p.setImageableArea(0,0,d.getWidth(), d.getHeight());
			pageFormat.setPaper(p);
			
			ViewPrinter printer = new ViewPrinter();
			printer.setModel(data.getModel());
			printer.setPageformat(pageFormat);
			    
			Dimension dimension= new Dimension((int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableWidth());
			//dimension= d;
			
			for (int i = 0; i < printer.getNumberOfPages(); i++)
			{
				try
				{
					pager.openPage(dimension, "page " + (i + 1));
					Graphics2D tmpg = (Graphics2D) g.create();
					tmpg.scale(scale,scale);
					printer.print(tmpg, pageFormat, i);
					tmpg.dispose();
				} catch (PrinterException e2)
				{
					e2.printStackTrace();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				} finally {
					try {
						pager.closePage();
					} catch (IOException e3) {
						e3.printStackTrace();
					}
				}
				
			}
			g.endExport();
			g.dispose();
		} catch (HeadlessException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1)
		{
			JOptionPane.showMessageDialog(
				parent,
				"Can't open file",
				"Error",
				JOptionPane.ERROR_MESSAGE);
		} catch (OutOfMemoryError err) {
			JOptionPane.showMessageDialog(
					parent,
					"Out of memory. Try starting java with option -Xmx128m",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	abstract protected ExportFileType getExportFileType();

	abstract protected VectorGraphics getVectorGraphics(File file)
		throws FileNotFoundException;

	abstract protected MultiPageDocument getPager();

	abstract protected String getOptionsTitle();
	
	abstract protected Dimension getDimension();

	/**
	 * @return
	 */
	public double getScale()
	{
		return scale;
	}

	/**
	 * @param d
	 */
	public void setScale(double d)
	{
		scale = d;
	}

    /**
     * @return Returns the properties.
     */
    public Properties getProperties() {
        return properties;
    }
    /**
     * @param properties The properties to set.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}