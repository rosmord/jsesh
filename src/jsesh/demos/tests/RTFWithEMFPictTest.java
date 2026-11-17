package jsesh.demos.tests;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;

import jsesh.graphics.emf.EMFGraphics2D;
import jsesh.graphics.generic.RandomAccessByteArray;
import jsesh.graphics.rtfBasicWriter.SimpleRTFWriter;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;

public class RTFWithEMFPictTest {

	public static void main(String[] args) throws Exception {
		MDCDrawingFacade drawingFacade= new MDCDrawingFacade();
		RandomAccessByteArray a= new RandomAccessByteArray();
		EMFGraphics2D emf2D= new EMFGraphics2D(a,640,480);
		Rectangle2D r = drawingFacade.draw("i-w-r:a-ra-m-p*t:pt", emf2D, 0, 0);
		emf2D.dispose();
		
		SimpleRTFWriter rtfWriter= new SimpleRTFWriter(new FileOutputStream(File.createTempFile("test", ".rtf")));
		rtfWriter.writeHeader();
		rtfWriter.writeEmfPicture(a.getByteArray(),(int)r.getWidth(),(int)r.getHeight());
		
		rtfWriter.writeTail();
		
	}
}
