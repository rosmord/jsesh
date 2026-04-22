package jsesh.graphics.export.pdfExport;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.drawingspecifications.JSeshStyle.Builder;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.TopItemList;

import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.SpotColor;

class PDFExportHelper {

	private PDFExportHelper() {
	}

	/**
	 * @param model
	 * @return
	 */
	public static String buildCommentText(JSeshStyle jseshStyle,
			TopItemList model) {

		MDCDocument doc = new MDCDocument(model, jseshStyle);
		// As requested by the IFAO, we save the Manuel de codage content in
		// the picture as a comment.
		return PDFExportConstants.CONTENT_TYPE_APPLICATION_JSESH2007
				+ doc.getMdC();
	}

	/**
	 * Sets the colors to something suitable for color separation. this is a
	 * sorry excuse for correct program structure. we should do better.
	 * 
	 * @param jseshStyle the current style
	 * @return a copy of the style, with colors set to CMYK standards.
	 */
	public static JSeshStyle.Builder ensureCMYKColorSpace(
			JSeshStyle jseshStyle) {

		// Use PANTONE COLOR
		PdfSpotColor redSpot = new PdfSpotColor("PANTONE 187 C", 1,
				new CMYKColor(0, 1, 0.8f, 0.2f));
		return jseshStyle.copy()
				.painting(colors -> colors
						.redColor(new SpotColor(redSpot))
						.blackColor(new CMYKColor(0, 0, 0, 255))
						.grayColor(new CMYKColor(0, 0, 0, 60)));

	}

}
