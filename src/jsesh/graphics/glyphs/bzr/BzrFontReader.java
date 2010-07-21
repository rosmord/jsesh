package jsesh.graphics.glyphs.bzr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BzrFontReader reads a BZR file and calls a BzrFontBuilder to process it. 
 *
 * Use design pattern builder. Role in Design Pattern is Director.
 * Created: Sun Jun  2 12:29:25 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */


public class BzrFontReader implements BzrCodes {
  
	private BzrFontBuilder builder;
	private BzrPortableFile in;
	private int c;
	private double size;
  
	public BzrFontReader (BzrFontBuilder builder)
	{
		this.builder= builder;
	}
  
	public void read(InputStream in) throws BzrFormatException, IOException
	{
		builder.reset();
		this.in = new BzrPortableFile(new BufferedInputStream(in));
		readHeader();
		readChars();
		readPostFace();
		in.close();
	}
  
	private void readHeader() throws IOException
	{
		// size
		size= in.readBDouble();
		// is it of any use for builder ?
		builder.setSize(size);
		// font name
		builder.setFontName(in.readString());
	}

	private void readChars() throws IOException, BzrFormatException
	{
		// Used for area computations

		while ((c= in.skipNops()) != POST)
	    {
				double width, llx, lly, urx, ury;
				// c is either BOC or BOC_ABBREV.
				// Then comes the character code.
				int code= in.read();
				// The char dimensions are expressed differently if c is BOC
				// or BOC_ABBREV :
				switch (c) {
				case BOC_ABBREV :
					width= readSDouble();
					llx= readSDouble();
					lly= readSDouble();
					urx= readSDouble();
					ury= readSDouble();
					break;
				case BOC:
					width= readBDouble();
					llx= readBDouble();
					lly= readBDouble();
					urx= readBDouble();
					ury= readBDouble();
					break;
				default:
					throw new BzrFormatException("found char " + c + "while expecting beginning of char indicator");
				}
	
				// Now we have char dimensions and code.
		
				builder.startChar(code, width, llx, lly, urx, ury);
	
				// Read the pathes which make the char :
				c= in.skipNops();
				while (c != EOC)
					{
						// Control points
						double p1x, p1y;
						double p2x, p2y;
						double c1x= 0, c1y= 0;
						double c2x= 0, c2y= 0;
						// Geometric area for this path.
						double area= 0.0;
			
						int pathType= c;
			
						p1x= readBDouble();
						p1y= readBDouble();
			
						switch(pathType) {
						case START_PATH:
							builder.newPath(p1x, p1y);
							break;
						case START_CURVE:
							builder.newCurve(1, p1x, p1y);
							break;
						case START_NOFIL:
							builder.newClosedCurve(1, p1x, p1y);
							break;
						default:
							throw new BzrFormatException("unsupported path type " + pathType);
						}

						// read the path :
						while ((c= in.skipNops()) != START_PATH 
									 && c != START_CURVE
									 && c != START_NOFIL
									 && c != EOC)
							{
								switch (c) {
								case LINE:
									p2x= readBDouble();
									p2y= readBDouble();
									builder.addLineSegment(p2x,p2y);
									break;
								case LINE_ABBREV:
									p2x= readSDouble();
									p2y= readSDouble();
									builder.addLineSegment(p2x,p2y);
									break;
								case SPLINE:
									c1x= readBDouble();
									c1y= readBDouble();
									c2x= readBDouble();
									c2y= readBDouble();
									p2x= readBDouble();
									p2y= readBDouble();
									builder.addSplineSegment(c1x, c1y, c2x, c2y, p2x, p2y);
									break;
								case SPLINE_ABBREV:
									c1x= readSDouble();
									c1y= readSDouble();
									c2x= readSDouble();
									c2y= readSDouble();
									p2x= readSDouble();
									p2y= readSDouble();
									builder.addSplineSegment(c1x, c1y, c2x, c2y, p2x, p2y);
									break;
								default:
									throw new BzrFormatException("unexpected char " + c);
								}

								// Now, fix the area :
								switch (c) {
								case LINE:
								case LINE_ABBREV:
									area+= (p2x -p1x) * (p2y + p1y);
									break;
								case SPLINE:
								case SPLINE_ABBREV:
									area+= jsesh.swingUtils.Bezier.area(p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y);
									break;
								}
								
								p1x= p2x;
								p1y= p2y;
							}
						builder.pathEnd(area >= 0.0);
					}
				builder.charEnd();
	    }
	}

	private void readPostFace() throws BzrFormatException, IOException
	{
		if (c != POST)
	    throw new BzrFormatException("unexpected char " + c);
		// Read the bounding box :
		double llx, lly, urx, ury;
		llx= readBDouble();
		lly= readBDouble();
		urx= readBDouble();
		ury= readBDouble();
		builder.setBoundingBox(llx,lly,urx, ury);
		builder.fontEnd();
	}

 
	/**
	 * Reads size-relative double
	 * @return scaled double value
	 * @throws IOException
	 */
	private double readSDouble() throws IOException
	{
		return in.readSDouble() * size;
	}

	/**
	 * Reads size-relative double
	 * @return scaled double value
	 * @throws IOException
	 */

	private double readBDouble() throws IOException
	{
		return in.readBDouble() * size;
	}
  
}// BzrFontReader



// Local Variables:
// tab-width: 2
// End:
