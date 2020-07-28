/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.demo;

/**
 * How to use JSesh to create bitmaps in Java.
 * compile: javac -cp .:/FOLDER_CONTAINING/jsesh.jar TestJSeshBitmap.java
 * run: java -cp .:/FOLDER_CONTAINING/jsesh.jar TestJSeshBitmap
 * 
 * jseshGlyphs.jar and jvectClipboard-1.0.jar should be in the same folder as jsesh.jar.
 * (normally, there is no need to add them explicitely to the class path , as jsesh.jar contains the necessary 
 * information in its manifest.
 */

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.*;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.preferences.*;
import jsesh.mdcDisplayer.draw.*;
import jsesh.mdc.*;

public class SimpleBitmap {

	public static BufferedImage buildImage(String mdcText)
			throws MDCSyntaxError {
		// Create the drawing system:
		MDCDrawingFacade drawing = new MDCDrawingFacade();
		// Change the scale, choosing the cadrat height in pixels.
		drawing.setCadratHeight(60);
		// Change a number of parameters
		DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();
		PageLayout pageLayout = new PageLayout();
		pageLayout.setLeftMargin(5);
		pageLayout.setTopMargin(5);
		drawingSpecifications.setTextDirection(TextDirection.RIGHT_TO_LEFT);
		drawingSpecifications.setTextOrientation(TextOrientation.VERTICAL);	
		drawing.setDrawingSpecifications(drawingSpecifications);
		// Create the picture
		BufferedImage result = drawing.createImage(mdcText);
		return result;
	}

	public static void main(String args[]) throws MDCSyntaxError, IOException {
		// Create the picture
		BufferedImage img = buildImage("i-w-r:a-C1-m-p*t:pt");
		File f = new File("example.png");
		// save it in png (better than jpeg in this case)
		ImageIO.write(img, "png", f);
	}
}
