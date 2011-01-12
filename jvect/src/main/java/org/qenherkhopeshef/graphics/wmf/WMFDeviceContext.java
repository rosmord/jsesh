package org.qenherkhopeshef.graphics.wmf;

import java.awt.Color;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.qenherkhopeshef.graphics.generic.RandomAccessFileAdapter;
import org.qenherkhopeshef.graphics.generic.RandomAccessStream;
import org.qenherkhopeshef.graphics.generic.RandomAccessStreamUtils;


/*
 * Created on 4 nov. 2004
 *
 * This file is distributed under the LGPL.
 */

/**
 * A low level implementation of the wmf vector file device.
 * 
 * <p>
 * To avoid the burden of encapsulating each integer value in an object, we have
 * decided to use the following conventions:
 * 
 * (U means unsigned, S means signed, and the size is in bits).
 * <ul>
 * <li>U8 is represented by a char.
 * <li>S16 is represented by a short.
 * <li>U16 is represented by an int.
 * <li>S32 is represented by an long. This last choice allows us to use a
 * different java type for each integer type used in metafile.
 * <li>Thanks to these choices, computations are straightforward.
 * </ul>
 * 
 * @author S. Rosmorduc
 * 
 */

public class WMFDeviceContext implements WMFFunctionCodes, WMFConstants {

	private WindowsMetaHeader head;

	private PlaceableMetaHeader meta;

	/**
	 * objects is a table of boolean, which remembers if objects are used (true)
	 * or not (false).
	 */
	private ArrayList objects;

	private RandomAccessStream out;

	
	public WMFDeviceContext(RandomAccessStream stream, Dimension2D dims)
			throws IOException {
		this(stream,  dims.getWidth(), dims.getHeight());
	}

	/**
	 * Create a metafile in a RandomAccessStream, for instance a
	 * RandomAccessByteArray or a RandomAccessFile.
	 * 
	 * @param stream
	 * @param width
	 * @param height
	 * @throws IOException
	 */

	public WMFDeviceContext(RandomAccessStream stream, double width, double height)
			throws IOException {
		initLowLevelGraphics(stream, (int)Math.ceil(width), (int)Math.ceil(height));
	}

	private void addCommand(short func) throws IOException {
		addRecord(func, 0);
	}

	private void addCommandABCD(short func, short a, short b, short c, short d)
			throws IOException {
		addRecord(func, 4);
		RandomAccessStreamUtils.writeS16(out, d);
		RandomAccessStreamUtils.writeS16(out, c);
		RandomAccessStreamUtils.writeS16(out, b);
		RandomAccessStreamUtils.writeS16(out, a);
	}

	private void addCommandL(short func, long x) throws IOException {
		addRecord(func, 2);
		RandomAccessStreamUtils.writeS32(out, x);
	}

	private void addCommandLINES(short func, WMFPoint p[], short nbr)
			throws IOException {
		int i;
		addRecord(func, 2 * nbr + 1);
		RandomAccessStreamUtils.writeS16(out, nbr);
		for (i = 0; i < nbr; i++) {
			RandomAccessStreamUtils.writeS16(out, p[i].x);
			RandomAccessStreamUtils.writeS16(out, p[i].y);
		}
	}

	private void addCommandX(short func, short x) throws IOException {
		addRecord(func, 1);
		RandomAccessStreamUtils.writeS16(out, x);
	}

	private void addCommandXY(short func, short x, short y) throws IOException {
		addRecord(func, 2);
		RandomAccessStreamUtils.writeS16(out, y);
		RandomAccessStreamUtils.writeS16(out, x);
	}

	private void addCommandXYL(short func, short a, short b, long c)
			throws IOException {
		addRecord(func, 4);
		RandomAccessStreamUtils.writeS32(out, c);
		RandomAccessStreamUtils.writeS16(out, b);
		RandomAccessStreamUtils.writeS16(out, a);
	}

	private void addRecord(short function, int nbrshorts) throws IOException {
		int size = 3 + nbrshorts;
		/* Update header values */
		head.NumOfObjects++;
		if (head.MaxRecordSize < size)
			head.MaxRecordSize = size;
		head.FileSize += size;
		/* Start writing data */
		RandomAccessStreamUtils.writeS32(out, size);
		RandomAccessStreamUtils.writeS16(out, function);
	}

	/**
	 * find and return a free object.
	 * 
	 * @return the object index.
	 */

