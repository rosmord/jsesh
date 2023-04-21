/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */
package jsesh.test.graphics;

import java.awt.Dimension;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.qenherkhopeshef.graphics.emf.EMFDeviceContext;
import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.emf.EMFPoint;
import org.qenherkhopeshef.graphics.generic.RandomAccessFileAdapter;
import org.qenherkhopeshef.graphics.generic.RandomAccessStream;
import org.qenherkhopeshef.graphics.wmf.WMFConstants;

/**
 * 
 * @author rosmord
 */
public class TestEMF1 extends TestCase {

	public TestEMF1(String testName) {
		super(testName);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// TODO add test methods here. The name must begin with 'test'. For example:
	// public void testHello() {}
	public void testCreateEMF1() throws IOException {
		EMFGraphics2D g = new EMFGraphics2D(new File("emf1.emf"),
				new Dimension(20, 20), null, null);

		g.draw(new Line2D.Double(0, 0, 20, 20));
		g.dispose();
	}

	/**
	 * Test of EMFDeviceContext. We try to reproduce a picture which rendered
	 * ok. on both Windows and Mac.
	 */
	public void testCreateEMF2() throws IOException {
		final File file = new File("testDeviceContext.emf");
		file.delete();
		RandomAccessStream stream = new RandomAccessFileAdapter(file);
		EMFDeviceContext dev = new EMFDeviceContext(stream, 428, 214, null, null);
		// 160,78
		dev.setWindowOrgEx(0, 0);
		dev.setWindowExtEx(0xA2, 0x51);
		dev.setBkMode(WMFConstants.TRANSPARENT);
		dev.setTextColor(0);
		int penId = dev.createPen(WMFConstants.PS_SOLID, 100, 0x0000FF);
		dev.selectObject(penId);
		// dev.extCreatePen(2, 0x38, 0, 0x38, 0, 0x10200, 4, 0,0,0,0,0);
		// select object 80000005
		dev.beginPath();
		dev.moveToEx(0, 0);
		/*
		dev.polyBezierTo16(new EMFRectangle(0, 0, 0xFFFFFFFF, 0xFFFFFFFF),
				new EMFPoint[] { new EMFPoint(0x38,0x000C),
						new EMFPoint(0x16,0x0023), new EMFPoint(0xD0, 0x003A),
						new EMFPoint(0x4, 0x0041), new EMFPoint(0x0, 0x7C),
						new EMFPoint(0xA,0x008C), new EMFPoint(0x14,0x009D),
						new EMFPoint(0x3E,0x0099), new EMFPoint(0x4B,0x009C) });
		*/
		dev.polyBezierTo(null, new EMFPoint[] {
				new EMFPoint(0,0),
				new EMFPoint(5,100),
				new EMFPoint(100, 100),
				new EMFPoint(100,0)
				
		});
		
		dev.endPath();
		// The given bounding box is 0 0xFFFFFFFD, 0xA0, 0x4E ?
		dev.strokePath(0, 0xFFFFFFFD, 0xA0, 0x4E);
		dev.deleteObject(penId);
		dev.closeMetafile();
	}
}
