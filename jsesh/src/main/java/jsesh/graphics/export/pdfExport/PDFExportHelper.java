package jsesh.graphics.export.pdfExport;

import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.SpotColor;

class PDFExportHelper {

	/**
	 * @param model
	 * @return
	 */
	public static String buildCommentText(DrawingSpecification specification,
			TopItemList model) {

		MDCDocument doc = new MDCDocument(model, specification);
		// As requested by the IFAO, we save the Manuel de codage content in
		// the picture as a comment.
		return PDFExportConstants.CONTENT_TYPE_APPLICATION_JSESH2007
				+ doc.getMdC();
	}

	/**
	 * Sets the colors to something suitable for color separation. this is a
	 * sorry excuse for correct program structure. we should do better.
	 * 
	 * @param drawingSpecifications
	 */
	public static void ensureCMYKColorSpace(
			DrawingSpecification drawingSpecifications) {

		// Use PANTONE COLOR
		PdfSpotColor redSpot = new PdfSpotColor("PANTONE 187 C", 1,
				new CMYKColor(0, 1, 0.8f, 0.2f));
		drawingSpecifications.setRedColor(new SpotColor(redSpot));
		drawingSpecifications.setBlackColor(new CMYKColor(0, 0, 0, 255));
		drawingSpecifications.setGrayColor(new CMYKColor(0, 0, 0, 60));
	}

}
