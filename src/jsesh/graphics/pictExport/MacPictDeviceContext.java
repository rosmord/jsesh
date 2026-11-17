/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.pictExport;

/**
 * Low level device to draw in mac picture format.
 * <p>
 * Generic remarks on quickdraw:
 * <ul>
 * <ul>
 * the internal representation of numbers is big endian.
 * <li> coordinates are 2 bytes long, signed (hence from -32 768 to 32767)
 * <li> the y-axis grows downward.
 * <li> colors are 48 bit long (6 bytes).
 * </ul>
 * <p>
 * Currently limited to the opcodes we actually use.
 * 
 * <p> Reference : <em>Imaging with quickdraw,</em> appendix A.
 */

public class MacPictDeviceContext implements MacPictOpcodes {

	/**
	 * Points for quickdraw.
	 * 
	 * @author rosmord
	 * 
	 */
	public static class Point {
		public short x, y;

		/**
		 * @param x
		 * @param y
		 */
		public Point(short x, short y) {
			this.x = x;
			this.y = y;
		}

	};

	/**
	 * Sets the foreground color.
	 * @param red : an integer between 0 and 64735
	 * @param green : an integer between 0 and 64735
	 * @param blue : an integer between 0 and 64735
	 */
	public void setForegroundColor(int red, int green, int blue) {
		
	}
	
	/**
	 * Paint a polygon with the current fill color.
	 * @param points
	 */
	public void paintPoly(Point [] points) {

	}

	/**
	 * Write the picture header.
	 *
	 */
	private void writeHeader() {
		// 0x200 '0' bytes
		// address 0x200 : Picture size (currently 0) (2 bytes). That's the file size, less 512. 
		// bounding rectangle of picture at 72 ? dpi (4*2 bytes)
		// Version opcode VERSION_OP (2)
		// Header opcode HEADER_OP (2)
		// Version : "-2" (2)
		// 0 (2)
		// best horizontal resolution (72 dpi ?) 0x0048 0000 (write in big endian ?) (4)
		// idem for vertical resolution (4)
		// source rectangle (8 bytes) (in the example, bounding translated by 2 ?)
		// 0 (2)
	}
	
	/**
	 * This method must be called at the end of the drawing session.
	 */
	public void closePicture() {
		// Sets the picture size at address 0x200.
	}
	
	/**
	 * Demonstration.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
}
