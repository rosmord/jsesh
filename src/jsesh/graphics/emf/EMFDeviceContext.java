package jsesh.graphics.emf;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;

import jsesh.graphics.generic.RandomAccessStream;
import jsesh.graphics.generic.RandomAccessStreamUtils;
import jsesh.graphics.wmf.WMFConstants;


/*
 * Created on 4 nov. 2004
 *
 * This file is distributed under the LGPL.
 */

/**
 * A low level implementation of the emf vector file device.
 * 
 * <p> The default scale is 1 device unit = 1/100 mm. It can be changed using setReferenceRatios().
 * 
 * To avoid the burden of encapsulating each integer value in an object, we have
 * decided to use the following conventions:
 * 
 * <p> In contrast with WMF, the order of coordinates in EMF is the usual one. X, then Y.
 * (U means unsigned, S means signed, and the size is in bits).
 * <ul>
 * <li>U8 is represented by a char.
 * <li>S16 is represented by a short.
 * <li>U16 is represented by an int.
 * <li>S32 is represented by an long.
 * <li>U32 is represented by an long.
 * <li>Thanks to these choices, computations are straightforward.
 * </ul>
 * 
 * @author S. Rosmorduc
 * 
 */

public class EMFDeviceContext implements EMFOpCodes {

	/**
	 * objects is a table of boolean, which remembers if objects are used (true)
	 * or not (false).
	 */
	private ArrayList objects= new ArrayList();

	private RandomAccessStream out;

	private EMFHeader header;
	
	/**
	 * Size of a device point, in mm.
	 */
	private double devicePointSize= 0.05;  

	public EMFDeviceContext(RandomAccessStream stream, Dimension dims)
			throws IOException {
		this(stream, (long)dims.getWidth(), (long)dims.getHeight());
	}

	/**
	 * Create a metafile in a RandomAccessStream, for instance a
	 * RandomAccessByteArray or a RandomAccessFile.
	 * 
	 * @param stream
	 * @param width : width in device points
	 * @param height : height in mm
	 * @throws IOException
	 */

	public EMFDeviceContext(RandomAccessStream stream, long width, long height)
			throws IOException {
		initLowLevelGraphics(stream, width, height);
	}

	public void beginPath() throws IOException {
		addCommand(EMR_BEGINPATH);
	}

	public void closeMetafile() throws IOException {
		// We need to specify that there is no palette
		addCommandABC(EMR_EOF,0,0x10,0x14);
		header.write();
		out.close();
	}

	public void deleteObject(int nobj) throws IOException {
		objects.set(nobj, Boolean.FALSE);
		addCommandX(EMR_DELETEOBJECT, nobj);
	}

	public void endPath() throws IOException {
		addCommand(EMR_ENDPATH);
	}

