/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.application;

import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * An action whose icon is built from an MDCtype.
 * @author S. Rosmorduc
 *
 */
abstract public class MDCIconAction extends AbstractAction {

	public MDCIconAction(String mdcText) {
		super("", buildImage(mdcText));
	}
	
	
	/**
	 * 
	 * @param mdcText
	 * @return an image for the given manuel de codage text.
	 */
	private static ImageIcon buildImage(String mdcText) {
		MDCDrawingFacade drawing= new MDCDrawingFacade();
		DrawingSpecifications drawingSpecifications= new DrawingSpecifications();
		drawingSpecifications.setMaxCadratHeight(20);
		drawingSpecifications.setLeftMargin(0);
		drawingSpecifications.setTopMargin(0);
		drawing.setDrawingSpecifications(drawingSpecifications);
		
		drawing.setPhilologySign(true);
		BufferedImage result= null;
		try {
			result= drawing.createImage(mdcText);
		} catch (MDCSyntaxError e) {
			throw new RuntimeException(e);
		}
		return new ImageIcon(result);
	}
}
