package jsesh.ui.export.pdfExport;

import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.SpotColor;

import jsesh.document.embeddedsigns.EmbeddedSignShapeRepository;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.io.constants.PDFExportConstants;
import jsesh.io.document.MDCDocumentWriter;
import jsesh.model.HieroglyphicText;

class PDFExportHelper {

	private PDFExportHelper() {
	}

	/// The MdC source stored as a comment in exported PDF files.
	///
	/// @param jseshStyle the style, written in the header.
	/// @param repository the shapes used; the embedded signs it holds and
	///                   `model` uses are written in the header.
	/// @param model      the text.
	/// @return the comment text.
	public static String buildCommentText(JSeshStyle jseshStyle,
			HieroglyphShapeRepository repository, HieroglyphicText model) {

		// As requested by the IFAO, we save the Manuel de codage content in
		// the picture as a comment.
		return PDFExportConstants.CONTENT_TYPE_APPLICATION_JSESH2007
				+ new MDCDocumentWriter().toMdC(model,
						DocumentPreferencesStyleConverter
								.toDocumentPreferences(jseshStyle),
						EmbeddedSignShapeRepository.usedIn(model, repository));
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
