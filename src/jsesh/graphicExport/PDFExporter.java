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
import java.util.Properties;

import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.MultiPageDocument;
import org.freehep.graphicsio.PageConstants;
import org.freehep.graphicsio.pdf.PDFExportFileType;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.util.export.ExportFileType;

public class PDFExporter extends PagedExporter
{
    private static final String prefix="org.freehep.graphicsio.pdf.PDFGraphics2D."; 

	PDFGraphics2D g;

	public PDFExporter(HieroglyphicTextModel data, Component parent, String name)
	{
		super(data, parent, name);
		properties= PDFGraphics2D.getDefaultProperties();
		properties= new Properties();
		properties.setProperty(prefix+"EmbedFonts", "true");
		properties.setProperty(prefix+"EmbedFontsAs", "Type1");
		properties.setProperty(prefix+"PageSize", "A4");
	}

	public ExportFileType getExportFileType() {	    
		return new PDFExportFileType();
	}

	protected VectorGraphics getVectorGraphics(File fname) throws FileNotFoundException {
		g= new PDFGraphics2D(fname, getDimension());
		//g.setFont(ResourcesManager.getInstance().getTransliterationFont());
		//g.drawString("toto",0,0);
		return g;
	}

	/* (non-Javadoc)
     * @see jsesh.graphicExport.PagedExporter#getDimension()
     */
    protected Dimension getDimension() {
        properties.list(System.out);
        String format= properties.getProperty(prefix+ "PageSize");
        if (format== null)
            format= "A4";
        Dimension d= PageConstants.getSize(format);
        return d;
    }
    
	protected MultiPageDocument getPager() {
		return g;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getFilterDescription()
	 */
	protected String getFilterDescription()
	{
		return "pdf files";
	}

	
	private static final String extensions[]= {".pdf", ".PDF"};
	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getExtensions()
	 */
	protected String[] getExtensions()
	{
		return extensions;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.application.PagedExporter#getOptionsTitle()
	 */
	protected String getOptionsTitle()
	{
		return "Options for PDF";
	}
}