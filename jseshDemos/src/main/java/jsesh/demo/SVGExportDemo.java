/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.demo;

import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;

import org.qenherkhopeshef.graphics.svg.SVGGraphics2D;
import org.qenherkhopeshef.graphics.utils.DoubleDimensions;


/**
 * Demonstrate a simple export of SVG graphics from JSesh.
 * @author rosmord
 *
 */
public class SVGExportDemo {
	public static void main(String[] args) throws IOException, MDCSyntaxError {
		String mdc= "r:a-G7-m-t:A-p*t:pt";
		MDCDrawingFacade facade= new MDCDrawingFacade();
		Rectangle2D dims = facade.getBounds(mdc, 0, 0);

		// StringWriter writer= new StringWriter();
		FileWriter writer= new FileWriter("foo.svg");
		SVGGraphics2D g= new SVGGraphics2D(writer, new DoubleDimensions(dims.getWidth(), dims.getHeight()));
		facade.draw(mdc, g, 0, 0);
		g.dispose();
	}
}