	private short allocObject() {
		short result;
		int i;
		for (i = 0; i < objects.size()
				&& ((Boolean) objects.get(i)).booleanValue(); i++)
			;
		if (i == objects.size()) {
			result = (short) i;
			objects.add(Boolean.TRUE);
		} else {
			result = (short) i;
			objects.set(i, Boolean.TRUE);
		}
		return result;
	}

	/**
	 * Arc from an ellipse described by top,left,... The Arc itself is described
	 * by xstart & co
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param xstart
	 * @param ystart
	 * @param xend
	 * @param yend
	 * @throws IOException
	 * 
	 */

	public void arc(short left, short top, short right, short bottom,
			short xstart, short ystart, short xend, short yend)
			throws IOException {
		addRecord(META_ARC, 8);
		RandomAccessStreamUtils.writeS16(out, yend);
		RandomAccessStreamUtils.writeS16(out, xend);
		RandomAccessStreamUtils.writeS16(out, ystart);
		RandomAccessStreamUtils.writeS16(out, xstart);
		RandomAccessStreamUtils.writeS16(out, bottom);
		RandomAccessStreamUtils.writeS16(out, right);
		RandomAccessStreamUtils.writeS16(out, top);
		RandomAccessStreamUtils.writeS16(out, left);
	}

	/*
	 * void SetBkColor( int x, int y) { AddCommandXY( 0x020B, x, y); }
	 */

	/**
	 * The last record in every metafile always has a function number of 0000h,
	 * a Size of 0003h, and no Parameters array. This record is used to indicate
	 * the end of the record data in the metafile. The use of this terminator
	 * record is missing from the original WMF description found in the Windows
	 * SDK and is now documented in article Q99334 of the Microsoft Knowledge
	 * Base.
	 * 
	 * @throws IOException
	 */

	public void close() throws IOException {
		addCommand((short) 0x0);
		/* OK. Now, we should write the real values for the header */
		out.seek(0);
		writeHEADER();
		out.close();
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
	public WMFPen createPenBrush(int penStyle, short width, long penColour,
			short fillhatch, long fillColour, int fillStyle) throws IOException {
		short p = createPenIndirect(penStyle, width, penColour);
		short b = createBrushIndirect(fillhatch, fillColour, fillStyle);
		return new WMFPen(p, b);
	}

	public WMFPen createDrawPen(int penStyle, short width, long colour)
			throws IOException {
		return createPenBrush(penStyle, width, colour,
				(short) WMFConstants.HS_VERTICAL, colour, WMFConstants.BS_NULL);
	}

	public WMFPen createDrawPen(short width, long colour) throws IOException {
		return createPenBrush(WMFConstants.PS_SOLID, width, colour,
				(short) WMFConstants.HS_VERTICAL, colour, WMFConstants.BS_NULL);
	}

	public WMFPen createFillPen(short fillHatch, long colour, int fillStyle)
			throws IOException {
		return createPenBrush(WMFConstants.PS_NULL, (short) 0, colour,
				fillHatch, colour, fillStyle);
	}

	public WMFPen createFillPen(long colour) throws IOException {
		return createPenBrush(WMFConstants.PS_NULL, (short) 0, colour,
				(short) WMFConstants.HS_VERTICAL, colour, WMFConstants.BS_SOLID);
	}

	public void selectPen(WMFPen pen) throws IOException {
		SelectObject(pen.getPenNum());
		SelectObject(pen.getBrushNum());
	}

	public void freePen(WMFPen pen) throws IOException {
		deleteObject(pen.getPenNum());
		deleteObject(pen.getBrushNum());
	}

	/**
	 * Create a new pen.
	 * 
	 * @param style
	 *            one of : one of PS_SOLID, PS_DASH, PS_DOT, PS_DASHDOT,
	 *            PS_DASHDOTDOT, PS_NULL, PS_INSIDEFRAME, PS_USERSTYLE,
	 *            PS_ALTERNATE
	 * @param width :
	 *            width of the pen, if > 1, will force style to solid.
	 * @param colour :
	 *            colour of the pen
	 * @return the pen object's id.
	 * @throws IOException
	 */
	public short createPenIndirect(int style, short width, long colour)
			throws IOException {
		addRecord(META_CREATEPENINDIRECT, 5);
		/* There seems to be a padding word between width and colour */
		RandomAccessStreamUtils.writeU16(out, style);
		RandomAccessStreamUtils.writeS16(out, width);
		RandomAccessStreamUtils.writeS16(out, (short) 0);
		RandomAccessStreamUtils.writeS32(out, colour);
		return allocObject();
	}
	
	/**
	 * create a brush.
	 * 
	 * @param hatch :
	 *            ignored if style is not hatched ; else : one of HS_HORIZONTAL,
	 *            HS_VERTICAL, HS_FDIAGONAL, HS_BDIAGONAL, HS_CROSS,
	 *            HS_DIAGCROSS
	 * @param colour :
	 *            colour of the brush.
	 * @param style :
	 *            one of BS_SOLID, BS_NULL or BS_HATCHED.
	 * @return object identifier.
	 * @throws IOException
	 */

	public short createBrushIndirect(short hatch, long colour, int style)
			throws IOException {
		addRecord(META_CREATEBRUSHINDIRECT, 4);
		RandomAccessStreamUtils.writeU16(out, style);
		RandomAccessStreamUtils.writeS32(out, colour);
		RandomAccessStreamUtils.writeS16(out, hatch);
		return allocObject();
	}

	public int createFontIndirect(short height, int width, int escapement,
			int orientation, int weight, char italic, char underlined,
			char striked, char charset, char outPrecision, char clipPrecision,
			char quality, char pitch, char name[]) throws IOException {
		int i;
		addRecord(META_CREATEFONTINDIRECT, 25);
		RandomAccessStreamUtils.writeS16(out, height);
		RandomAccessStreamUtils.writeU16(out, width);
		RandomAccessStreamUtils.writeU16(out, escapement);
		RandomAccessStreamUtils.writeU16(out, orientation);
		RandomAccessStreamUtils.writeU16(out, weight);
		RandomAccessStreamUtils.writeU8(out, italic);
		RandomAccessStreamUtils.writeU8(out, underlined);
		RandomAccessStreamUtils.writeU8(out, striked);
		RandomAccessStreamUtils.writeU8(out, charset);
		RandomAccessStreamUtils.writeU8(out, outPrecision);
		RandomAccessStreamUtils.writeU8(out, clipPrecision);
		RandomAccessStreamUtils.writeU8(out, quality);
		RandomAccessStreamUtils.writeU8(out, pitch);
		for (i = 0; i < 32; i++)
			RandomAccessStreamUtils.writeU8(out, name[i]);
		return allocObject();
	}



	public int createRegion(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_CREATEREGION, left, top, right, bottom);
		return allocObject();
	}

