/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphicExport;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;

import org.qenherkhopeshef.utils.PlatformDetection;

import jsesh.graphicExport.RTFExportPreferences.RTFExportGranularity;
import jsesh.graphics.emf.EMFGraphics2D;
import jsesh.graphics.generic.RandomAccessByteArray;
import jsesh.graphics.pict.MacPictGraphics2D;
import jsesh.graphics.rtfBasicWriter.SimpleRTFWriter;
import jsesh.graphics.wmf.WMFGraphics2D;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.utils.TranslitterationUtilities;

/**
 * Exports a MDC model into a RTF file (or byte array). Suitable for cut and
 * paste on a macintosh.
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
public class MacRTFExporter {

	/**
	 * Constant for mac pict pictures.
	 */
	public static final int MAC_PICT= 0;
	
	/**
	 * Constant for EMF pictures.
	 */
	public static final int EMF= 1;
	
	/**
	 * Constants for WMF pictures.
	 */
	public static final int WMF= 2;
	
	private int pictureType= MAC_PICT;
	
	private ViewBuilder viewBuilder;

	private DrawingSpecifications drawingSpecifications;

	private RTFExportPreferences rtfPreferences = new RTFExportPreferences();

	private SimpleRTFWriter rtfWriter;

	
	public MacRTFExporter() {
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			pictureType= MAC_PICT;
			//pictureType= EMF;
		} else if (PlatformDetection.getPlatform() == PlatformDetection.WINDOWS){
			// Temporary patch to allow correct cut and paste with windows
			pictureType= WMF;
		} else {
			pictureType= EMF;
		}
	}

	public void ExportModelTo(TopItemList model, OutputStream outputStream)
			throws IOException {
		rtfWriter = new SimpleRTFWriter(outputStream);
		// Actual export, using a visitor.
		rtfWriter.writeHeader();
		if (rtfPreferences.getExportGranularity().equals(
				RTFExportGranularity.ONE_LARGE_PICTURE)) {
			exportAsPicture(model);
		} else {
			RTFExporterAux aux = new RTFExporterAux();
			model.accept(aux);
			aux.close();
		}
		rtfWriter.writeTail();
	}

	/**
	 * Export the data as one huge picture.
	 * 
	 * @throws IOException
	 */
	private void exportAsPicture(TopItemList model) throws IOException {
		RTFExportSimpleDrawer simpleDrawer = buildSimpleDrawer();
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

	private RTFExportSimpleDrawer buildSimpleDrawer() {
		switch (pictureType) {
		case MAC_PICT:
			return new MacPictSimpleDrawer(viewBuilder, rtfPreferences
					.getCadratHeight()); // We used to have a multiplier of 1.83 here. Why ???
			
		case EMF:
			return new EMFSimpleDrawer(viewBuilder, rtfPreferences
					.getCadratHeight());
		case WMF:
			return new WMFSimpleDrawer(viewBuilder,rtfPreferences.getCadratHeight());
		default:
			return null;
		}
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
			flushElements();
			String text = t.getText();
			char code = 'p';
			switch (t.getScriptCode()) {
			case 'l':
				code = 'p';
				break;
			case 'b':
				code = 'b';
				break;
			case 'i':
				code = 'i';
				break;
			case 't':
				text = TranslitterationUtilities.toLowerCase(text);
				break;
			case '+':
			default:
				return;
			}
			try {
				rtfWriter.startStyle(code);
				rtfWriter.writeString(text);
				rtfWriter.endBlock();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
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
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
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
			try {
				if (rtfPreferences.getExportGranularity().equals(
						RTFExportGranularity.GROUPED_CADRATS)) {
					if (toDraw != null) {
						RTFExportSimpleDrawer simpleDrawer = buildSimpleDrawer();
						simpleDrawer.drawTopItemList(toDraw);
						float deltay = 0;
						if (simpleDrawer.getCurrentView().getFirstSubView() != null)
							deltay = (float) simpleDrawer.getCurrentView()
									.getFirstSubView().getDeltaBaseY();
						
						simpleDrawer.writeToRTF(rtfWriter);
//						rtfWriter.writeMacPictPicture(simpleDrawer.getAsArrayForRTF(),
//								(int) simpleDrawer.getScaledWidth(),
//								(int) simpleDrawer.getScaledHeight());

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
			RandomAccessByteArray b = new RandomAccessByteArray();

			RTFExportSimpleDrawer simpleDrawer = buildSimpleDrawer();
			simpleDrawer.drawElement(t);
			try {
				float deltay = 0;
				if (simpleDrawer.getCurrentView().getFirstSubView() != null)
					deltay = (float) simpleDrawer.getCurrentView()
							.getFirstSubView().getDeltaBaseY();
				simpleDrawer.writeToRTF(rtfWriter);
//				rtfWriter.writeMacPictPicture(simpleDrawer.getAsArrayForRTF(),
//						(int) simpleDrawer.getScaledWidth(), (int) simpleDrawer
//								.getScaledHeight());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}


	private static abstract class RTFExportSimpleDrawer extends TopItemSimpleDrawer {
		
		protected RTFExportSimpleDrawer(ViewBuilder viewBuilder, DrawingSpecifications drawingSpecifications, double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
		}

		public abstract void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException;

		abstract public byte[] getAsArrayForRTF();
	}
	
	private class EMFSimpleDrawer extends RTFExportSimpleDrawer {

		private EMFGraphics2D emGraphics2D;
		private RandomAccessByteArray out;
		
		public EMFSimpleDrawer(ViewBuilder viewBuilder, double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
			setShadeAfter(false);
		}
		
		protected Graphics2D buildGraphics() {
			out= new RandomAccessByteArray();
			emGraphics2D= null;
			try {
				emGraphics2D= new EMFGraphics2D(out,(int)getScaledWidth()+1, (int)getScaledHeight()+1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return emGraphics2D;
		}

		public byte[] getAsArrayForRTF() {
			return out.getByteArray();
		}

		public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException{
				double picScale= 100.0 / LengthUnit.MM.getPointsValue();
				rtfWriter.writeEmfPicture(getAsArrayForRTF(),
						(int)Math.ceil(getScaledWidth()*picScale),(int)Math.ceil(getScaledHeight()*picScale)); 
		
		}
		
	}

	private class WMFSimpleDrawer extends RTFExportSimpleDrawer {

		private WMFGraphics2D wmfGraphics2D;
		private RandomAccessByteArray out;
		
		public WMFSimpleDrawer(ViewBuilder viewBuilder, double cadratHeight) {
			super(viewBuilder, drawingSpecifications, cadratHeight);
			setShadeAfter(false);
		}
		
		protected Graphics2D buildGraphics() {
			out= new RandomAccessByteArray();
			wmfGraphics2D= null;
			try {
				wmfGraphics2D= new WMFGraphics2D(out,(int)getScaledWidth()+1, (int)getScaledHeight()+1);
				wmfGraphics2D.setPrecision(1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return wmfGraphics2D;
		}

		public byte[] getAsArrayForRTF() {
			// Skip the placeable header.
			return out.getByteArray(22);
		}

		public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException{
				double picScale= 100.0 / LengthUnit.MM.getPointsValue();
				rtfWriter.writeWmfPicture(getAsArrayForRTF(),
						(int)Math.ceil(getScaledWidth()*picScale),(int)Math.ceil(getScaledHeight()*picScale)); 
		
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
			
			macPictGraphics2D = new MacPictGraphics2D(0,0, getScaledWidth()+1, getScaledHeight()+1);
			
			
			return macPictGraphics2D;
		}

		/**
		 * @return the macPictGraphics2D
		 */
		public MacPictGraphics2D getMacPictGraphics2D() {
			return macPictGraphics2D;
		}


		public byte[] getAsArrayForRTF() {
			return macPictGraphics2D.getAsArrayForRTF();
		}


		public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
			rtfWriter.writeMacPictPicture(getAsArrayForRTF(),
					(int)Math.ceil(getScaledWidth()),(int)Math.ceil(getScaledHeight())); 
		}

		
	}
}
