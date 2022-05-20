package jsesh.utilitysoftwares.demos.tests;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;

import jsesh.mdcDisplayer.draw.MDCDrawingFacade;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

public class RTFWithEMFPictTest {

	public static void main(String[] args) throws Exception {
		MDCDrawingFacade drawingFacade= new MDCDrawingFacade();
		RandomAccessByteArray a= new RandomAccessByteArray();
		EMFGraphics2D emf2D= new EMFGraphics2D(a,640,480,null,null);
		Rectangle2D r = drawingFacade.draw("i-w-r:a-ra-m-p*t:pt", emf2D, 0, 0);
		emf2D.dispose();
		
		SimpleRTFWriter rtfWriter= new SimpleRTFWriter(new FileOutputStream(File.createTempFile("test", ".rtf")));
		rtfWriter.writeHeader();
		rtfWriter.writeEmfPicture(a.getByteArray(),(int)r.getWidth(),(int)r.getHeight());
		
		rtfWriter.writeTail();
		
	}
}
