/**
 * 
 */
package jsesh.test.graphics;

import java.io.IOException;
import java.io.RandomAccessFile;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessFileAdapter;

/**
 * @author rosmord
 *
 */
public class EMFExportTest {

	public static void main(String[] args) throws IOException, MDCSyntaxError {
		MDCDrawingFacade facade= new MDCDrawingFacade();
		RandomAccessFileAdapter file = new RandomAccessFileAdapter(new RandomAccessFile("/tmp/test.emf", "rw"));
		EMFGraphics2D g= new EMFGraphics2D(file, 60,20, "JSesh", "i-w-r:a-ra-m-p*t:pt");
		//EMFGraphics2D g= new EMFGraphics2D(file, 60,20, null, null);
		
		facade.draw("i-w-r:a-ra-m-p*t:pt", g, 10, 10);
		g.dispose();
	}
}
