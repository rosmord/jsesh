/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package org.qenherkhopeshef.graphics.pict;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Low level device to draw in mac picture format.
 * <p>
 * Generic remarks on quickdraw:
 * <ul>
 * <ul>
 * <li> the internal representation of numbers is big endian.
 * <li> coordinates are 2 bytes long, signed (hence from -32 768 to 32767)
 * <li> the y-axis grows downward.
 * <li> colors are 48 bit long (6 bytes).
 * </ul>
 * <p>
 * Currently limited to the opcodes we actually use.
 * 
 * <p>
 * It might be interesting to use the Region system of QuickDraw, instead of our
 * homebrew system, but the codes are not well documented. We'll have a look,
 * though.
 * 
 * <p>
 * General note for the reader: Quickdraw has the rather habit of specifying
 * ordinate before absisse. Hence, all actual inner commands are (y,x) instead
 * of (x,y). We have decided that externally, they would follow the usual
 * practices, that is, the user of this class will specify x and then y.
 * 
 * <p>
 * Reference : <em>Imaging with quickdraw,</em> appendix A.
 */

public class MacPictDeviceContext implements MacPictOpcodes {

	//public static final int DEFAULT_RESOLUTION = 511; 

	public static final int DEFAULT_RESOLUTION = 2048; 

	private SimpleByteBuffer buffer = new SimpleByteBuffer();

	private boolean boundingBoxComputed = true;

	private MPRectangle boundingBox = new MPRectangle();

	/**
	 * Inner bounding box. If null, compute from bounding box.
	 */
	private MPRectangle innerBoundingBox = null;

	/**
	 * Clip box. If null, compute from bounding box.
	 */
	private MPRectangle clipBox = null;

	/**
	 * Picture resolution.
	 */
	private int dpi = DEFAULT_RESOLUTION;

	/**
	 * Position in the file of the inner bounding box.
	 */
	private int innerBBPos;

	/**
	 * Position of clipping region.
	 */
	private int clipPos;

	/**
	 * Position of the bounding box.
	 */
	//private int boundingBoxPos;

	/**
	 * Position of the resolution information.
	 */
	private int resolutionPos;

	public MacPictDeviceContext() {
		writeHeader();
	};
	
	/**
	 * Create a device with a fixed bounding box.
	 * @param minx
	 * @param miny
	 * @param maxx
	 * @param maxy
	 */
	public  MacPictDeviceContext(short minx, short miny, short maxx, short maxy) {
		this();
		setBoundingBox(minx, miny, maxx, maxy);
	}

	/**
	 * This method must be called at the end of the drawing session.
	 */
	public void closePicture() {
		buffer.writeShortBigEndian(MacPictOpcodes.OP_END_PIC);
		// Sets the picture size at address 0x200.
		buffer.seek(0x200);
		buffer.writeShortBigEndian(buffer.getSize() - 0x200);
		//buffer.writeShortBigEndian(buffer.getSize() - 0x208);
		// Write the bounding box

		buffer.writeShortBigEndian((short) boundingBox.getMinY());
		buffer.writeShortBigEndian((short) boundingBox.getMinX());
		buffer.writeShortBigEndian((short) boundingBox.getMaxY());
		buffer.writeShortBigEndian((short) boundingBox.getMaxX());

		if (innerBoundingBox == null) {

			setInnerBoundingBox((short) (boundingBox.getMinX()),
					(short) (boundingBox.getMinY()), (short) (boundingBox
							.getMaxX()), (short) (boundingBox.getMaxY()));
		}
		if (clipBox == null) {

			setClipBox((short) (boundingBox.getMinX()), (short) (boundingBox
					.getMinY()), (short) (boundingBox.getMaxX()),
					(short) (boundingBox.getMaxY()));
		}
	}

	public void fillPoly(MPPoint[] points) {
		int polyCommand = MacPictOpcodes.FILL_POLY;
		processPoly(points, polyCommand);
	}

	public void framePoly(MPPoint[] points) {
		int polyCommand = MacPictOpcodes.FRAME_POLY;
		processPoly(points, polyCommand);
	}

	/**
	 * Return the content of the device context as a byte array.
	 * 
	 * @return
	 * @see org.qenherkhopeshef.graphics.pict.SimpleByteBuffer#getAsArray()
	 */

	public byte[] getAsArray() {
		return buffer.getAsArray();
	}

