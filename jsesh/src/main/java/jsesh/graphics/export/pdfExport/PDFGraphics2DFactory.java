package jsesh.graphics.export.pdfExport;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jsesh.graphics.export.BaseGraphics2DFactory;

import com.lowagie.text.Rectangle;

public class PDFGraphics2DFactory implements BaseGraphics2DFactory {

	PDFDocumentWriterAux documentWriter;
	private float height;
	private float width;
	
	public PDFGraphics2DFactory(PDFExportPreferences pdfExportPreferences, String comment) throws FileNotFoundException {		
		Rectangle format = pdfExportPreferences.getPageRectangle();
		 height=format.getHeight();
		 width= format.getWidth();
		OutputStream out= new FileOutputStream(pdfExportPreferences.getFile());
		documentWriter= new PDFDocumentWriterAux(pdfExportPreferences, out, width, height,comment);
		documentWriter.open();
	}
	
	public Graphics2D buildGraphics() throws IOException {
		return documentWriter.createGraphics();
	}
	
	

	public void setDimension(Dimension2D deviceDimensions) {
		
	}

	public void writeGraphics() throws IOException {
		documentWriter.getDocument().newPage();
	}

	public void close() {
		documentWriter.close();
	}

}