	public void deleteObject(short nobj) throws IOException {
		FreeObject(nobj);
		addCommandX(META_DELETEOBJECT, nobj);
	}

	public void ellipse(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_ELLIPSE, left, top, right, bottom);
	}

	/*
	 * Clipping
	 */

	public void ExcludeClipRect(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_EXCLUDECLIPRECT, left, top, right, bottom);
	}

	public void FillRgn(short nbrush, short nregion) throws IOException {
		addCommandXY(META_FILLREGION, nbrush, nregion);
	}

	public void FloodFill(short x, short y, long colour) throws IOException {
		addCommandXYL(META_FLOODFILL, x, y, colour);
	}

	/**
	 * frees a resource.
	 * 
	 * @param nobj
	 */

	private void FreeObject(int nobj) {
		objects.set(nobj, Boolean.FALSE);
	}

	/**
	 * Aux. function for the constructor.
	 * 
	 * @param out
	 * @param width
	 * @param height
	 * @throws IOException
	 */

	private void initLowLevelGraphics(RandomAccessStream out, int width,
			int height) throws IOException {
		out.setLength(0);
		objects = new ArrayList();
		meta = new PlaceableMetaHeader();
		head = new WindowsMetaHeader();
		/* unsigned char s[]= { 0xD7, 0xCD, 0xC6, 0x9A}; */
		/* Aldus Placeable file data */
		meta.Key = (0x9AC6CDD7); /* Always this value */
		meta.Handle = (0); /* always 0 */
		meta.Left = 0; /* coordinate in twips (1/1440 inc) */
		meta.Top = 0;
		meta.Right = width;
		meta.Bottom = height;
		meta.Inch = (1440); /* metafile units per inch */
		meta.Reserved = (0);
		meta.Checksum = (0);
		/* Plain Metafile part */
		head.FileType = (1);
		head.HeaderSize = (9);
		head.Version = (0x0300);
		head.FileSize = (9);
		head.NumOfObjects = (0);
		head.MaxRecordSize = (0);
		head.NumOfParams = (0);
		/* administrative part */
		this.out = out;
		// NOTE : create the object table.

		/* Ok. Now, we write the header */
		writeHEADER();
		/* Good. Now, commands can be written to the file */
	}

	public void IntersectClipRetc(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_INTERSECTCLIPRECT, left, top, right, bottom);
	}

	public void InvertRgn(short nregion) throws IOException {
		addCommandX(META_INVERTREGION, nregion);
	}

	public void LineTo(short x, short y) throws IOException {
		addCommandXY(META_LINETO, x, y);
	}

	public void LineTo(WMFPoint p) throws IOException {
		LineTo(p.x, p.y);
	}

	public void MoveTo(short x, short y) throws IOException {
		addCommandXY(META_MOVETO, x, y);
	}

	public void MoveTo(WMFPoint p) throws IOException {
		MoveTo(p.x, p.y);
	}

	public void OffsetViewportOrg(short x, short y) throws IOException {
		addCommandXY(META_OFFSETVIEWPORTORG, x, y);
	}

	public void OffsetWindowOrg(short x, short y) throws IOException {
		addCommandXY(META_OFFSETWINDOWORG, x, y);
	}

	public void PaintRgn(short nregion) throws IOException {
		addCommandX(META_PAINTREGION, nregion);
	}

	public void PatBlt(short left, short top, short right, short bottom, long rop)
			throws IOException {
		addRecord(META_PATBLT, 6);
		RandomAccessStreamUtils.writeS32(out, rop);
		RandomAccessStreamUtils.writeS16(out, bottom);
		RandomAccessStreamUtils.writeS16(out, right);
		RandomAccessStreamUtils.writeS16(out, top);
		RandomAccessStreamUtils.writeS16(out, left);
	}

	/*
	 * Now, the actual functions
	 */

	public void Polygon(WMFPoint p[], short nbr) throws IOException {
		addCommandLINES(META_POLYGON, p, nbr);
	}

	public void PolyLine(WMFPoint p[], short nbr) throws IOException {
		addCommandLINES(META_POLYLINE, p, nbr);
	}

	public void rectangle(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_RECTANGLE, left, top, right, bottom);
	}

	public void RestoreDC(short lev) throws IOException {
		addCommandX(META_RESTOREDC, lev);
	}

	public void RoundRectangle(short left, short top, short right, short bottom,
			short wi, short h) throws IOException {
		addRecord(META_ROUNDRECT, 6);
		RandomAccessStreamUtils.writeS16(out, h);
		RandomAccessStreamUtils.writeS16(out, wi);
		RandomAccessStreamUtils.writeS16(out, bottom);
		RandomAccessStreamUtils.writeS16(out, right);
		RandomAccessStreamUtils.writeS16(out, top);
		RandomAccessStreamUtils.writeS16(out, left);
	}

	public void SaveDC() throws IOException {
		addCommand(META_SAVEDC);
	}

	public void ScaleViewportExt(short xnum, short xdenom, short ynum, short ydenom)
			throws IOException {
		addRecord(META_SCALEVIEWPORTEXT, 4);
		RandomAccessStreamUtils.writeS16(out, ydenom);
		RandomAccessStreamUtils.writeS16(out, ynum);
		RandomAccessStreamUtils.writeS16(out, xdenom);
		RandomAccessStreamUtils.writeS16(out, xnum);
	}

	public void ScaleWindowExt(short xnum, short xdenom, short ynum, short ydenom)
			throws IOException {
		addRecord(META_SCALEWINDOWEXT, 4);
		RandomAccessStreamUtils.writeS16(out, ydenom);
		RandomAccessStreamUtils.writeS16(out, ynum);
		RandomAccessStreamUtils.writeS16(out, xdenom);
		RandomAccessStreamUtils.writeS16(out, xnum);
	}

	public void SelectClipRgn(short nregion) throws IOException {
		addCommandX(META_SELECTCLIPREGION, nregion);
	}

	public void SelectObject(short nobj) throws IOException {
		addCommandX(META_SELECTOBJECT, nobj);
	}

	public void SetBKColor(long color) throws IOException {
		addCommandL(META_SETBKCOLOR, color);
	}

	public void SetBKMode(short mode) throws IOException {
		addCommandX(META_SETBKMODE, mode);
	}

	public void SetMapMode(short map) throws IOException {
		addCommandX(META_SETMAPMODE, map);
	}

	public void SetPixel(short x, short y, long colour) throws IOException {
		addCommandXYL(META_SETPIXEL, x, y, colour);
	}

	public void SetPixel(WMFPoint p, long colour) throws IOException {
		SetPixel(p.x, p.y, colour);
	}

	/**
	 * Sets the way polygons are filled.
	 * 
	 * @param fillmode :
	 *            one of ALTERNATE, WINDING, POLYFILL_LAST (?).
	 * @throws IOException
	 */
	public void SetPolyFillMode(short fillmode) throws IOException {
		addCommandX(META_SETPOLYFILLMODE, fillmode);
	}

	public void SetROP2(long rop) throws IOException {
		addCommandL(META_SETROP2, rop);
	}

	public void SetTextCharacterExtra(short extra) throws IOException {
		addCommandX(META_SETTEXTCOLOR, extra);
	}

	public void SetTextColor(long color) throws IOException {
		addCommandL(META_SETTEXTCOLOR, color);
	}

	public void setViewportExt(short x, short y) throws IOException {
		addCommandXY(META_SETVIEWPORTEXT, x, y);
	}

	public void setViewportExt(WMFPoint p) throws IOException {
		setViewportExt(p.x, p.y);
	}

	public void setViewportOrg(short x, short y) throws IOException {
		addCommandXY(META_SETVIEWPORTORG, x, y);
	}

	public void setWindowExt(short x, short y) throws IOException {
		addCommandXY(META_SETWINDOWEXT, x, y);
	}

	public void setWindowExt(WMFPoint p) throws IOException {
		setWindowExt(p.x, p.y);
	}

	public void setWindowOrg(short x, short y) throws IOException {
		addCommandXY(META_SETWINDOWORG, x, y);
	}

	public void textOut(short x, short y, char s[]) throws IOException {
		int i;
		short length = (short) s.length;
		addRecord(META_TEXTOUT, (length + 1) / 2 + 3);
		/* count of chars in the string */
		RandomAccessStreamUtils.writeS16(out, (short) length);
		/* the string (16bit aligned) */
		for (i = 0; i < length; i++)
			RandomAccessStreamUtils.writeU8(out, s[i]);
		/* ensures a even number of chars */
		if (length % 2 != 0)
			RandomAccessStreamUtils.writeU8(out, (char) 0);
		/* y */
		RandomAccessStreamUtils.writeS16(out, y);
		/* x */
		RandomAccessStreamUtils.writeS16(out, x);
	}

	/*
	 * Stolen from wingdi int rgb(int r, int g,int b) { return ((unsigned
	 * int)((r) | ((g) < < 8) | ((b) < < 16))); }
	 */

	/**
	 * Draws a text.
	 * 
	 * @param x
	 * @param y
	 * @param txt
	 * @throws IOException
	 */
	public void textOut(short x, short y, String txt) throws IOException {
		char t[] = txt.toCharArray();
		textOut(x, y, t);
	}

	public void textOut(WMFPoint p, char s[]) throws IOException {
		textOut(p.x, p.y, s);
	}

	public void textOut(WMFPoint p, String txt) throws IOException {
		textOut(p.x, p.y, txt);
	}

	private void writeHEADER() throws IOException {
		// Generate Aldus metafile header
		meta.compute_checksum();
		meta.write();
		
		// We could also generate a non-aldus metafile :
		// writeClipboardHeader(meta);
		
		// Generate the standard header common to all metafiles.
		head.write();
	}

	/**
	 * write a header for a clipboard meta file (DOESN'T WORK).
	 * @param meta2
	 * @throws IOException 
	 */
	private void writeClipboardHeader(PlaceableMetaHeader meta2) throws IOException {
		// LONG MappingMode; /* Units used to playback metafile */
		RandomAccessStreamUtils.writeS32(out, 6);
		// LONG Width; /* Width of the metafile */
		RandomAccessStreamUtils.writeS32(out, meta2.Right);		
		// LONG Height; /* Height of the metafile */
		RandomAccessStreamUtils.writeS32( out, meta2.Bottom);
		// LONG Handle; /* Handle to the metafile in memory */
		RandomAccessStreamUtils.writeS32(out, 0);
	}
	

	class PlaceableMetaHeader {

		int Bottom; /* Bottom coordinate in metafile units */

		short Checksum; /* Checksum value for previous 10 shorts */

		short Handle; /* Metafile HANDLE number (always 0) */

		short Inch; /* Number of metafile units per inch (normally 1440) */

		long Key; /* Magic number (always 9AC6CDD7h) */

		int Left; /* Left coordinate in metafile units */

		long Reserved; /* Reserved (always 0) */

		int Right; /* Right coordinate in metafile units */

		int Top; /* Top coordinate in metafile units (IN TWIPS !!!) */

		void compute_checksum() {
			// IMPORTANT : write this NOW !
			/*
			 * short *ptr; Checksum = 0; for (ptr = (short *) pmh; ptr < (short
			 * *)&pmh->Checksum; ptr++) Checksum ^= *ptr;
			 */
			Checksum = 0;
			short b[] = new short[9];
			b[0] = (short) (Key & 0xFF);
			b[1] = (short) ((Key & 0xFF00) >>> 16);
			b[2] = (short) Left;
			b[3] = (short) Top;
			b[4] = (short) Right;
			b[5] = (short) Bottom;
			b[6] = Inch;
			b[7] = (short) (Reserved & 0xFF);
			b[8] = (short) ((Reserved & 0xFF00) >>> 16);
			for (int i = 0; i < b.length; i++)
				Checksum ^= b[i];
		}

		public void write() throws IOException {
			RandomAccessStreamUtils.writeS32(out, Key);
			RandomAccessStreamUtils.writeS16(out, Handle);
			RandomAccessStreamUtils.writeU16(out, Left);
			RandomAccessStreamUtils.writeU16(out, Top);
			RandomAccessStreamUtils.writeU16(out, Right);
			RandomAccessStreamUtils.writeU16(out, Bottom);
			RandomAccessStreamUtils.writeS16(out, Inch);
			RandomAccessStreamUtils.writeS32(out, Reserved);
			RandomAccessStreamUtils.writeS16(out, Checksum);
		}
	}

	class WindowsMetaHeader {

		long FileSize; /* Total size of the metafile in shorts */

		short FileType; /* Type of metafile (0=memory, 1=disk) */

		short HeaderSize; /* Size of header in shortS (always 9) */

		long MaxRecordSize; /* The size of largest record in shorts */

		short NumOfObjects; /* Number of objects in the file */

		short NumOfParams; /* Not Used (always 0) */

		short Version; /* Version of Microsoft Windows used */

		public void write() throws IOException {
			RandomAccessStreamUtils.writeS16(out, FileType); // WMFWORD FileType; /* Type of metafile
								// (0=memory, 1=disk) */
			RandomAccessStreamUtils.writeS16(out, HeaderSize); // WMFWORD HeaderSize; /* Size of header in
									// WMFWORDS (always 9) */
			RandomAccessStreamUtils.writeS16(out, Version); // WMFWORD Version; /* Version of Microsoft
								// Windows used */
			RandomAccessStreamUtils.writeS32(out, FileSize); // WMFDWORD FileSize; /* Total size of the
								// metafile in WMFWORDs */
			RandomAccessStreamUtils.writeS16(out, NumOfObjects); // WMFWORD NumOfObjects; /* Number of
									// objects in the file */
			RandomAccessStreamUtils.writeS32(out, MaxRecordSize); // WMFDWORD MaxRecordSize; /* The size
										// of largest record in WMFWORDs
			RandomAccessStreamUtils.writeS16(out, NumOfParams); // WMFWORD NumOfParams; /* Not Used (always
									// 0) */
		}
	}

	/**
	 * Build a correct colour for use in metafiles. (note that the model is
	 * really bgr).
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return the color code.
	 */
	static public long buildColor(int r, int g, int b) {
		return ((r) | ((g) << 8) | ((b) << 16));
	}

	static public long buildColor(Color col) {
		return buildColor(col.getRed(), col.getGreen(), col.getBlue());
	}

	public static void main(String args[]) throws IOException {
		RandomAccessFile o = new RandomAccessFile("test.wmf", "rw");
		WMFDeviceContext wmf = new WMFDeviceContext(
				new RandomAccessFileAdapter(o), 200, 200);
		WMFPoint pol[] = { new WMFPoint((short) 20, (short) 20),
				new WMFPoint((short) 40, (short) 20),
				new WMFPoint((short) 100, (short) 110),
				new WMFPoint((short) 200, (short) 40),
				new WMFPoint((short) 405, (short) 200) };
		wmf.setWindowOrg((short) 0, (short) 0);
		wmf.setWindowExt((short) 200, (short) 200);
		wmf.MoveTo((short) 10, (short) 10);
		wmf.LineTo((short) 50, (short) 100);
		wmf.LineTo((short) 100, (short) 50);
		wmf.LineTo((short) 10, (short) 10);
		wmf.Polygon(pol, (short) 4);
	}


}