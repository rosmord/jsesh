package jsesh.swing;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import javax.swing.ImageIcon;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.PageLayout;

/**
 * Static icons repository.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class ImageIconFactory {

	/**
	 * Cache for icons. Note that the cache will leak a bit of memory, but only
	 * for MDC code... which is finite.
	 */
	private static HashMap<String, SoftReference<ImageIcon>> iconMap = new HashMap<String, SoftReference<ImageIcon>>();

	/**
	 * @param mdcText
	 * @return an image for the given manuel de codage text.
	 */
	public static synchronized ImageIcon buildImage(String mdcText) {
		ImageIcon imageIcon = null;
		if (iconMap.containsKey(mdcText)) {
			imageIcon = iconMap.get(mdcText).get();
		}
		if (imageIcon == null) {
			MDCDrawingFacade drawing = prepareFacade();

			BufferedImage bufferedImage = null;
			try {
				bufferedImage = drawing.createImage(mdcText);
			} catch (MDCSyntaxError e) {
				throw new RuntimeException("Error when parsing " + mdcText, e);
			}
			imageIcon = new ImageIcon(bufferedImage);
			iconMap.put(mdcText, new SoftReference<ImageIcon>(imageIcon));
		}

		return imageIcon;
	}

	/**
	 * Build the picture of a single symbol (not real glyphs, usually
	 * parenthesis and the like).
	 * 
	 * @param symbolCode
	 *            the code for the symbol, from {@link SymbolCodes}
	 * @return an image for the given manuel de codage text.
	 */
	public static ImageIcon buildImage(int symbolCode) {
		String mdc = LexicalSymbolsUtils.getStringForLexicalItem(symbolCode);
		return buildImage(mdc);
	}

	/**
	 * @return
	 */
	private static MDCDrawingFacade prepareFacade() {
		MDCDrawingFacade drawing = new MDCDrawingFacade();
		DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();
		drawingSpecifications.setMaxCadratHeight(20);
		PageLayout pageLayout = drawingSpecifications.getPageLayout();
		pageLayout.setLeftMargin(0);
		pageLayout.setTopMargin(0);
		drawingSpecifications.setPageLayout(pageLayout);
		drawing.setDrawingSpecifications(drawingSpecifications);
		drawing.setPhilologySign(true);
		return drawing;
	}

}
