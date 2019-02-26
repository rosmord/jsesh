package jsesh.graphics.export.pdfExport;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jsesh.graphics.export.generic.BaseGraphics2DFactory;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.CMYKColor;

public class PDFGraphics2DFactory implements BaseGraphics2DFactory {

    private final PDFDocumentWriterAux documentWriter;
    
    public PDFGraphics2DFactory(PDFExportPreferences pdfExportPreferences, String comment) throws FileNotFoundException {
        Rectangle format = pdfExportPreferences.getPageRectangle();
        float height = format.getHeight();
        float width = format.getWidth();
        OutputStream out = new FileOutputStream(pdfExportPreferences.getFile());
        documentWriter = new PDFDocumentWriterAux(pdfExportPreferences, out, width, height, comment);
        documentWriter.open();
    }

    @Override
    public Graphics2D buildGraphics() throws IOException {
        Graphics2D result = documentWriter.createGraphics();
        result.setColor(new CMYKColor(0, 0, 0, 255));
        result.setBackground(new CMYKColor(0, 0, 0, 0));
        return result;
    }

    @Override
    public void setDimension(Dimension2D deviceDimensions) {
        // ???? is this called or not. NOT LOGICAL.
    }

    @Override
    public void writeGraphics() throws IOException {
    }

    public void close() {
        documentWriter.close();
    }

    @Override
    public void newPage() throws IOException {
        documentWriter.getDocument().newPage();
    }

}
