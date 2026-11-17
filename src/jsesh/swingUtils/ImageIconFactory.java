package jsesh.swingUtils;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

public class ImageIconFactory {

	/**
	 * 
	 * @param mdcText
	 * @return an image for the given manuel de codage text.
	 */
	public static ImageIcon buildImage(String mdcText) {
		MDCDrawingFacade drawing = prepareFacade();
	
		BufferedImage result = null;
		try {
			result = drawing.createImage(mdcText);
		} catch (MDCSyntaxError e) {
			throw new RuntimeException(e);
		}
		return new ImageIcon(result);
	}

	
	/**
	 * 
	 * @param mdcText
	 * @return an image for the given manuel de codage text.
	 */
	public static ImageIcon buildImage(TopItemList topItemList) {
		MDCDrawingFacade drawing = prepareFacade();
	
		BufferedImage result = null;
		
		result = drawing.createImage(topItemList);
		return new ImageIcon(result);
	}


	/**
	 * @return
	 */
	private static MDCDrawingFacade prepareFacade() {
		MDCDrawingFacade drawing = new MDCDrawingFacade();
		DrawingSpecifications drawingSpecifications = new DrawingSpecifications();
		drawingSpecifications.setMaxCadratHeight(20);
		drawingSpecifications.setLeftMargin(0);
		drawingSpecifications.setTopMargin(0);
		drawing.setDrawingSpecifications(drawingSpecifications);
		drawing.setPhilologySign(true);
		return drawing;
	}

}
