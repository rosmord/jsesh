/**
 * 
 */
package jsesh.demos.tests;

import java.io.IOException;
import java.io.RandomAccessFile;

import jsesh.graphics.emf.EMFGraphics2D;
import jsesh.graphics.generic.RandomAccessFileAdapter;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;

/**
 * @author rosmord
 *
 */
public class EMFExportTest {

	public static void main(String[] args) throws IOException, MDCSyntaxError {
		MDCDrawingFacade facade= new MDCDrawingFacade();
		RandomAccessFileAdapter file = new RandomAccessFileAdapter(new RandomAccessFile("/tmp/test.emf", "rw"));
		EMFGraphics2D g= new EMFGraphics2D(file, 60,20);
		facade.draw("i-w-r:a-ra-m-p*t:pt", g, 10, 10);
		g.dispose();
	}
}
