/**
 * 
 */
package jsesh.test.graphics;

import java.io.RandomAccessFile;

import org.qenherkhopeshef.graphics.emf.EMFDeviceContext;
import org.qenherkhopeshef.graphics.emf.EMFPoint;
import org.qenherkhopeshef.graphics.generic.RandomAccessFileAdapter;
import org.qenherkhopeshef.graphics.generic.RandomAccessStream;
import org.qenherkhopeshef.graphics.wmf.WMFConstants;

/**
 * @author rosmord
 *
 */
public class EMFDeviceContextTests {

	public static void main(String[] args) throws Exception {
		RandomAccessStream out= new RandomAccessFileAdapter(new RandomAccessFile("/tmp/demo.emf","rw"));
		EMFDeviceContext emf= new EMFDeviceContext(out,2000,1000, "me", "my soft");
		//emf.setMapMode(WMFConstants.MM_TWIPS);
		//emf.setWindowOrg( 0,  0);
		//emf.setWindowExt( 1000, 1000);
		short a = emf.createBrush(0, 0xFF, WMFConstants.BS_SOLID);
		///short a = emf.createBrush(0, 0, WMFConstants.BS_SOLID);
		//long a= emf.createBrush(0,0,0);
		emf.selectObject(a);
		
		emf.beginPath();
		emf.moveToEx(100, 100);
		
		EMFPoint[] controls= new EMFPoint[] {
				new EMFPoint(100, 1000),
				new EMFPoint(1100, 1000),
				new EMFPoint(1100, 100)
		};
		
		emf.polyBezierTo(null,controls);
		emf.lineTo(100, 100);
		emf.closeFigure();
				
		emf.moveToEx(300, 300);
		controls= new EMFPoint[] {
				new EMFPoint(300, 400),
				new EMFPoint(900, 400),
				new EMFPoint(900, 300),
				new EMFPoint(900, 200),
				new EMFPoint(300, 200),
				new EMFPoint(300, 300)
		};
		emf.polyBezierTo(null,controls);
		emf.closeFigure();
		
		emf.endPath();
		emf.fillPath();
		emf.deleteObject(a);
		emf.closeMetafile();		

	}
	
	public static void drawTriangle() throws Exception {
		RandomAccessStream out= new RandomAccessFileAdapter(new RandomAccessFile("/tmp/demo.emf","rw"));
		EMFDeviceContext emf= new EMFDeviceContext(out,400,400, null, null);
		//emf.setMapMode(WMFConstants.MM_TWIPS);
		emf.setWindowOrg( 0,  0);
		emf.setWindowExt( 1000, 1000);
		short a = emf.createBrush(0, 0xFF, WMFConstants.BS_SOLID);
		///short a = emf.createBrush(0, 0, WMFConstants.BS_SOLID);
		//long a= emf.createBrush(0,0,0);
		emf.selectObject(a);
		
		emf.beginPath();
		emf.moveToEx(0, 0);
		emf.lineTo(1000, 5000);
		
		EMFPoint[] controls= new EMFPoint[] {
				new EMFPoint(200, 4000),
				new EMFPoint(10000, 2000),
				new EMFPoint(200, 2000)
		};
		
		//emf.polyBezierTo(null,controls);
		emf.lineTo(2000, 0);
		emf.closeFigure();
		
		emf.moveToEx(1000, 4000);
		emf.lineTo(500, 2000);
		emf.lineTo(1500, 2000);
		
		
		emf.endPath();
		emf.fillPath();
		emf.deleteObject(a);
		emf.closeMetafile();		
	}
}
