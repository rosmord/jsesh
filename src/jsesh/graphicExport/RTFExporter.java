/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphicExport;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import java.awt.Dimension;
import java.awt.Graphics2D;

import jsesh.graphicExport.RTFExportPreferences.RTFExportGranularity;
import jsesh.graphics.generic.RandomAccessByteArray;
import jsesh.graphics.wmfexport.WMFGraphics2D;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.layout.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.utils.TranslitterationUtilities;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.style.RtfFont;

/**
 * Exports a MDC model into a RTF file (or byte array).
 * <p>
 * Currently used in JSesh not only for plain file export, but also as vector
 * cut-and-paste mecanism.
 * <p>
 * After considering as an option the possibility to create a generic class to
 * unify HTML, PDF and Rtf output, it appeared that the common part was quite
 * small, so we decided to write utility classes for the common parts.
 * 
 * <p>
 * There are currently problems with Itext and WMF imports. itext1.3 and 1.4
 * behave differently with respect to the imported wmf file size.
 * 
 * @author rosmord
 * 
 */
public class RTFExporter {

	private ViewBuilder viewBuilder;

	private DrawingSpecifications drawingSpecifications;

	private RTFExportPreferences rtfPreferences = new RTFExportPreferences();

	private Document rtfDocument;

	public void ExportModelTo(TopItemList model, OutputStream outputStream) {
		// Ensure the drawing specifications are correct in the builder.
		// FIXME REALLY CORRECT THE WAY DRAWINGSPECIFICATION AND BUILDER WORK.
		// DEFAULTS SHOULD BE SIMPLE TO USE.
		rtfDocument = new Document();
		RtfWriter2.getInstance(rtfDocument, outputStream);
		// Actual export, using a visitor.
		rtfDocument.open();
		if (rtfPreferences.getExportGranularity().equals(
				RTFExportGranularity.ONE_LARGE_PICTURE)) {
			exportAsPicture(model);
		} else {
			RTFExporterAux aux = new RTFExporterAux();
			model.accept(aux);
			aux.close();
		}
		rtfDocument.close();
	}

