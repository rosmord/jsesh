package jsesh.graphics.wmfexport;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import jsesh.graphics.generic.RandomAccessFileAdapter;
import jsesh.graphics.generic.RandomAccessStream;

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
			writeS32(Key);
			writeS16(Handle);
			writeU16(Left);
			writeU16(Top);
			writeU16(Right);
			writeU16(Bottom);
			writeS16(Inch);
			writeS32(Reserved);
			writeS16(Checksum);
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
			writeS16(FileType); // WMFWORD FileType; /* Type of metafile
								// (0=memory, 1=disk) */
			writeS16(HeaderSize); // WMFWORD HeaderSize; /* Size of header in
									// WMFWORDS (always 9) */
			writeS16(Version); // WMFWORD Version; /* Version of Microsoft
								// Windows used */
			writeS32(FileSize); // WMFDWORD FileSize; /* Total size of the
								// metafile in WMFWORDs */
			writeS16(NumOfObjects); // WMFWORD NumOfObjects; /* Number of
									// objects in the file */
			writeS32(MaxRecordSize); // WMFDWORD MaxRecordSize; /* The size
										// of largest record in WMFWORDs
			writeS16(NumOfParams); // WMFWORD NumOfParams; /* Not Used (always
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
	static long buildColor(int r, int g, int b) {
		return ((r) | ((g) << 8) | ((b) << 16));
	}

	static long buildColor(Color col) {
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

	WindowsMetaHeader head;

	PlaceableMetaHeader meta;

	/**
	 * objects is a table of boolean, which remembers if objects are used (true)
	 * or not (false).
	 */
	ArrayList objects;

	RandomAccessStream out;

	public WMFDeviceContext(RandomAccessStream stream, Dimension dims)
			throws IOException {
		this(stream, (int) dims.getWidth(), (int) dims.getHeight());
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

	public WMFDeviceContext(RandomAccessStream stream, int width, int height)
			throws IOException {
		initLowLevelGraphics(stream, width, height);
	}

	private void addCommand(short func) throws IOException {
		addRecord(func, 0);
	}

	private void addCommandABCD(short func, short a, short b, short c, short d)
			throws IOException {
		addRecord(func, 4);
		writeS16(d);
		writeS16(c);
		writeS16(b);
		writeS16(a);
	}

	private void addCommandL(short func, long x) throws IOException {
		addRecord(func, 2);
		writeS32(x);
	}

	private void addCommandLINES(short func, WMFPoint p[], short nbr)
			throws IOException {
		int i;
		addRecord(func, 2 * nbr + 1);
		writeS16(nbr);
		for (i = 0; i < nbr; i++) {
			writeS16(p[i].x);
			writeS16(p[i].y);
		}
	}

	private void addCommandX(short func, short x) throws IOException {
		addRecord(func, 1);
		writeS16(x);
	}

	private void addCommandXY(short func, short x, short y) throws IOException {
		addRecord(func, 2);
		writeS16(y);
		writeS16(x);
	}

	private void addCommandXYL(short func, short a, short b, long c)
			throws IOException {
		addRecord(func, 4);
		writeS32(c);
		writeS16(b);
		writeS16(a);
	}

	private void addRecord(short function, int nbrshorts) throws IOException {
		int size = 3 + nbrshorts;
		/* Update header values */
		head.NumOfObjects++;
		if (head.MaxRecordSize < size)
			head.MaxRecordSize = size;
		head.FileSize += size;
		/* Start writing data */
		writeS32(size);
		writeS16(function);
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
		writeS16(yend);
		writeS16(xend);
		writeS16(ystart);
		writeS16(xstart);
		writeS16(bottom);
		writeS16(right);
		writeS16(top);
		writeS16(left);
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

	void close() throws IOException {
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

	short createBrushIndirect(short hatch, long colour, int style)
			throws IOException {
		addRecord(META_CREATEBRUSHINDIRECT, 4);
		writeU16(style);
		writeS32(colour);
		writeS16(hatch);
		return allocObject();
	}

	int createFontIndirect(short height, int width, int escapement,
			int orientation, int weight, char italic, char underlined,
			char striked, char charset, char outPrecision, char clipPrecision,
			char quality, char pitch, char name[]) throws IOException {
		int i;
		addRecord(META_CREATEFONTINDIRECT, 25);
		writeS16(height);
		writeU16(width);
		writeU16(escapement);
		writeU16(orientation);
		writeU16(weight);
		writeU8(italic);
		writeU8(underlined);
		writeU8(striked);
		writeU8(charset);
		writeU8(outPrecision);
		writeU8(clipPrecision);
		writeU8(quality);
		writeU8(pitch);
		for (i = 0; i < 32; i++)
			writeU8(name[i]);
		return allocObject();
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
	short createPenIndirect(int style, short width, long colour)
			throws IOException {
		addRecord(META_CREATEPENINDIRECT, 5);
		/* There seems to be a padding word between width and colour */
		writeU16(style);
		writeS16(width);
		writeS16((short) 0);
		writeS32(colour);
		return allocObject();
	}

	int createRegion(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_CREATEREGION, left, top, right, bottom);
		return allocObject();
	}

	void deleteObject(short nobj) throws IOException {
		FreeObject(nobj);
		addCommandX(META_DELETEOBJECT, nobj);
	}

	void ellipse(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_ELLIPSE, left, top, right, bottom);
	}

	/*
	 * Clipping
	 */

	void ExcludeClipRect(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_EXCLUDECLIPRECT, left, top, right, bottom);
	}

	void FillRgn(short nbrush, short nregion) throws IOException {
		addCommandXY(META_FILLREGION, nbrush, nregion);
	}

	void FloodFill(short x, short y, long colour) throws IOException {
		addCommandXYL(META_FLOODFILL, x, y, colour);
	}

	/**
	 * frees a resource.
	 * 
	 * @param nobj
	 */

	void FreeObject(int nobj) {
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

	void IntersectClipRetc(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_INTERSECTCLIPRECT, left, top, right, bottom);
	}

	void InvertRgn(short nregion) throws IOException {
		addCommandX(META_INVERTREGION, nregion);
	}

	void LineTo(short x, short y) throws IOException {
		addCommandXY(META_LINETO, x, y);
	}

	void LineTo(WMFPoint p) throws IOException {
		LineTo(p.x, p.y);
	}

	void MoveTo(short x, short y) throws IOException {
		addCommandXY(META_MOVETO, x, y);
	}

	void MoveTo(WMFPoint p) throws IOException {
		MoveTo(p.x, p.y);
	}

	void OffsetViewportOrg(short x, short y) throws IOException {
		addCommandXY(META_OFFSETVIEWPORTORG, x, y);
	}

	void OffsetWindowOrg(short x, short y) throws IOException {
		addCommandXY(META_OFFSETWINDOWORG, x, y);
	}

	void PaintRgn(short nregion) throws IOException {
		addCommandX(META_PAINTREGION, nregion);
	}

	void PatBlt(short left, short top, short right, short bottom, long rop)
			throws IOException {
		addRecord(META_PATBLT, 6);
		writeS32(rop);
		writeS16(bottom);
		writeS16(right);
		writeS16(top);
		writeS16(left);
	}

	/*
	 * Now, the actual functions
	 */

	void Polygon(WMFPoint p[], short nbr) throws IOException {
		addCommandLINES(META_POLYGON, p, nbr);
	}

	void PolyLine(WMFPoint p[], short nbr) throws IOException {
		addCommandLINES(META_POLYLINE, p, nbr);
	}

	void rectangle(short left, short top, short right, short bottom)
			throws IOException {
		addCommandABCD(META_RECTANGLE, left, top, right, bottom);
	}

	void RestoreDC(short lev) throws IOException {
		addCommandX(META_RESTOREDC, lev);
	}

	void RoundRectangle(short left, short top, short right, short bottom,
			short wi, short h) throws IOException {
		addRecord(META_ROUNDRECT, 6);
		writeS16(h);
		writeS16(wi);
		writeS16(bottom);
		writeS16(right);
		writeS16(top);
		writeS16(left);
	}

	void SaveDC() throws IOException {
		addCommand(META_SAVEDC);
	}

	void ScaleViewportExt(short xnum, short xdenom, short ynum, short ydenom)
			throws IOException {
		addRecord(META_SCALEVIEWPORTEXT, 4);
		writeS16(ydenom);
		writeS16(ynum);
		writeS16(xdenom);
		writeS16(xnum);
	}

	void ScaleWindowExt(short xnum, short xdenom, short ynum, short ydenom)
			throws IOException {
		addRecord(META_SCALEWINDOWEXT, 4);
		writeS16(ydenom);
		writeS16(ynum);
		writeS16(xdenom);
		writeS16(xnum);
	}

	void SelectClipRgn(short nregion) throws IOException {
		addCommandX(META_SELECTCLIPREGION, nregion);
	}

	void SelectObject(short nobj) throws IOException {
		addCommandX(META_SELECTOBJECT, nobj);
	}

	void SetBKColor(long color) throws IOException {
		addCommandL(META_SETBKCOLOR, color);
	}

	void SetBKMode(short mode) throws IOException {
		addCommandX(META_SETBKMODE, mode);
	}

	void SetMapMode(short map) throws IOException {
		addCommandX(META_SETMAPMODE, map);
	}

	void SetPixel(short x, short y, long colour) throws IOException {
		addCommandXYL(META_SETPIXEL, x, y, colour);
	}

	void SetPixel(WMFPoint p, long colour) throws IOException {
		SetPixel(p.x, p.y, colour);
	}

	/**
	 * Sets the way polygons are filled.
	 * 
	 * @param fillmode :
	 *            one of ALTERNATE, WINDING, POLYFILL_LAST (?).
	 * @throws IOException
	 */
	void SetPolyFillMode(short fillmode) throws IOException {
		addCommandX(META_SETPOLYFILLMODE, fillmode);
	}

	void SetROP2(long rop) throws IOException {
		addCommandL(META_SETROP2, rop);
	}

	void SetTextCharacterExtra(short extra) throws IOException {
		addCommandX(META_SETTEXTCOLOR, extra);
	}

	void SetTextColor(long color) throws IOException {
		addCommandL(META_SETTEXTCOLOR, color);
	}

	void setViewportExt(short x, short y) throws IOException {
		addCommandXY(META_SETVIEWPORTEXT, x, y);
	}

	void setViewportExt(WMFPoint p) throws IOException {
		setViewportExt(p.x, p.y);
	}

	void setViewportOrg(short x, short y) throws IOException {
		addCommandXY(META_SETVIEWPORTORG, x, y);
	}

	void setWindowExt(short x, short y) throws IOException {
		addCommandXY(META_SETWINDOWEXT, x, y);
	}

	void setWindowExt(WMFPoint p) throws IOException {
		setWindowExt(p.x, p.y);
	}

	void setWindowOrg(short x, short y) throws IOException {
		addCommandXY(META_SETWINDOWORG, x, y);
	}

	void textOut(short x, short y, char s[]) throws IOException {
		int i;
		short length = (short) s.length;
		addRecord(META_TEXTOUT, (length + 1) / 2 + 3);
		/* count of chars in the string */
		writeS16((short) length);
		/* the string (16bit aligned) */
		for (i = 0; i < length; i++)
			writeU8(s[i]);
		/* ensures a even number of chars */
		if (length % 2 != 0)
			writeU8((char) 0);
		/* y */
		writeS16(y);
		/* x */
		writeS16(x);
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

	void textOut(WMFPoint p, char s[]) throws IOException {
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
		writeS32(6);
		// LONG Width; /* Width of the metafile */
		writeS32(meta2.Right);		
		// LONG Height; /* Height of the metafile */
		writeS32( meta2.Bottom);
		// LONG Handle; /* Handle to the metafile in memory */
		writeS32(0);
	}

	private void writeS16(short x) throws IOException {
		int s0, s1;
		s0 = (x & 0xFF);
		s1 = (x & 0xFF00) >>> 8;
		out.write(s0);
		out.write(s1);
	}

	private void writeS32(long x32) throws IOException {
		int x = (int) x32;
		int s0, s1, s2, s3;
		s0 = (x & 0xFF);
		s1 = (x & 0xFF00) >>> 8;
		s2 = (x & 0xFF0000) >>> 16;
		s3 = (x & 0xFF000000) >>> 24;

		out.write(s0);
		out.write(s1);
		out.write(s2);
		out.write(s3);
	}

	private void writeU16(int x) throws IOException {
		int s0, s1;
		s0 = (x & 0xFF);
		s1 = (x & 0xFF00) >>> 8;
		out.write(s0);
		out.write(s1);
	}

	private void writeU8(char c) throws IOException {
		out.write((int) c);
	}
}