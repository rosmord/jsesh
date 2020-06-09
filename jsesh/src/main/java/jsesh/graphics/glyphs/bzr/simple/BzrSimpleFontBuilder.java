package jsesh.graphics.glyphs.bzr.simple;

import java.awt.geom.GeneralPath;

import jsesh.graphics.glyphs.bzr.BzrFontBuilder;
import jsesh.hieroglyphs.graphics.ShapeChar;

/**
 * BzrSimpleFontBuilder builds demo fonts from a BZR file. Note that in BZR
 * files the y coordinates are oriented toward the top, whereas the "normal"
 * orientation for Java 2D graphics is reversed. To avoid further complication,
 * in particular in respect to font use, we conform to J2D.
 * 
 * Created: Fri Jun 7 12:37:00 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC
 *         </a>
 * 
 */

public class BzrSimpleFontBuilder implements BzrFontBuilder {

	private BzrSimpleFont font;

	private ShapeChar currentChar;

	private GeneralPath path;

	private double ury;

	public BzrSimpleFontBuilder() {
		font = null;
	}

	public BzrSimpleFont getFont() {
		return font;
	}

	// implementation of jsesh.graphics.glyphs.bzr.BzrFontBuilder interface

	/**
	 * 
	 * @param param1
	 *            <description>
	 */
	public void setSize(double param1) {
		// NO OP
	}

	/**
	 *  
	 */
	public void reset() {
		font = new BzrSimpleFont();
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 */
	public void setFontName(String param1) {
		// TODO: implement this jsesh.graphics.glyphs.bzr.BzrFontBuilder method
	}

	/**
	 * Called when the reader starts reading a new char.
	 * 
	 * @param code
	 *            the code of the char, between 0 and 255.
	 * @param width
	 *            the width of char.
	 * @param left
	 *            leftmost x position for the char
	 * @param bottom
	 *            y position for the char (y axis is upward-oriented)
	 * @param right
	 *            rightmost x position for the char
	 * @param up
	 *            upper y position for the char.
	 */
	public void startChar(int code, double width, double left, double bottom,
			double right, double up) {
		currentChar = new ShapeChar();
		font.setChar(code, currentChar);
		ury = up;
		// y axis goes downward.
		currentChar.setBBox(left, up - bottom, right, 0.0);
		path = new GeneralPath();
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 */
	public void newPath(double param1, double param2) {
		path.moveTo(convertX(param1), convertY(param2));
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 */
	public void newCurve(int param1, double param2, double param3) {
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 */
	public void newClosedCurve(int param1, double param2, double param3) {
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 */
	public void addLineSegment(double param1, double param2) {
		path.lineTo(convertX(param1), convertY(param2));
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 * @param param4
	 *            <description>
	 * @param param5
	 *            <description>
	 * @param param6
	 *            <description>
	 */
	public void addSplineSegment(double param1, double param2, double param3,
			double param4, double param5, double param6) {
		path.curveTo(convertX(param1), convertY(param2), convertX(param3),
				convertY(param4), convertX(param5), convertY(param6));
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 */
	public void pathEnd(boolean param1) {
		// TODO : implement this jsesh.graphics.glyphs.bzr.BzrFontBuilder method
	}

	/**
	 *  
	 */
	public void charEnd() {
		currentChar.setShape(path);
		//currentChar.fixShape();
	}

	/**
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 * @param param4
	 *            <description>
	 */
	public void setBoundingBox(double param1, double param2, double param3,
			double param4) {
		
	}

	/**
	 *  
	 */
	public void fontEnd() {
		
	}

	private float convertX(double x) {
		return (float) x;
	}

	private float convertY(double y) {
		return (float) (ury - y);
	}
}// BzrSimpleFontBuilder

