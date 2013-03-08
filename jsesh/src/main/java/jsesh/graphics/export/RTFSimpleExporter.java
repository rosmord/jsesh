/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.export;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;

import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.TranslitterationUtilities;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.PageLayout;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.rtfBasicWriter.RTFFontFamily;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;
import org.qenherkhopeshef.graphics.wmf.WMFGraphics2D;
import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Exports a MDC model into a RTF file (or byte array). Suitable for cut and
 * paste on a macintosh (well, and on other machines as well).
 * <p>
 * Currently used in JSesh not only for plain file export, but also as vector
 * cut-and-paste mecanism.
 * 
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
public class RTFSimpleExporter {

	private static final String TRANSLITFONTNAME = "MDCTranslitLC";
	private static final String TIMES = "Times";
	/**
	 * Constant for mac pict pictures.
	 */
	public static final int MAC_PICT = 0;
	/**
	 * Constant for EMF pictures. EMF is better, but EMF pictures only work well
	 * with OpenOffice.
	 */
	public static final int EMF = 1;
	/**
	 * Constants for WMF pictures.
	 */
	public static final int WMF = 2;
	private int pictureType = MAC_PICT;
	private ViewBuilder viewBuilder;
	private DrawingSpecification drawingSpecifications;
	private RTFExportPreferences rtfPreferences = new RTFExportPreferences();
	private SimpleRTFWriter rtfWriter;

