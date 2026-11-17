/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.graphicExport;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;

import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.MultiPageDocument;
import org.freehep.graphicsio.exportchooser.ImageExportFileType;
import org.freehep.util.export.ExportFileType;

/**
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
public class JPGExporter extends PagedExporter
{
	
	private MultiFileGraphics g;
	
	/**
	 * @param data
	 * @param parent
	 * @param name
	 */
	public JPGExporter(HieroglyphicTextModel data, Component parent, String name)
	{
		super(data, parent, name);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getExportFileType()
	 */
	protected ExportFileType getExportFileType()
	{
		return ImageExportFileType.getInstance("jpg");
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getVectorGraphics(java.io.File)
	 */
	protected VectorGraphics getVectorGraphics(File file) throws FileNotFoundException
	{
		File directory= file.getParentFile();
		String base= file.getName();
		int extIndex= base.lastIndexOf('.');
		if (extIndex== -1)
			extIndex= base.length();
	
		g= MultiFileGraphics.getExportForJPG(directory,base.substring(0,extIndex),"jpg");
		return g;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getPager()
	 */
	protected MultiPageDocument getPager()
	{
		return g;
	}
	
	/* (non-Javadoc)
     * @see jsesh.graphicExport.PagedExporter#getDimension()
     */
    protected Dimension getDimension() {
        return new Dimension((int)scale*640, (int)scale*480);
    }

	
	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getOptionsTitle()
	 */
	protected String getOptionsTitle()
	{
		return "JPG options";
	}

}
