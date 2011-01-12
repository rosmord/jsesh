package jsesh.graphics.glyphs.bzr;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * A class which can be used to produce a readable representation for a BZR file.
 * mainly for test purposes.
 *
 * Created: Sun Jun  2 16:34:46 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public class BzrToStringBuilder implements BzrFontBuilder {
    String string;
    
    /**
     * Get the value of string.
     * @return value of string.
     */
    public String getString() {
	return string;
    }
    /**
     *
     * @param param1 <description>
     */
    public void setSize(double param1)
    {
	addTag("size", "" + param1);
    }

    /**
     *
     */
    public void reset()
    {
	string= "<font>\n";
    }

    /**
     *
     * @param param1 <description>
     */
    public void setFontName(String param1)
    {
	addTag("name", param1);
    }
    
    /**
     * Called when the reader starts reading a new char.
     *
     * @param code the code of the char, between 0 and 255.
     * @param width the width of char.
     * @param left leftmost x position for the char
     * @param bottom y position for the char (y axis is upward-oriented)
     * @param right rightmost x position for the char
     * @param up upper y position for the char.
     */

    public void startChar(int code, double width, double left, double bottom, double right, double up)
    {
	openTag("char");
	addAttr("code", ""+ code);
	addAttr("width", ""+ width);
	addAttr("left", ""+ left);
	addAttr("bottom", ""+ bottom);
	addAttr("right", ""+ right);
	addAttr("up", ""+ up);
	closeTag();
    }

    /**
     * Called when starting a new path.
     *
     * @param firstx a <code>double</code> value
     * @param firsty a <code>double</code> value
     * @see BzrCodes
     */
    
    public void newPath(double firstx, double firsty)
    {
	openTag("path");
	addAttr("firstx", ""+ firstx);
	addAttr("firsty", ""+ firsty);
	closeTag();
    }

    /**
     * Called when starting a new curve
     *
     * @param width an <code>int</code> code for the line width. Its exact meaning is not yet decided.
     * @param firstx a <code>double</code> value
     * @param firsty a <code>double</code> value
     */
    public void newCurve(int width, double firstx, double firsty)
    {
	openTag("curve");
	addAttr("width", ""+ width);
	addAttr("firstx", ""+ firstx);
	addAttr("firsty", ""+ firsty);
	closeTag();
    }

    /**
     * Describe <code>newClosedCurve</code> method here.
     *
     * @param width an <code>int</code> value
     * @param firstx a <code>double</code> value
     * @param firsty a <code>double</code> value
     */
    public void newClosedCurve(int width, double firstx, double firsty)
    {
	openTag("closedcurve");
	addAttr("width", ""+ width);
	addAttr("firstx", ""+ firstx);
	addAttr("firsty", ""+ firsty);
	closeTag();
    }
    
    /**
     * Describe <code>addLineSegment</code> method here.
     *
     * @param px a <code>double</code> value
     * @param py a <code>double</code> value
     */
    
    public void addLineSegment(double px,double py)
    {
	addTag("moveTo", "" + px + " " + py);
    }
    
    /**
     * Describe <code>addSplineSegment</code> method here.
     *
     * @param c1x a <code>double</code> value
     * @param c1y a <code>double</code> value
     * @param c2x a <code>double</code> value
     * @param c2y a <code>double</code> value
     * @param px a <code>double</code> value
     * @param py a <code>double</code> value
     */
    public void addSplineSegment(double c1x, double c1y, double c2x, double c2y, double px, double py)
    {
	addTag("curveTo", ""+ c1x + " "+  c1y+ " "+  c2x+ " "+  c2y+ " "+  px+ " "+  py+ " ");
    }

    /**
     * called when a path ends.
     *
     * @param isPositive is true if the path is positive (i.e. should be
     * drawn in black) and false if the path is negative (i.e. should be drawn in white).
     */
    public void pathEnd(boolean isPositive)
    {
	addTag("positive", "" + isPositive);
	closingTag("path");
    }

    /**
     * called when a char ends.
     *
     */
    public void charEnd()
    {
	closingTag("char");
    }

    /**
     * sets the <b>font</b> bounding box.
     *
     * @param llx a <code>double</code> value
     * @param lly a <code>double</code> value
     * @param urx a <code>double</code> value
     * @param ury a <code>double</code> value
     */
    public void setBoundingBox(double llx, double lly, double urx, double ury)
    {
	addTag("fontBB", "" + llx+ " "+ lly + " "+ urx + " " + ury);
    }

    /**
     * called when the whole font has been read.
     *
     */
    public void fontEnd()
    {
	closingTag("font");
    }

    // There should be an XML builder class

    private void addTag(String tag, String tagVal)
    {
	string+= "<" + tag + ">" + tagVal + "</" + tag + ">\n";
    }

    private void addAttr(String attrName, String attrval)
    {
	string += attrName +"=\""+ attrval + "\" ";
    }

    private void openTag(String tag)
    {
	string+= "<" + tag + " ";
    }

    private void closeTag()
    {
	string+= ">";
    }

    private void closingTag(String tag)
    {
	string+= "</"+ tag+ ">\n";
    }

    public static void main(String[] args) throws IOException, BzrFormatException
    {
	BzrToStringBuilder builder= new BzrToStringBuilder();
	BzrFontReader reader= new BzrFontReader(builder);
	reader.read(new FileInputStream(args[0]));
	System.out.println("Bzr file is \n"+ builder.getString());
    }
}