	public void fillPath() throws IOException {
		addCommand(EMR_FILLPATH);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @throws IOException
	 */
	public void lineTo(long x, long y) throws IOException {
		addCommandXY(EMR_LINETO, x, y);
	}

	/**
	 * Moves the current point.
	 * 
	 * @param x
	 * @param y
	 * @throws IOException
	 */
	public void moveToEx(long x, long y) throws IOException {
		addCommandXY(EMR_MOVETOEX, x, y);
	}

	/**
	 * 
	 * @param boundingBox can be null, in which case it is computed.
	 * @param controls
	 * @throws IOException
	 */
	public void polyBezierTo(EMFRectangle boundingBox, EMFPoint controls[]) throws IOException {
		addRecord(EMR_POLYBEZIERTO, controls.length * 2 + 1 + 4);
		// Write the bounds 
		if (boundingBox== null) {
			boundingBox= new EMFRectangle();
			for (int i=0; i < controls.length; i++) {
				if (controls[i].getX() > boundingBox.getMaxx())
					boundingBox.setMaxx(controls[i].getX());
				if (controls[i].getX() < boundingBox.getMinx())
					boundingBox.setMinx(controls[i].getX());
				
				if (controls[i].getY() > boundingBox.getMaxy())
					boundingBox.setMaxy(controls[i].getY());
				if (controls[i].getY() < boundingBox.getMiny())
					boundingBox.setMiny(controls[i].getY());
				
			}
			
		}
		
		RandomAccessStreamUtils.writeU32(out, boundingBox.getMinx());
		RandomAccessStreamUtils.writeU32(out, boundingBox.getMiny());
		RandomAccessStreamUtils.writeU32(out, boundingBox.getMaxx());
		RandomAccessStreamUtils.writeU32(out, boundingBox.getMaxy());
		
		
		RandomAccessStreamUtils.writeU32(out, controls.length);
		//for (int i = controls.length-1; i>= 0; i--) {
		for (int i = 0; i< controls.length; i++) {
			RandomAccessStreamUtils.writeU32(out, controls[i].getX());
			RandomAccessStreamUtils.writeU32(out, controls[i].getY());
		}
	}
	
	/**
	 * Create a new brush and returns its number.
	 * @param hatch (a hatch style from WMFConstants)
	 * @param colour a 24bit RGB colour.
	 * @param style (a brush style from WMFConstants)
	 * @return the object id for this brush.
	 * @throws IOException
	 * @see WMFConstants
	 */
	public short createBrush(long hatch, long colour, long style) throws IOException {
		addRecordWithByteNumber(EMR_CREATEBRUSHINDIRECT, 16);
		short objectId= allocObject();
		// A (reasonable) guess from the available data. I wish the EMF format was really documented !
		RandomAccessStreamUtils.writeU32(out, objectId);
		// style, colour, hatch in this order. color is RGB, with 0 padding in the end.
		RandomAccessStreamUtils.writeU32(out, style);
		// write the colour
		writeColor((int)colour);
		RandomAccessStreamUtils.writeU32(out, hatch);
		return objectId;
	}
	
	
	/**
	 * Create a pen and return its object id.
	 * @param penStyle a pen style (from {@link WMFConstants})
	 * @param width pen width
	 * @param colour pen colour.
	 * @return
	 * @throws IOException 
	 */
	public short createPen(int penStyle, int width, long colour) throws IOException {
		addRecordWithByteNumber(EMR_CREATEPEN, 16);
		short objectId= allocObject();
		// A (reasonable) guess from the available data. I wish the EMF format was really documented !
		RandomAccessStreamUtils.writeU32(out, objectId);
		// style, colour, hatch in this order. color is RGB, with 0 padding in the end.
		RandomAccessStreamUtils.writeU32(out, penStyle);
		// width
		RandomAccessStreamUtils.writeU32(out, width);
		// write the colour
		writeColor((int)colour);
		return objectId;
	}
	
	/**
	 * write a colour (takes 4 bytes).
	 * @param colour a colour, in the form 0xRRGGBB
	 * @throws IOException 
	 */
	private void writeColor(int colour) throws IOException {
		int red= (colour & 0xFF0000) >> 16;
		int green= (colour & 0xFF00) >> 8;
		int blue= (colour & 0xFF);
		RandomAccessStreamUtils.writeU8(out,red) ;
		RandomAccessStreamUtils.writeU8(out,green) ;
		RandomAccessStreamUtils.writeU8(out,blue) ;
		RandomAccessStreamUtils.writeU8(out,0) ;
	}

	/**
	 * Sets the device coordinate system to use.
	 * We suggest TWIPS 1/20 of a point, or 1/1440 of an inch 
	 * @param mapMode one of the MM codes from the WMFConstants interface. 
	 * @throws IOException 
	 * @see jsesh.graphics.wmf.WMFConstants
	 */
	public void setMapMode(long mapMode) throws IOException {
		addCommandX(EMR_SETMAPMODE, mapMode);
	}
	
	/**
	 * Add a EMF record.
	 * 
	 * @param func
	 * @throws IOException
	 */
	private void addCommand(long func) throws IOException {
		addRecord(func, 0);
	}
	
	/**
	 * Add a new command with one argument.
	 * @param emr_deleteobject
	 * @param nobj
	 * @throws IOException 
	 */
	
	private void addCommandX(long emr_code, long arg0) throws IOException {
		addRecord(emr_code, 1);
		RandomAccessStreamUtils.writeU32(out, arg0);
	}

	/**
	 * Add a new command with two argument.
	 * 
	 * @param emr_deleteobject
	 * @param nobj
	 * @throws IOException 
	 */
	
	private void addCommandXY(long emr_code, long a, long b) throws IOException {
		addRecord(emr_code, 2);
		RandomAccessStreamUtils.writeU32(out, a);
		RandomAccessStreamUtils.writeU32(out, b);
		
	}
	
	private void addCommandABC(long emr_code, long a, long b, long c) throws IOException {
		addRecord(emr_code, 3);
		RandomAccessStreamUtils.writeU32(out, a);
		RandomAccessStreamUtils.writeU32(out, b);
		RandomAccessStreamUtils.writeU32(out, c);
	}
	
	/**
	 * Add an EMF record, with the number of DWORD arguments.
	 * 
	 * @param func :
	 *            a DWORD command code.
	 * @param numberOfparameters :
	 *            the number of parameters
	 * @throws IOException
	 */
	private void addRecord(long func, long numberOfparameters) throws IOException {
		addRecordWithByteNumber(func, numberOfparameters*4);
	}

	/**
	 * Adds a EMF record, with the size of the arguments in number of BYTES.
	 * @param func
	 * @param byteNumber
	 */
	private void addRecordWithByteNumber(long func, long byteNumber) {
		long size = 8 + byteNumber;
		/* Update header values */
		header.numberOfRecords++;
		header.metafileSize+= size;
		/* Start writing data */
		RandomAccessStreamUtils.writeU32(out, func);
		RandomAccessStreamUtils.writeU32(out, size);
	}
	
	/**
	 * find and return a free object.
	 * <p> The returned index starts at 1.
	 * @return the object index.
	 */

	private short allocObject() {
		short result;
		int i;
		for (i = 1; i < objects.size()
				&& ((Boolean) objects.get(i)).booleanValue(); i++)
			;
		if (i == objects.size()) {
			result = (short) i;
			objects.add(Boolean.TRUE);
		} else {
			result = (short) i;
			objects.set(i, Boolean.TRUE);
		}
		header.numOfHandles++;
		return result;
	}

	/**
	 * @param stream
	 * @param width width in device points
	 * @param height height in device points
	 * @throws IOException 
	 */
	private void initLowLevelGraphics(RandomAccessStream stream, long width,
			long height) throws IOException {
		objects.add(new Integer(0)); // Dummy value. We ge sure that indexes start at 1.
		// As objects contains booleans, if there's a bug, this code will cause an exception.
		this.out= stream;
		header = new EMFHeader();
		header.boundsRight= (int)Math.ceil(width);
		header.boundsBottom= (int)Math.ceil(height);
		setDevicePointSize(0.01);
		header.write();
		
	}


	/**
	 * Sets the scale for this picture.
	 * <p> The scales are expressed as two couples dXPixels/dxMM and dyPixel/dyMM.
	 * What's important is the corresponding ratio.
	 * 
	 * @param dXpixels
	 * @param dyPixels
	 * @param dxMM
	 * @param dyMM
	 */
	public void setReferenceRatios(long dXpixels, long dyPixels, long dxMM, long dyMM) {
		header.widthDevPixels= dXpixels;
		header.heightDevPixels= dyPixels;

		header.widthDevMM= dxMM;
		header.heightDevMM= dyMM;
	}
	
	/**
	 * Returns the length (in mm) of one horizontal device unit. 
	 * @return
	 */
	public double getXScale() {
		return (double)header.widthDevMM / header.widthDevPixels;
	}

	/**
	 * Returns the length (in mm) of one vertical device unit. 
	 * @return
	 */

	public double getYScale() {
		return (double)header.heightDevMM / header.heightDevPixels;
	}
	
	/**
	 * Sets the size of device point, in mm.
	 * Default value is 0.01mm.
	 * @param devicePointSize the devicePointSize to set
	 */
	public void setDevicePointSize(double devicePointSize) {
		this.devicePointSize = devicePointSize;
		header.frameRight= (long)Math.ceil(header.boundsRight*devicePointSize*100);
		header.frameBottom= (long)Math.ceil(header.boundsBottom*devicePointSize*100);
		header.widthDevMM=  (long)Math.ceil(header.widthDevPixels*devicePointSize);
		header.heightDevMM=  (long)Math.ceil(header.heightDevPixels*devicePointSize);
	}
	
	/**
	 * @return the devicePointSize
	 */
	public double getDevicePointSize() {
		return devicePointSize;
	}
	
	class EMFHeader {
		int recordType = 1; /* EMR_HEADER */

		int recordSize= 88; /* Base Size of the header in bytes. Can increase if there's a header. */
		
		/*
		 * The following bounds could be computed, or explicitely set.
		 */
		
		long boundsLeft=0; /* Left inclusive bounds (in device units) */

		long boundsTop=0; /* Top inclusive bounds */

		long boundsRight=1000; /* Right inclusive bounds */

		long boundsBottom= 1000; /* Bottom inclusive bounds */

		long frameLeft=0; /*
						 * Left side of inclusive picture frame (unit is 0.01
						 * mm)
						 */

		long frameTop=00; /* Top side of inclusive picture frame */

		long frameRight=1000; /* Right side of inclusive picture frame */

		long frameBottom=1000; /* Bottom side of inclusive picture frame */

		int signature = 0x464D4520; /* Signature ID (always 0x464D4520) */

		int version = 0x00000100; // Version of the metafile. Always 0x00000100
									

		int metafileSize=88; /* Size of the metafile in bytes */

		int numberOfRecords=1; /* Number of records in the metafile */

		short numOfHandles= 1; /* Number of handles in the handle table. Starts with 1. Don't ask. */

		short reserved = 0; /* Not used (always 0) */

		int sizeOfDescrip; /* Size of description string in WORDs */

		int offsOfDescrip; /* Offset of description string in metafile */

		int numPalEntries; /* Number of color palette entries */

		long widthDevPixels=1000; /* Width of reference device in pixels */

		long heightDevPixels=1000; /* Height of reference device in pixels */

		long widthDevMM= 10; /* Width of reference device in millimeters */

		long heightDevMM= 10; /* Height of reference device in millimeters */

		/**
		 * Writes the header. Should be called after all data has been written.
		 * @throws IOException 
		 */
		private void write() throws IOException {
			out.seek(0);
			RandomAccessStreamUtils.writeU32(out,recordType); 
			RandomAccessStreamUtils.writeU32(out,recordSize);
			
			RandomAccessStreamUtils.writeS32(out,boundsLeft); // left
			RandomAccessStreamUtils.writeS32(out,boundsTop); // top
			RandomAccessStreamUtils.writeS32(out,boundsRight); // right.
			RandomAccessStreamUtils.writeS32(out,boundsBottom); // Bottom
			
			RandomAccessStreamUtils.writeS32(out,frameLeft);
			RandomAccessStreamUtils.writeS32(out,frameTop);
			RandomAccessStreamUtils.writeS32(out,frameRight);
			RandomAccessStreamUtils.writeS32(out,frameBottom);
		
			RandomAccessStreamUtils.writeU32(out,signature);
			RandomAccessStreamUtils.writeU32(out,version);
			RandomAccessStreamUtils.writeU32(out,metafileSize);
			RandomAccessStreamUtils.writeU32(out,numberOfRecords);
			RandomAccessStreamUtils.writeU16(out, numOfHandles);
			RandomAccessStreamUtils.writeU16(out, reserved);
			RandomAccessStreamUtils.writeU32(out,sizeOfDescrip);
			RandomAccessStreamUtils.writeU32(out,offsOfDescrip);
			RandomAccessStreamUtils.writeU32(out,numPalEntries);
			
			RandomAccessStreamUtils.writeS32(out, widthDevPixels);
			RandomAccessStreamUtils.writeS32(out, heightDevPixels);
			
			RandomAccessStreamUtils.writeS32(out, widthDevMM);
			RandomAccessStreamUtils.writeS32(out, heightDevMM);
			
		}

	}
	

	public void selectObject(long nobj) throws IOException {
		addCommandX(EMR_SELECTOBJECT, nobj);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void closeFigure() throws IOException {
		addCommand(EMR_CLOSEFIGURE);
	}

	/**
	 * @param i
	 * @param j
	 * @throws IOException 
	 */
	public void setWindowOrg(int x, int y) throws IOException {
		addCommandXY(EMR_SETWINDOWORGEX, x, y);
	}

	/**
	 * Sets the reference window extension.
	 * @param i
	 * @param j
	 * @throws IOException 
	 */
	public void setWindowExt(int w, int h) throws IOException {
		addCommandXY(EMR_SETWINDOWEXTEX, w, h);
	}

	public void lineTo(EMFPoint point) throws IOException {
		lineTo(point.getX(), point.getY());
	}

	public void moveToEx(EMFPoint point) throws IOException {
		moveToEx(point.getX(), point.getY());
	}

	/**
	 * Generic function for building a Pen.
	 * 
	 * @param penStyle :
	 *            one of : one of PS_SOLID, PS_DASH, PS_DOT, PS_DASHDOT,
	 *            PS_DASHDOTDOT, PS_NULL, PS_INSIDEFRAME, PS_USERSTYLE,
	 *            PS_ALTERNATE
	 * @param width :
	 *            the pen's width, in 1/1440 of an inch.
	 * @param penColour :
	 *            the line colour
	 * @param fillhatch :
	 *            ignored if style is not hatched ; else : one of HS_HORIZONTAL,
	 *            HS_VERTICAL, HS_FDIAGONAL, HS_BDIAGONAL, HS_CROSS,
	 *            HS_DIAGCROSS
	 * @param fillColour :
	 *            the fill colour.
	 * @param fillStyle
	 *            one of BS_SOLID, BS_NULL or BS_HATCHED.
	 * @return the new pen.
	 * @throws IOException
	 */
	
	public EMFPen createPenBrush(int penStyle, short width, long penColour,
			short fillhatch, long fillColour, int fillStyle) throws IOException {
		short p = createPen(penStyle, width, penColour);
		short b = createBrush(fillhatch, fillColour, fillStyle);
		return new EMFPen(p, b);
	}

	public EMFPen createDrawPen(int penStyle, short width, long colour)
			throws IOException {
		return createPenBrush(penStyle, width, colour,
				WMFConstants.HS_VERTICAL, colour, WMFConstants.BS_NULL);
	}

	public EMFPen createDrawPen(short width, long colour) throws IOException {
		return createPenBrush(WMFConstants.PS_SOLID, width, colour,
				WMFConstants.HS_VERTICAL, colour, WMFConstants.BS_NULL);
	}

	public EMFPen createFillPen(short fillHatch, long colour, int fillStyle)
			throws IOException {
		return createPenBrush(WMFConstants.PS_NULL, (short) 0, colour,
				fillHatch, colour, fillStyle);
	}

	public void saveDC() throws IOException {
		addCommand(EMR_SAVEDC);
	}
	
	public void restoreDC() throws IOException {
		addCommand(EMR_RESTOREDC);
	}
	
	public EMFPen createFillPen(long colour) throws IOException {
		return createPenBrush(WMFConstants.PS_NULL, (short) 0, colour,
				WMFConstants.HS_VERTICAL, colour, WMFConstants.BS_SOLID);
	}

	public void selectPen(EMFPen pen) throws IOException {
		selectObject(pen.getPenNum());
		selectObject(pen.getBrushNum());
	}

	public void freePen(EMFPen pen) throws IOException {
		deleteObject(pen.getPenNum());
		deleteObject(pen.getBrushNum());
	}
}