	public RTFSimpleExporter() {
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			pictureType = MAC_PICT;
			// pictureType= EMF;
		} else if (PlatformDetection.getPlatform() == PlatformDetection.WINDOWS) {
			// Temporary patch to allow correct cut and paste with windows
			// pictureType= WMF;
			pictureType = MAC_PICT;
		} else {
			// pictureType= EMF;
			pictureType = MAC_PICT;
		}
	}

	public void ExportModelTo(TopItemList model, OutputStream outputStream)
			throws IOException {
		if (rtfPreferences.getExportGraphicFormat().equals(
				RTFExportPreferences.RTFExportGraphicFormat.WMF)) {
			pictureType = WMF;
		} else if (rtfPreferences.getExportGraphicFormat().equals(
				RTFExportPreferences.RTFExportGraphicFormat.EMF)) {
			pictureType = EMF;
		} else if (rtfPreferences.getExportGraphicFormat().equals(
				RTFExportPreferences.RTFExportGraphicFormat.MACPICT)) {
			pictureType = MAC_PICT;
		}
		rtfWriter = new SimpleRTFWriter(outputStream);
		rtfWriter.declareFont(TIMES, RTFFontFamily.ROMAN);
		rtfWriter.declareFont(TRANSLITFONTNAME, RTFFontFamily.ROMAN);
		Font f= drawingSpecifications.getFont('t');
		rtfWriter.declareFont(drawingSpecifications.getFont('t').getName(), RTFFontFamily.ROMAN);
		// Actual export, using a visitor.
		rtfWriter.writeHeader();
		if (shouldExportAsOnePicture()) {
			exportAsPicture(model);
		} else {
			// Will change...
			drawingSpecifications.setTextDirection(TextDirection.LEFT_TO_RIGHT);
			// Won't
			drawingSpecifications
					.setTextOrientation(TextOrientation.HORIZONTAL);
			RTFExporterAux aux = new RTFExporterAux();
			model.accept(aux);
			aux.close();
		}
		rtfWriter.writeTail();
	}

	/**
	 * Should we export the whole text as one picture ? This must be done when
	 * explicitly requested, or when the text is in column, as it would not give
	 * a good rendering in other cases. (Right now, we will do exactly the same
	 * for right to left text also. Later we will try to improve it).
	 * 
	 * @return
	 */
	private boolean shouldExportAsOnePicture() {
		if (rtfPreferences.getExportGranularity().equals(
				RTFExportGranularity.ONE_LARGE_PICTURE)) {
			return true;
		} else if (rtfPreferences.respectOriginalTextLayout()) {
			if (drawingSpecifications.getTextDirection().equals(
					TextDirection.RIGHT_TO_LEFT)
					|| drawingSpecifications.getTextOrientation().equals(
							TextOrientation.VERTICAL)) {
				return true;
			} else
				return false;
		} else
			return false;
	}

	/**
	 * Export the data as one huge picture.
	 * 
	 * @throws IOException
	 */
	private void exportAsPicture(TopItemList model) throws IOException {
		RTFExportSimpleDrawer simpleDrawer = buildSimpleDrawer(buildMdCForExport(model));
		simpleDrawer.drawTopItemList(model);
		simpleDrawer.writeToRTF(rtfWriter);
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
			DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications.copy();
		PageLayout pageLayout = this.drawingSpecifications.getPageLayout();
		pageLayout.setLeftMargin(01);
		pageLayout.setTopMargin(01);
		this.drawingSpecifications.setPageLayout(pageLayout);
		this.drawingSpecifications.setGrayColor(new Color(200, 200, 200));
	}

	/**
	 * @param rtfPreferences
	 *            The rtfPreferences to set.
	 */
	public void setRtfPreferences(RTFExportPreferences rtfPreferences) {
		this.rtfPreferences = rtfPreferences;
	}

	/**
	 * Build a drawer for creating a picture.
	 * 
	 * @param comment
	 *            a comment placed in the picture (if possible and supported).
	 * @return
	 */
	private RTFExportSimpleDrawer buildSimpleDrawer(String comment) {
		RTFExportSimpleDrawer result = null;
		switch (pictureType) {
		case MAC_PICT:
			result = new MacPictSimpleDrawer(viewBuilder,
					rtfPreferences.getCadratHeight()); // We used to have a
														// multiplier of 1.83
			// here. Why ???
			break;
		case EMF:
			result = new EMFSimpleDrawer(viewBuilder,
					rtfPreferences.getCadratHeight(), comment);
			break;
		case WMF:
			result = new WMFSimpleDrawer(viewBuilder,
					rtfPreferences.getCadratHeight());
			break;
		}
		return result;
	}

	private String buildMdCForExport(TopItemList t) {
		MDCDocument doc = new MDCDocument(t, drawingSpecifications);
		return doc.getMdC();
	}

	private String buildMdCForExport(TopItem t) {
		TopItemList list = new TopItemList();
		list.addTopItem(t.buildTopItem());
		return buildMdCForExport(list);
	}

	public class RTFExporterAux extends ModelElementDeepAdapter {

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
			try {
				flushElements();
				String text = t.getText();
				String fontName = TIMES;
				switch (t.getScriptCode()) {
				case 'l':
					rtfWriter.setBold(false);
					rtfWriter.setItalic(false);
					break;
				case 'b':
					rtfWriter.setBold(true);
					rtfWriter.setItalic(false);
					break;
				case 'i':
					rtfWriter.setBold(false);
					rtfWriter.setItalic(true);
					break;
				case 't':
					rtfWriter.setBold(false);
					rtfWriter.setItalic(false); // italic choosen in the font itself.
					text = TranslitterationUtilities
							.getActualTransliterationString(text,
									drawingSpecifications
											.getTransliterationEncoding());
					fontName = drawingSpecifications.getFont('t').getFontName();
					break;
				case '+':
				default:
					return;
				}
				rtfWriter.useFont(fontName);
				rtfWriter.writeString(text);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementDeepAdapter#visitLineBreak(jsesh.mdc.
		 * model.LineBreak)
		 */
		public void visitLineBreak(LineBreak b) {
			flushElements();
			try {
				rtfWriter.newParagraph();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementDeepAdapter#visitPageBreak(jsesh.mdc.
		 * model.PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			flushElements();
			try {
				rtfWriter.newPage();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void visitTopItem(TopItem t) {
			if (drawingSpecifications.getTextDirection().equals(
					TextDirection.RIGHT_TO_LEFT)
					|| rtfPreferences.getExportGranularity().equals(
							RTFExportGranularity.GROUPED_CADRATS)) {
				if (toDraw == null) {
					toDraw = new TopItemList();
				}
				// TODO : We should modify the drawing methods, so that it's
				// possible to draw any list of top items. Then
				// we would need to create fewer copies of our items.
				toDraw.addTopItem((TopItem) t.deepCopy());
			} else {
				drawElement(t);
			}
		}

		/**
         *
         */
		private void flushElements() {
			try {
				if (drawingSpecifications.getTextDirection().equals(
						TextDirection.RIGHT_TO_LEFT)
						|| rtfPreferences.getExportGranularity().equals(
								RTFExportGranularity.GROUPED_CADRATS)) {
					if (toDraw != null) {
						RTFExportSimpleDrawer simpleDrawer = buildSimpleDrawer(buildMdCForExport(toDraw));
						simpleDrawer.drawTopItemList(toDraw);
						// float deltay = 0;
						// if (simpleDrawer.getCurrentView().getFirstSubView()
						// != null) {
						// deltay = (float) simpleDrawer.getCurrentView()
						// .getFirstSubView().getDeltaBaseY();
						// }

						simpleDrawer.writeToRTF(rtfWriter);
						// rtfWriter.writeMacPictPicture(simpleDrawer.getAsArrayForRTF(),
						// (int) simpleDrawer.getScaledWidth(),
						// (int) simpleDrawer.getScaledHeight());

						toDraw = null;
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void close() {
			flushElements();
			// rtfDocument.close();
		}

		private void drawElement(TopItem t) {

			RTFExportSimpleDrawer simpleDrawer = buildSimpleDrawer(buildMdCForExport(t));
			simpleDrawer.drawElement(t);
			try {
				// float deltay = 0;
				if (simpleDrawer.getCurrentView().getFirstSubView() != null) {
					// deltay = (float) simpleDrawer.getCurrentView()
					// .getFirstSubView().getDeltaBaseY();
				}
				simpleDrawer.writeToRTF(rtfWriter);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static abstract class RTFExportSimpleDrawer extends
			TopItemSimpleDrawer {

		protected RTFExportSimpleDrawer(ViewBuilder viewBuilder,
				DrawingSpecification drawingSpecifications, double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
		}

		public abstract void writeToRTF(SimpleRTFWriter rtfWriter)
				throws IOException;

		abstract public byte[] getAsArrayForRTF();

	}

	private class EMFSimpleDrawer extends RTFExportSimpleDrawer {

		private EMFGraphics2D emGraphics2D;
		private RandomAccessByteArray out;
		private String comment;

		public EMFSimpleDrawer(ViewBuilder viewBuilder, double cadratHeight,
				String comment) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
			setShadeAfter(false);
			this.comment = comment;
		}

		protected Graphics2D buildGraphics() {
			out = new RandomAccessByteArray();
			emGraphics2D = null;
			try {
				// TODO : change the "JSesh" name here into a field...
				emGraphics2D = new EMFGraphics2D(out,
						(int) getScaledWidth() + 1,
						(int) getScaledHeight() + 1, "JSesh", comment);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return emGraphics2D;
		}

		public byte[] getAsArrayForRTF() {
			return out.getByteArray();
		}

		public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
			rtfWriter.writeEmfPicture(getAsArrayForRTF(), getScaledWidth(),
					getScaledHeight());
		}
	}

	private class WMFSimpleDrawer extends RTFExportSimpleDrawer {

		private WMFGraphics2D wmfGraphics2D;
		private RandomAccessByteArray out;
		private double deviceScale = 1.0;

		public WMFSimpleDrawer(ViewBuilder viewBuilder, double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
			setShadeAfter(false);
		}

		protected Graphics2D buildGraphics() {
			out = new RandomAccessByteArray();
			wmfGraphics2D = null;
			try {
				wmfGraphics2D = new WMFGraphics2D(out,
						(int) getScaledWidth() + 1, (int) getScaledHeight() + 1);
				wmfGraphics2D.setPrecision(1);
				deviceScale = wmfGraphics2D.getTransform().getScaleX();
				drawingSpecifications.setGraphicDeviceScale(deviceScale);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return wmfGraphics2D;
		}

		public byte[] getAsArrayForRTF() {
			// Skip the placeable header.
			return out.getByteArray(22);
		}

		public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
			rtfWriter.writeWmfPicture(getAsArrayForRTF(), getScaledWidth(),
					getScaledHeight());

		}

	}

	private class MacPictSimpleDrawer extends RTFExportSimpleDrawer {

		private MacPictGraphics2D macPictGraphics2D;

		/**
		 * @param viewBuilder
		 * @param cadratHeight
		 * 
		 */
		protected MacPictSimpleDrawer(ViewBuilder viewBuilder,
				double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
			setShadeAfter(false);
		}

		protected Graphics2D buildGraphics() {

			macPictGraphics2D = new MacPictGraphics2D(0, 0,
					getScaledWidth() + 1, getScaledHeight() + 1);

			return macPictGraphics2D;
		}

		public byte[] getAsArrayForRTF() {
			return macPictGraphics2D.getAsArrayForRTF();
		}

		public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
			rtfWriter.writeMacPictPicture(getAsArrayForRTF(), getScaledWidth(),
					getScaledHeight());
		}
	}
}
