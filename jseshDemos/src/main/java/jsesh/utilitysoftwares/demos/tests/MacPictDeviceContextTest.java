package jsesh.utilitysoftwares.demos.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.qenherkhopeshef.graphics.pict.MacPictDeviceContext;
import org.qenherkhopeshef.graphics.pict.PenMode;
import org.qenherkhopeshef.graphics.pict.MacPictDeviceContext.MPPoint;


public class MacPictDeviceContextTest {
	/**
	 * Demonstration program.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		MacPictDeviceContext macPictDeviceContext = new MacPictDeviceContext();
		MPPoint p1 = new MPPoint((short) 0, (short) 0);
		MPPoint p2 = new MPPoint((short) 200, (short) 200);
		MPPoint p3 = new MPPoint((short) 100, (short) 0);
		//macPictDeviceContext.setHilightColor(0, 0xFFFF, 0);
		macPictDeviceContext.setForegroundColor(0, 0, 0);
		macPictDeviceContext.setBackgroundColor(0xFFFF,0xFFFF,0xFFFF);
		macPictDeviceContext.setPenMode(PenMode.COPY_MODE);
		
		byte [] pattern= {(byte)0x0, (byte)0xFF, (byte)0x0, (byte)0xFF,(byte) 0x0,(byte) 0xFF,(byte) 0x0,(byte) 0xFF};
		
		macPictDeviceContext.setPenPattern(pattern);
		
		macPictDeviceContext.paintPoly(new MPPoint[] { p1, p2, p3 });
		
		macPictDeviceContext.setPenSize((short)1,(short)3);
		macPictDeviceContext.framePoly(new MPPoint[] { p1, p2, p3 });
		
		macPictDeviceContext.setPenSize((short)0x05,(short)0x005);
		macPictDeviceContext.line(new MPPoint((short)10,(short)50), new MPPoint((short)180,(short)50));
		macPictDeviceContext.lineFrom(new MPPoint((short)100, (short)200));
		macPictDeviceContext.lineFrom(new MPPoint((short)100, (short)100));
		macPictDeviceContext.lineFrom(new MPPoint((short)0, (short)200));
		macPictDeviceContext.lineFrom(new MPPoint((short)50, (short)100));
		
		macPictDeviceContext.setForegroundColor(0, 0xFFFF, 0);
		macPictDeviceContext.paintRectangle((short)100, (short)100,(short) 200,(short) 200);
		
		// Picture output.
		macPictDeviceContext.closePicture();
		FileOutputStream out = new FileOutputStream(File.createTempFile("img",
				".pict"));
		macPictDeviceContext.writeToStream(out);
		out.close();
	}
}