	/**
	 * Export the WMF data as one huge picture.
	 */
	private void exportAsPicture(TopItemList model) {
		RandomAccessByteArray b = new RandomAccessByteArray();

		// Please, note :
		// Due to the little control I have on IText's output of wmf pictures,
		// I decided to use a simple hack here. This should be removed when we
		// use our own
		// RTF output. The size we got from IText was more or less twice as
		// small as the wished size.
		// so...
		WMFSimpleDrawer simpleDrawer = buildSimpleDrawer(b);

		simpleDrawer.drawTopItemList(model);

		try {
			Image img = Image.getInstance(b.getByteArray());
			rtfDocument.add(img);
		} catch (BadElementException exception) {
			exception.printStackTrace();
		} catch (MalformedURLException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (DocumentException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * @param viewBuilder
	 *            The viewBuilder to set.
	 */
	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	/**
	 * @param drawingSpecifications
	 *            The drawingSpecifications to set.
	 */
	public void setDrawingSpecifications(
			DrawingSpecifications drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications.copy();
		this.drawingSpecifications.setLeftMargin(01);
		this.drawingSpecifications.setTopMargin(01);
	}

	/**
	 * @param rtfPreferences
	 *            The rtfPreferences to set.
	 */
	public void setRtfPreferences(RTFExportPreferences rtfPreferences) {
		this.rtfPreferences = rtfPreferences;
	}

	WMFSimpleDrawer buildSimpleDrawer(RandomAccessByteArray b) {
		return new WMFSimpleDrawer(
				viewBuilder,
				rtfPreferences.getCadratHeight() * 1.83, b);
	}

	public class RTFExporterAux extends ModelElementDeepAdapter {
		static final String TIMES = "Times New Roman";

		private RtfFont romanFont = new RtfFont(TIMES, 12);

		private RtfFont boldFont = new RtfFont(TIMES, 12, RtfFont.BOLD);

		private RtfFont italicFont = new RtfFont(TIMES, 12, RtfFont.ITALIC);

		private RtfFont translitFont = new RtfFont("MDCTranslitLC", 12);

		private Paragraph currentParagraph = null;

		/**
		 * list of elements to draw when the mode is
		 * RTFExportGranularity.GROUPED_CADRATS.
		 */

		TopItemList toDraw = null;

		public void visitTopItemList(TopItemList t) {
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				t.getTopItemAt(i).accept(this);
			}
		}

		public void visitAlphabeticText(AlphabeticText t) {
			flushElements();
			RtfFont f = null;
			String text = t.getText();

			switch (t.getScriptCode()) {
			case 'l':
				f = romanFont;
				break;
			case 'b':
				f = boldFont;
				break;
			case 'i':
				f = italicFont;
				break;
			case 't':
				f = translitFont;
				text = TranslitterationUtilities.toLowerCase(text);
				break;
			case '+':
			default:
				return;
			}

			write(text, f);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
		 */
		public void visitLineBreak(LineBreak b) {
			flushElements();
			flushParagraph();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			flushElements();
			flushParagraph();
		}

		public void visitTopItem(TopItem t) {
			if (rtfPreferences.getExportGranularity().equals(
					RTFExportGranularity.GROUPED_CADRATS)) {
				if (toDraw == null)
					toDraw = new TopItemList();
				// TODO : We should modify the drawing methods, so that it's
				// possible to draw any list of top items. Then
				// we would need to create fewer copies of our items.
				toDraw.addTopItem((TopItem) t.deepCopy());
			} else
				drawElement(t);
		}

		/**
		 * 
		 */
		private void flushElements() {
			if (rtfPreferences.getExportGranularity().equals(
					RTFExportGranularity.GROUPED_CADRATS)) {
				if (toDraw != null) {
					try {
						RandomAccessByteArray b = new RandomAccessByteArray();
						WMFSimpleDrawer simpleDrawer = buildSimpleDrawer(b);
						simpleDrawer.drawTopItemList(toDraw);
						Image img = Image.getInstance(b.getByteArray());
						float deltay = 0;
						if (simpleDrawer.getCurrentView().getFirstSubView() != null)
							deltay = (float) simpleDrawer.getCurrentView()
									.getFirstSubView().getDeltaBaseY();

						getCurrentParagraph().add(
								new Chunk(img, 0f, -6f + deltay, true));
					} catch (BadElementException exception) {
						exception.printStackTrace();
					} catch (MalformedURLException exception) {
						exception.printStackTrace();
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					toDraw= null;
				}
			}
		}

		/**
		 * @return Returns the currentParagraph.
		 */
		public Paragraph getCurrentParagraph() {
			if (currentParagraph == null) {
				currentParagraph = new Paragraph();
			}
			return currentParagraph;
		}

		public void close() {
			flushElements();
			flushParagraph();
			// rtfDocument.close();
		}

		/**
		 * ends a paragraph.
		 */
		private void flushParagraph() {
			try {
				rtfDocument.add(getCurrentParagraph());
			} catch (DocumentException exception) {
				exception.printStackTrace();
			}
			currentParagraph = null;
		}

		/**
		 * @param text
		 * @param f
		 */
		private void write(String text, RtfFont f) {
			getCurrentParagraph().add(new Chunk(text, f));
		}

		private void drawElement(TopItem t) {
			RandomAccessByteArray b = new RandomAccessByteArray();

			// Please, note :
			// Due to the little control I have on IText's output of wmf
			// pictures,
			// I decided to use a simple hack here. This should be removed when
			// we use our own
			// RTF output. The size we got from IText was more or less twice as
			// small as the wished size.
			// so...
			WMFSimpleDrawer simpleDrawer = buildSimpleDrawer(b);
			simpleDrawer.drawElement(t);
			try {

				Image img = Image.getInstance(b.getByteArray());
				float deltay = 0;
				if (simpleDrawer.getCurrentView().getFirstSubView() != null)
					deltay = (float) simpleDrawer.getCurrentView()
							.getFirstSubView().getDeltaBaseY();

				/*
				 * img.scaleAbsolute(simpleDrawer.getScaledWidth(), simpleDrawer
				 * .getScaledHeight());
				 */
				// img.setDpi(1440, 1440);
				getCurrentParagraph().add(
						new Chunk(img, 0f, -6f + deltay, true));
			} catch (BadElementException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class WMFSimpleDrawer extends TopItemSimpleDrawer {
		RandomAccessByteArray b;

		/**
		 * @param viewBuilder
		 * @param cadratHeight
		 * @param b
		 *            the array where the wmf data will be written.
		 */
		protected WMFSimpleDrawer(ViewBuilder viewBuilder, double cadratHeight,
				RandomAccessByteArray b) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
			this.b = b;
		}

		protected WMFSimpleDrawer(ViewBuilder viewBuilder, double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
		}

		protected Graphics2D buildGraphics() {
			WMFGraphics2D g = null;
			try {
				g = new WMFGraphics2D(b, new Dimension((int) Math
						.ceil(getScaledWidth()), (int) Math
						.ceil(getScaledHeight())));
				g.setPrecision(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return g;
		}

	}
}
