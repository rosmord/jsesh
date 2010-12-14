package jsesh.graphics.export.pdfExport;

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
public class PDFDataSaver {

	DrawingSpecification drawingSpecification;
	PDFExportPreferences pdfExportPreferences;

	/**
	 * Create a new {@link PDFDataSaver}.
	 * 
	 * @param drawingSpecification
	 *            the drawing specification this object will use as a basis.
	 */
	public PDFDataSaver(DrawingSpecification drawingSpecification,
			PDFExportPreferences pdfExportPreferences) {
		this.drawingSpecification = drawingSpecification.copy();
		this.pdfExportPreferences = pdfExportPreferences;
	}

	public PDFDataSaver(DrawingSpecification drawingSpecification) {
		this(drawingSpecification, new PDFExportPreferences());
	}

	public ByteArrayInputStream getPDFContent(TopItemList topItemList)
			throws IOException {
		ByteArrayOutputStream imageArray = new ByteArrayOutputStream();
		writeSinglePagePDF(imageArray, topItemList);
		return new ByteArrayInputStream(imageArray.toByteArray());
	}

	public void writeSinglePagePDF(OutputStream out, TopItemList topItemList)
			throws IOException {
		// Dirty code which needs to be cleaned up.
		DrawingSpecification drawingSpecification = this.drawingSpecification
				.copy();
		// Ensure CMYK colour space...

		// TODO REMOVE DUPLICATED CODE BELOW.
		// (CODE ALSO IN PDFEXPORTER).
		PDFExportHelper.prepareColors(drawingSpecification);
		
		PageLayout pageLayout= drawingSpecification.getPageLayout();
		pageLayout.setLeftMargin(1);
		pageLayout.setTopMargin(1);
		drawingSpecification.setPageLayout(pageLayout);

		// If this works, we will need to move this code elsewhere as it
		// might be useful.
		ViewBuilder builder = new SimpleViewBuilder();
		MDCView view = builder.buildView(topItemList, drawingSpecification);

		PDFDocumentWriterAux documentWriterAux = new PDFDocumentWriterAux(
				pdfExportPreferences, out, view.getWidth(), view.getHeight(),
				PDFExportHelper.buildCommentText(drawingSpecification,
						topItemList));
		documentWriterAux.open();

		Graphics2D g = documentWriterAux.createGraphics(view.getWidth(), view
				.getHeight());
		g.setFont(documentWriterAux.getFont());
		ViewDrawer drawer = new ViewDrawer();
		drawer.setShadeAfter(false);
		drawer.draw(g, view, drawingSpecification);
		g.dispose();
		documentWriterAux.close();
	}

}
