package jsesh.utilitysoftwares.demos.tests;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;

import jsesh.mdcDisplayer.draw.MDCDrawingFacade;

import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

public class RTFWithMacPictTest {

	public static void main(String[] args) throws Exception {
		MDCDrawingFacade drawingFacade= new MDCDrawingFacade();
		MacPictGraphics2D macPictGraphics2D= new MacPictGraphics2D();
		Rectangle2D r = drawingFacade.draw("i-w-r:a-ra-m-p*t:pt", macPictGraphics2D, 0, 0);
		macPictGraphics2D.dispose();
		
		SimpleRTFWriter rtfWriter= new SimpleRTFWriter(new FileOutputStream(File.createTempFile("test", ".rtf")));
		rtfWriter.writeHeader();
		rtfWriter.writeMacPictPicture(macPictGraphics2D.getAsArrayForRTF(),(int)r.getWidth(),(int)r.getHeight());
		
		rtfWriter.writeTail();
		
	}
}
