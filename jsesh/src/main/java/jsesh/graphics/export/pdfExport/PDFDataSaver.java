package jsesh.graphics.export.pdfExport;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.PageLayout;

// TODO : use EXPORTDATA !!
// TODO : mix with PDFExporter to avoid code duplication.
// TODO : also mix with TopItemSimpleDrawer (which should be generalized) to factorize common code.
/**
 * This class is redundant with PDFExporter, and one of them should go.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class PDFDataSaver {

    private DrawingSpecification drawingSpecification;
    private PDFExportPreferences pdfExportPreferences;
    private float scale = 1.0f;

    /**
     * Create a new {@link PDFDataSaver}.
     *
     * @param drawingSpecification the drawing specification this object will
     * use as a basis.
     * @param pdfExportPreferences pdf-specific preferences.
     *
     */
    public PDFDataSaver(DrawingSpecification drawingSpecification,
            PDFExportPreferences pdfExportPreferences) {
        this.drawingSpecification = drawingSpecification.copy();
        this.pdfExportPreferences = pdfExportPreferences;
    }

    /**
     * Export with default pdf preferences.
     *
     * @param drawingSpecification
     */
    public PDFDataSaver(DrawingSpecification drawingSpecification) {
        this(drawingSpecification, new PDFExportPreferences());
    }

    /**
     * Set the scale applied to the whole picture (after drawingspecifications
     * have been used).
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public ByteArrayInputStream createPDFContent(TopItemList topItemList)
            throws IOException {
        ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
        writeSinglePagePDF(imageArray, topItemList);
        return new ByteArrayInputStream(imageArray.toByteArray());
    }

    public void writeSinglePagePDF(OutputStream out, TopItemList topItemList)
            throws IOException {
        // Dirty code which needs to be cleaned up.
        DrawingSpecification myDrawingSpecification = this.drawingSpecification
                .copy();

        PDFExportHelper.ensureCMYKColorSpace(myDrawingSpecification);

        // Always allow a 0.5-point margin for the document ?
        PageLayout pageLayout = myDrawingSpecification.getPageLayout();
        float margin = 1f;
        pageLayout.setLeftMargin(margin * 0.5f / scale);
        pageLayout.setTopMargin(margin * 0.5f / scale);

        myDrawingSpecification.setPageLayout(pageLayout);

        ViewBuilder builder = new SimpleViewBuilder();
        MDCView view = builder.buildView(topItemList, myDrawingSpecification);

        PDFDocumentWriterAux documentWriterAux = new PDFDocumentWriterAux(
                pdfExportPreferences, out, margin + scale * view.getWidth(),
                margin + scale * view.getHeight(),
                PDFExportHelper.buildCommentText(myDrawingSpecification,
                        topItemList));
        documentWriterAux.open();

        Graphics2D g = documentWriterAux.createGraphics();
        g.setFont(documentWriterAux.getFont());
        g.scale(scale, scale);
        g.setStroke(new BasicStroke(0));
        ViewDrawer drawer = new ViewDrawer();
        drawer.setShadeAfter(false);
        drawer.draw(g, view, myDrawingSpecification);

        g.dispose();
        documentWriterAux.close();
    }
}
