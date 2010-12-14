package jsesh.graphics.glyphs.bzr;

/**
 * interface used for building actual representation of a BZR font.
 * 
 * Use design pattern builder. Role in Design Pattern is Builder.
 *
 * Created: Sun Jun  2 12:43:07 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public interface BzrFontBuilder {
    
    /**
     * Initialise a builder for reading a new BZR font file.
     *
     */
    void reset();

    /*
     * Sets the base size of the font. 
     * Usually not very useful : it only means that values stored in the BZR file 
     * are supposed to be multiplied by size. The said multiplication is already done 
     * by the BZR font reader, hence the implementation for this one might well do nothing.
     *
     * @param size base size for the font.
     */
    void setSize(double size);

    /**
     * Sets the font name.
     *
     * @param name the font name
     */
    
    void setFontName(String name);

    
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
    
    void startChar(int code, double width, double left, double bottom, double right, double up);
    
    /**
     * Called when starting a new path.
     *
     * @param firstx a <code>double</code> value
     * @param firsty a <code>double</code> value
     * @see BzrCodes
     */
    
    void newPath(double firstx, double firsty);

    /**
     * Called when starting a new curve
     *
     * @param width an <code>int</code> code for the line width. Its exact meaning is not yet decided.
     * @param firstx a <code>double</code> value
     * @param firsty a <code>double</code> value
     */
    void newCurve(int width, double firstx, double firsty);

    /**
     * Describe <code>newClosedCurve</code> method here.
     *
     * @param width an <code>int</code> value
     * @param firstx a <code>double</code> value
     * @param firsty a <code>double</code> value
     */
    void newClosedCurve(int width, double firstx, double firsty);

    /**
     * Describe <code>addLineSegment</code> method here.
     *
     * @param px a <code>double</code> value
     * @param py a <code>double</code> value
     */
    
    void addLineSegment(double px,double py);

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
    void addSplineSegment(double c1x, double c1y, double c2x, double c2y, double px, double py);

  /**
   * called when a path ends.
   *
   * @param isPositive is true if the path is positive (i.e. should be
   * drawn in black) and false if the path is negative (i.e. should be drawn in white).
   */
  void pathEnd(boolean isPositive);

    /**
     * called when a char ends.
     *
     */
    void charEnd();

    /**
     * sets the <b>font</b> bounding box.
     *
     * @param llx a <code>double</code> value
     * @param lly a <code>double</code> value
     * @param urx a <code>double</code> value
     * @param ury a <code>double</code> value
     */
    void setBoundingBox(double llx, double lly, double urx, double ury);

    /**
     * called when the whole font has been read.
     *
     */
    void fontEnd();

}// BzrFontBuilder