	/**
	 * In RTF files, the first 512 null bytes are not included.
	 * 
	 * @return
	 */
	public byte[] getAsArrayForRTF() {
		return buffer.getAsArray(0x200);
	}

	/**
	 * gets the picture resolution.
	 * 
	 * @return the dpi
	 */
	public int getDpi() {
		return dpi;
	}

	/**
	 * Draws a line between p1 and p2.
	 * 
	 * @param p1
	 * @param p2
	 */
	public void line(MPPoint p1, MPPoint p2) {
		buffer.writeShortBigEndian(MacPictOpcodes.LINE);
		writePoint(p1);
		writePoint(p2);
	}

	/**
	 * Draws a line toward a point.
	 * 
	 * @param p
	 */
	public void lineFrom(MPPoint p) {
		buffer.writeShortBigEndian(MacPictOpcodes.LINE_FROM);
		writePoint(p);
	}

	/**
	 * Paint a polygon with the current fill color.
	 * 
	 * @param points
	 */

	public void paintPoly(MPPoint[] points) {
		int polyCommand = MacPictOpcodes.FILL_POLY;
		processPoly(points, polyCommand);
	}

	public void paintRectangle(MPPoint minp, MPPoint maxp) {
		buffer.writeShortBigEndian(MacPictOpcodes.PAINT_RECT);
		// Actual mac order is top,left,bottom,right.
		buffer.writeShortBigEndian(minp.y);
		buffer.writeShortBigEndian(minp.x);
		buffer.writeShortBigEndian(maxp.y);
		buffer.writeShortBigEndian(maxp.x);
	}

	public void paintRectangle(short minx, short miny, short maxx, short maxy) {
		buffer.writeShortBigEndian(MacPictOpcodes.PAINT_RECT);
		// Actual mac order is top,left,bottom,right.
		buffer.writeShortBigEndian(miny);
		buffer.writeShortBigEndian(minx);
		buffer.writeShortBigEndian(maxy);
		buffer.writeShortBigEndian(maxx);

	}

	public void setBackgroundColor(int red, int green, int blue) {
		buffer.writeShortBigEndian(MacPictOpcodes.RGB_BK_COL);
		buffer.writeShortBigEndian(red);
		buffer.writeShortBigEndian(green);
		buffer.writeShortBigEndian(blue);
	}

	public void setBackgroundColor(MPColor color) {
		setBackgroundColor(color.red, color.green, color.blue);
	}

	public void setBoundingBox(short minx, short miny, short maxx, short maxy) {
		boundingBox= new MPRectangle(minx, miny, maxx, maxy);
		boundingBoxComputed= false;
	}

	/*
	 * public void moveTo(Point p) {
	 * buffer.writeShortBigEndian(MacPictOpcodes.); writePoint(p); }
	 */

	public void setClipBox(short minX, short minY, short maxX, short maxY) {
		this.clipBox = new MPRectangle(minX, minY, maxX, maxY);
		int oldPos = buffer.getPos();
		buffer.seek(clipPos);
		buffer.writeShortBigEndian(minY);
		buffer.writeShortBigEndian(minX);
		buffer.writeShortBigEndian(maxY);
		buffer.writeShortBigEndian(maxX);
		buffer.seek(oldPos);
	}

	/**
	 * Sets the picture resolution.
	 * 
	 * @param dpi
	 *            the dpi to set
	 */
	public void setDpi(int dpi) {
		this.dpi = dpi;
		int currentPos = buffer.getPos();
		buffer.seek(resolutionPos);
		// Resolution
		// Horizontal
		buffer.writeIntBigEndian(getDpi() << 16);
		// Vertical.
		buffer.writeIntBigEndian(getDpi() << 16);
		buffer.seek(currentPos);
	}

	/**
	 * Sets the foreground color.
	 * 
	 * Note that the colour scale is 48 bits long.
	 * 
	 * @param red :
	 *            an integer between 0 and 64735
	 * @param green :
	 *            an integer between 0 and 64735
	 * @param blue :
	 *            an integer between 0 and 64735
	 */
	public void setForegroundColor(int red, int green, int blue) {
		buffer.writeShortBigEndian(MacPictOpcodes.RGB_FG_COL);
		buffer.writeShortBigEndian(red);
		buffer.writeShortBigEndian(green);
		buffer.writeShortBigEndian(blue);
	}

	/**
	 * Utility method to use
	 * 
	 * @param color
	 */
	public void setForegroundColor(MPColor color) {
		setForegroundColor(color.red, color.green, color.blue);
	}

