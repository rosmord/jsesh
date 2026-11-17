/*
 * Created on 17 oct. 2004
 *
 */
package jsesh.deprecated;

import java.util.Properties;

import java.io.File;
import java.io.FileNotFoundException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jsesh.graphicExport.ExportFileType;
import jsesh.graphics.generic.BaseGraphics2D;
import jsesh.graphics.generic.BaseGraphics2DException;
import jsesh.mdcDisplayer.draw.ViewPrinter;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;


/**
 * System previously used to export pages.
 * has been replaced by SelectionExporter.
 * @author Serge Rosmorduc
 * @deprecated
 */
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
						Messages.getString("PagedExporter.File") //$NON-NLS-1$
							+ outputFile.getName()
							+ Messages.getString("PagedExporter.exists.continue"), //$NON-NLS-1$
						Messages.getString("PagedExporter.file.exists"), //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (returnval == JOptionPane.CANCEL_OPTION)
					return;
			}

			BaseGraphics2D g = getGraphics(outputFile);

			g.setProperties(properties);

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
			g.scale(scale,scale);
			for (int i = 0; i < printer.getNumberOfPages(); i++)
			{
			    
				try
				{
					g.openPage(dimension, Messages.getString("PagedExporter.page") + (i + 1));										 //$NON-NLS-1$
					printer.print(g, pageFormat, i);					
				} catch (PrinterException e2)
				{
					e2.printStackTrace();
				}
				catch (BaseGraphics2DException e1)
				{
					e1.printStackTrace();
				} finally {
					try {
						g.closePage();
					} catch (BaseGraphics2DException e3) {
						e3.printStackTrace();
					}
				}
				
			}
			g.dispose();
		} catch (HeadlessException e1)
		{			
			e1.printStackTrace();
		} catch (FileNotFoundException e1)
		{
			JOptionPane.showMessageDialog(
				parent,
				Messages.getString("PagedExporter.cant.open.file"), //$NON-NLS-1$
				Messages.getString("PagedExporter.error"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
		}		
		catch (OutOfMemoryError err) {
			JOptionPane.showMessageDialog(
					parent,
					Messages.getString("PagedExporter.out.of.memory"), //$NON-NLS-1$
					Messages.getString("PagedExporter.error"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		}

	}

	
	/**
     * @return an object encapsulating the characteristics and parameters of the target format (like JPEG compression, etc...)
     */
    public abstract ExportFileType getExportFileType() ;

    abstract protected BaseGraphics2D getGraphics(File file)
		throws FileNotFoundException;


	abstract protected String getOptionsTitle();
	
	abstract protected Dimension getDimension();

	/**
	 * @return the scale.
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