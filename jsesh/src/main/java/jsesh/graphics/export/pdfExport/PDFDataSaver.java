package jsesh.graphics.export.pdfExport;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer;
import jsesh.mdcDisplayer.layout.Layout;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;

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

    private JSeshStyle jSeshStyle;
    private PDFExportPreferences pdfExportPreferences;
    private float scale = 1.0f;
    private HieroglyphsDrawer hieroglyphsDrawer;

    /**
     * Create a new {@link PDFDataSaver}.
     *
     * @param drawingSpecification the drawing specification this object will
     * use as a basis.
     * @param pdfExportPreferences pdf-specific preferences.
     *
     */
    public PDFDataSaver(JSeshStyle jSeshStyle,
            HieroglyphsDrawer hieroglyphsDrawer,
            PDFExportPreferences pdfExportPreferences) {
        this.jSeshStyle = jSeshStyle;
        this.pdfExportPreferences = pdfExportPreferences;
        this.hieroglyphsDrawer = hieroglyphsDrawer;
    }

    /**
     * Export with default pdf preferences.
     *
     * @param drawingSpecification
     */
    public PDFDataSaver(JSeshStyle jSeshStyle, HieroglyphsDrawer hieroglyphsDrawer) {
        this(jSeshStyle, hieroglyphsDrawer, new PDFExportPreferences());
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
        float margin = 1f;
        JSeshStyle myDrawingSpecification = 
            PDFExportHelper.ensureCMYKColorSpace(this.jSeshStyle)
            .copy()
            .geometry(geom ->
                geom
                    .leftMargin(margin* 0.5f / scale)
                    .topMargin(margin* 0.5f / scale)
            ).build()
            ;

        JSeshRenderContext renderContext = JSeshRenderContext.buildBadDefault(myDrawingSpecification, hieroglyphsDrawer);

        ViewBuilder builder = new ViewBuilder();
        MDCView view = builder.buildView(topItemList, renderContext);

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
        drawer.draw(g, renderContext, view);

        g.dispose();
        documentWriterAux.close();
    }
}
