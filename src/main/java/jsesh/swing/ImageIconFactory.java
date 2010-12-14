package jsesh.swing;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.PageLayout;

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
			throw new RuntimeException("Error when parsing "+ mdcText, e);
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
		DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();
		drawingSpecifications.setMaxCadratHeight(20);
		PageLayout pageLayout= drawingSpecifications.getPageLayout();
		pageLayout.setLeftMargin(0);
		pageLayout.setTopMargin(0);
		drawingSpecifications.setPageLayout(pageLayout);
		drawing.setDrawingSpecifications(drawingSpecifications);
		drawing.setPhilologySign(true);
		return drawing;
	}

}