	public void setHilightColor(int red, int green, int blue) {
		buffer.writeShortBigEndian(MacPictOpcodes.HILITE_COLOR);
		buffer.writeShortBigEndian(red);
		buffer.writeShortBigEndian(green);
		buffer.writeShortBigEndian(blue);
	}

	// Understand better this one.

	public void setInnerBoundingBox(short minX, short minY, short maxX,
			short maxY) {
		innerBoundingBox = new MPRectangle(minX, minY, maxX, maxY);
		int oldPos = buffer.getPos();
		buffer.seek(innerBBPos);
		buffer.writeShortBigEndian(minY);
		buffer.writeShortBigEndian(minX);
		buffer.writeShortBigEndian(maxY);
		buffer.writeShortBigEndian(maxX);
		buffer.seek(oldPos);
	}

	/**
	 * Sets pen mode. See possible values in PenMode
	 * 
	 * @param mode
	 * @see PenMode
	 */
	public void setPenMode(short mode) {
		buffer.writeShortBigEndian(MacPictOpcodes.PN_MODE);
		buffer.writeShortBigEndian(mode);
	}

	public void setPenPattern(byte[] pattern) {
		buffer.writeShortBigEndian(MacPictOpcodes.PN_PATTERN);
		for (int i = 0; i < pattern.length; i++)
			buffer.writeByte(pattern[i]);
	}

	public void setPenSize(short xsize, short ysize) {
		buffer.writeShortBigEndian(MacPictOpcodes.PN_SIZE);
		buffer.writeShortBigEndian(ysize);
		buffer.writeShortBigEndian(xsize);
	}

	public void writeToStream(OutputStream outputStream) throws IOException {
		this.buffer.writeToStream(outputStream);
		outputStream.close();
	}

	private void addToBoundingBox(MPPoint p) {
		addToBoundingBox(p.x, p.y);
	}

	private void addToBoundingBox(short x, short y) {
		if (boundingBoxComputed) {
			if (boundingBox == null) {
				boundingBox = new MPRectangle(x, y, (short) x, (short) y);
			} else
				boundingBox.add(x, y);
		}
	}
	
	private void processPoly(MPPoint[] points, int polyCommand) {
		buffer.writeShortBigEndian(polyCommand);
		// Size of polygon (in bytes) : 2 *(1 + 4 + 2*points.size)
		buffer.writeShortBigEndian(2 * (5 + 2 * points.length));
		if (points.length > 0) {
			// bounding MPRectangle (4 short)
			short minx = points[0].x, miny = points[0].y, maxx = points[0].x, maxy = points[0].y;

			// compute BB
			for (int i = 1; i < points.length; i++) {
				short x = points[i].x;
				short y = points[i].y;
				if (minx > x)
					minx = x;
				if (maxx < x)
					maxx = x;
				if (miny > y)
					miny = y;
				if (maxy < y)
					maxy = y;
			}
			// Write BB
			buffer.writeShortBigEndian(miny);
			buffer.writeShortBigEndian(minx);
			buffer.writeShortBigEndian(maxy);
			buffer.writeShortBigEndian(maxx);
			// draw points
			for (int i = 0; i < points.length; i++) {
				writePoint(points[i]);
				addToBoundingBox(points[i]);
			}
		} else {
			// Empty Bounding box for empty MPRectangle
			buffer.writeShortBigEndian(0);
			buffer.writeShortBigEndian(0);
			buffer.writeShortBigEndian(0);
			buffer.writeShortBigEndian(0);
		}
	}

	/**
	 * Write the picture header.
	 * 
	 * <p>
	 * Structure thereof:
	 * <ul>
	 * <li> 512 (i.e. 0x200) '0' bytes
	 * <li> at address 0x200, Picture size (currently 0) (2 bytes). That's the
	 * file size, less 512.
	 * <li> bounding MPRectangle of picture at 72 ? dpi (4*2 bytes)
	 * <li> Version op opcode VERSION_OP (2)
	 * <li> Version opcode VERSION (2)
	 * <li> Header opcode HEADER_OP (2)
	 * <li> Version : "-2" (2)
	 * <li> 0 (2)
	 * <li> best horizontal resolution (72 dpi) 0x0048 0000 (write in big
	 * endian) (4)
	 * <li> idem for vertical resolution (4)
	 * <li> source MPRectangle (8 bytes) (in the example, bounding translated by
	 * 2 ?)
	 * <li> 0 (2)
	 * </ul>
	 */

	private void writeHeader() {
		buffer.seek(0);
		buffer.fill((byte) 0, 0, 512);
		buffer.writeShortBigEndian(0); // Size of the picture
		//int boundingBoxPos = buffer.getPos();
		// Bounding box
		buffer.writeShortBigEndian(0); // bounding box
		buffer.writeShortBigEndian(0); // bb
		buffer.writeShortBigEndian(0); // bb
		buffer.writeShortBigEndian(0); // bb

		buffer.writeShortBigEndian(MacPictOpcodes.VERSION_OP);
		buffer.writeShortBigEndian(MacPictOpcodes.VERSION);
		buffer.writeShortBigEndian(MacPictOpcodes.HEADER_OP);
		// 'true' Header
		buffer.writeShortBigEndian(-2); // Extended version 2
		buffer.writeShortBigEndian(0); // Reserved value

		resolutionPos = buffer.getPos();
		// Resolution
		buffer.writeIntBigEndian(getDpi() << 16 + 0);
		buffer.writeIntBigEndian(getDpi() << 16 + 0);

		innerBBPos = buffer.getPos();
		// bounding MPRectangle (fixed point). Normally a MPRectangle included
		// in the bounding box.
		buffer.writeShortBigEndian(0); // bounding box
		buffer.writeShortBigEndian(0); // bb
		buffer.writeShortBigEndian(0); // bb
		buffer.writeShortBigEndian(0); // bb

		// Reserved
		buffer.writeShortBigEndian(0);
		buffer.writeShortBigEndian(0);

		// Ok.

		// Clipping region
		// THIS IS MANDATORY !!!
		buffer.writeShortBigEndian(MacPictOpcodes.CLIP);
		buffer.writeShortBigEndian(10); // size
		clipPos = buffer.getPos();
		buffer.writeShortBigEndian(0); // bounding box
		buffer.writeShortBigEndian(0); // bb
		buffer.writeShortBigEndian(0); // bb
		buffer.writeShortBigEndian(0); // bb

	}

	private void writePoint(MPPoint p) {
		buffer.writeShortBigEndian(p.y);
		buffer.writeShortBigEndian(p.x);

	}

	/**
	 * 48-bit color.
	 * 
	 * @author rosmord
	 * 
	 */
	public static class MPColor {
		public int red, green, blue;

		public MPColor(int red, int green, int blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
	}

	/**
	 * Points for quickdraw.
	 * 
	 * @author rosmord
	 * 
	 */
	public static class MPPoint {
		public short x, y;

		/**
		 * @param x
		 * @param y
		 */
		public MPPoint(short x, short y) {
			this.x = x;
			this.y = y;
		}

	}

	public static class MPRectangle {
		private short minX, minY, maxX, maxY;

		public MPRectangle() {

		}

		public MPRectangle(short minx, short miny, short maxx, short maxy) {
			super();
			this.minX = minx;
			this.minY = miny;
			this.maxX = maxx;
			this.maxY = maxy;
		}

		/**
		 * Enlarge the bounding box to include this point.
		 * 
		 * @param x
		 * @param y
		 */
		public void add(short x, short y) {
			if (minX > x)
				minX = x;
			if (minY > y)
				minY = y;
			if (maxX < x)
				maxX = x;
			if (maxY < y)
				maxY = y;
		}

		/**
		 * @return the maxX
		 */
		public short getMaxX() {
			return maxX;
		}

		/**
		 * @return the maxY
		 */
		public short getMaxY() {
			return maxY;
		}

		/**
		 * @return the minX
		 */
		public short getMinX() {
			return minX;
		}

		/**
		 * @return the minY
		 */
		public short getMinY() {
			return minY;
		}

		/**
		 * @param maxX
		 *            the maxX to set
		 */
		public void setMaxX(short maxX) {
			this.maxX = maxX;
		}

		/**
		 * @param maxY
		 *            the maxY to set
		 */
		public void setMaxY(short maxY) {
			this.maxY = maxY;
		}

		/**
		 * @param minX
		 *            the minX to set
		 */
		public void setMinX(short minX) {
			this.minX = minX;
		}

		/**
		 * @param minY
		 *            the minY to set
		 */
		public void setMinY(short minY) {
			this.minY = minY;
		}
	}

}
