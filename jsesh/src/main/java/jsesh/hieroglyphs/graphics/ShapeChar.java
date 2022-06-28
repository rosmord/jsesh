package jsesh.hieroglyphs.graphics;

import jsesh.hieroglyphs.graphics.LigatureZone;
import jsesh.hieroglyphs.graphics.HorizontalGravity;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import jsesh.swing.utils.ShapeHelper;
import jsesh.utils.DoubleFormatter;

/**
 * A glyph ; basically a shape which can be painted). moved out of the Bzr
 * package, as it's no longer even mainly used.
 *
 * Created: Mon Jun 10 10:49:26 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC
 * </a>
 *
 *
 * TODO: Include documentation about the signs (author, license, etc.) TODO: get
 * sure "fixShape" is always called when necessary (make it private?). The
 * problem is that fixShape should only be called *after* setting both shape and
 * ligature zones.
 */
public class ShapeChar implements Cloneable {

    /**
     * The precision used when saving the signs as SVG files. probably out of
     * place here.
     */
    private static final int PRECISION = 2;

    private Shape shape;

    private Rectangle2D bbox;

    private LigatureZone zones[];

    private String license; // the sign license, if any.

    private String documentation; // sign description

    private String author; // sign creator

    public ShapeChar() {
    }

    public Object clone() {
        ShapeChar result = null;
        try {
            result = (ShapeChar) super.clone();
            if (shape != null) {
                result.shape = new GeneralPath(shape);
            }
            if (bbox != null) {
                result.bbox = (Rectangle2D) bbox.clone();
            }
            if (zones != null) {
                result.zones = (LigatureZone[]) zones.clone();
                for (int i = 0; i < zones.length; i++) {
                    if (result.zones[i] != null) {
                        result.zones[i] = (LigatureZone) result.zones[i]
                                .clone();
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // Should not happen.
        }
        // The rest of the fields are immutable.
        return result;
    }

    /**
     * Get the value of bbox.
     *
     * @return value of bbox.
     */
    public Rectangle2D getBbox() {
        return bbox;
    }

    /**
     * Set the value of bbox.
     *
     * @param v Value to assign to bbox.
     */
    public void setBbox(Rectangle2D v) {
        this.bbox = v;
    }

    /**
     * sets this char's bounding box from coordinates
     *
     * @param llx a <code>double</code> value
     * @param lly a <code>double</code> value
     * @param urx a <code>double</code> value
     * @param ury a <code>double</code> value
     *
     */
    public void setBBox(double llx, double lly, double urx, double ury) {
        this.bbox = new Rectangle2D.Double(llx, ury, urx - llx, lly - ury);
    }

    /**
     * Get the value of shape.
     *
     * @return value of shape.
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Set the value of shape.
     *
     * @param v Value to assign to shape.
     */
    public void setShape(Shape v) {
        this.shape = v;
        fixShape();
    }

    /**
     *
     * @param g
     * @param x
     * @param y
     * @param xscale
     * @param yscale
     * @param angle rotation angle for this glyph, in radians.
     */
    public void draw(Graphics2D g, double x, double y, double xscale,
            double yscale, float angle) {
        // AffineTransform tr;
        // tr= g.getTransform();
        Graphics2D tmpG = (Graphics2D) g.create();
        tmpG.translate(x, y);
        Shape s = getShape();
        tmpG.scale(xscale, yscale);
        if (angle != 0) {
            // In case of rotation, compute the rotated shape (we need it to
            // know the sign placement).
            s = AffineTransform.getRotateInstance(angle)
                    .createTransformedShape(s);
            // Now, we move the top-left of this shape to 0,0
            Rectangle2D r = s.getBounds2D();
            tmpG.translate(-r.getMinX(), -r.getMinY());
        }
        // The following line doesn't do what it's supposed to do.
        tmpG.fill(s);
        // g.setTransform(tr);
        tmpG.dispose();
    }

    public Shape getTransformedShape(double x, double y, double xscale,
            double yscale, double angle) {
        Shape result = getShape();
        result = ShapeHelper.transformShape(x, y, xscale, yscale, angle, result);
        return result;
    }

    public Area getSignArea(double x, double y, double xscale, double yscale,
            double angle) {
        Area s1 = new Area();
        Shape s = getTransformedShape(x, y, xscale, yscale, angle);
        PathIterator iter = s.getPathIterator(null);
        GeneralPath current = null;
        while (!iter.isDone()) {
            float coords[] = new float[6];
            int type = iter.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    current.closePath();
                    s1.add(new Area(current));
                    current = null;
                    break;
                case PathIterator.SEG_CUBICTO:
                    current.curveTo(coords[0], coords[1], coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case PathIterator.SEG_LINETO:
                    current.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_MOVETO:
                    if (current != null) {
                        s1.add(new Area(current));
                    }
                    current = new GeneralPath(iter.getWindingRule());
                    current.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    current.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
            }
            iter.next();
        }
        if (current != null) {
            s1.add(new Area(current));
        }
        return s1;
    }

    /**
     * fixShape ensures the shapeChar is correct, using the shape as starting
     * point.
     * <p>
     * Fixes both the bounding box and the sign origin.
     */
    private void fixShape() {
        // Just to be sure of the bounding box...

        PathIterator it = new GeneralPath(shape).getPathIterator(null, 0.01);
        GeneralPath p = new GeneralPath();
        p.append(it, false);
        Rectangle2D r = p.getBounds2D();
        AffineTransform transform = AffineTransform.getTranslateInstance(-r
                .getMinX(), -r.getMinY());
        shape = transform.createTransformedShape(shape);
        if (zones != null) {
            for (int i = 0; i < zones.length; i++) {
                if (zones[i] != null) {
                    Rectangle2D rect = new Rectangle2D.Double(zones[i]
                            .getMinX()
                            - r.getMinX(), zones[i].getMinY() - r.getMinY(),
                            zones[i].getWidth(), zones[i].getHeight());
                    zones[i].setBox(rect);
                }
            }
        }
        setBBox(0, r.getMaxY() - r.getMinY(), r.getMaxX() - r.getMinX(), 0);
    }

    /**
     * Modify a shape char in order to get an height of exactly h units. Should
     * be used only in the context of sign importation and/or modifications.
     *
     * @param h : the height of the sign.
     */
    public void scaleToHeight(double h) {
        double s = h / getBbox().getHeight();
        scaleGlyph(s);
    }

    /**
     * Scale a sign globally. This scales all instances of the sign, and is a
     * font manipulation operation, not to be used in simple drawing.
     */
    public void scaleGlyph(double scale) {

        // scale zones.
        if (zones != null) {
            for (int i = 0; i < zones.length; i++) {
                if (zones[i] != null) {
                    zones[i].setBox(new Rectangle2D.Double(zones[i].getMinX()
                            * scale, zones[i].getMinY() * scale, zones[i]
                            .getWidth()
                            * scale, zones[i].getHeight() * scale));
                }
            }
        }
        setShape(AffineTransform.getScaleInstance(scale, scale)
                .createTransformedShape(getShape()));
    }

    /**
     * Saves the drawing of this sign as an SVG file. Note that the output
     * should be in ASCII (or UTF-8) encoding for best portability.
     *
     * <p>
     * About the jsesh namespace (http://jsesh.qenherkhopeshef.org/jseshg/1.0)
     * This namespace is currently used for informations which are not plain
     * SVG. This includes author and the like.
     *
     * @param o
     * @param encoding the name of the encoding to use. We suggest "UTF-8".
     * @param pictureOnly : if true, will only save the drawing of the sign, and
     * not the "ligature zones".
     * @throws java.io.IOException
     */
    public void exportToSVG(OutputStream o, String encoding, boolean pictureOnly) throws IOException {
        final String ZONE_STYLE = "fill:none;stroke:red;stroke-width:0.2;stroke-opacity:1";
        Writer out = new OutputStreamWriter(o, encoding);
        out.write("<?xml version='1.0' ");
        DoubleFormatter formatter = new DoubleFormatter(PRECISION);

        if (!"".equals(encoding)) {
            out.write("encoding ='" + encoding + "' ");
        }
        out.write("standalone='no'?>\n");
        out.write("<svg width='");
        formatter.writeTo(out, bbox.getWidth());
        out.write("' height='");
        formatter.writeTo(out, bbox.getHeight());
        out.write("' ");
        // Necessary according to firefox site.
        out.write("xmlns='http://www.w3.org/2000/svg' ");
        out.write("xmlns:xlink='http://www.w3.org/1999/xlink' ");
        out.write("version='1.1' ");
        if (!pictureOnly) {
            out
                    .write("xmlns:inkscape='http://www.inkscape.org/namespaces/inkscape'");
        }
        out.write(">\n");
        out.write("<path style='fill:black; stroke:none' d='");
        writeSVGPath(out, PRECISION);
        out.write("'/>");
        // The ligature zones.
        if (!pictureOnly && zones != null) {
            for (int i = 0; i < zones.length; i++) {
                if (zones[i] != null) {
                    out.write("<rect");
                    out.write(' ');
                    out.write("style='" + ZONE_STYLE + "'");
                    out.write(' ');
                    out.write("id='zone" + (i + 1) + "'");
                    out.write(' ');
                    out.write("width='" + zones[i].getWidth() + "'");
                    out.write(' ');
                    out.write("height='" + zones[i].getHeight() + "'");
                    out.write(' ');
                    out.write("x='" + zones[i].getMinX() + "'");
                    out.write(' ');
                    out.write("y='" + zones[i].getMinY() + "'");
                    out.write(' ');
                    String gravity = "";
                    if (!zones[i].getHorizontalGravity().equals(
                            HorizontalGravity.CENTER)) {
                        gravity += zones[i].getHorizontalGravity().getCode();
                    }
                    if (!zones[i].getVerticalGravity().equals(
                            VerticalGravity.CENTER)) {
                        gravity += zones[i].getVerticalGravity().getCode();
                    }
                    if (gravity.length() > 0) {
                        out.write("inkscape:label='gravity:" + gravity + "'");
                    }
                    out.write("/>");
                }
            }
        }
        out.write("</svg>\n");
        out.close();
    }

    /**
     * Saves the drawing of this sign as an SVG file. Note that the output
     * should be in ASCII (or UTF-8) encoding for best portability.
     * <p>
     * The resulting picture will be usable by JSesh, and include ligature zones
     * if any (see JSesh documentation about this).
     * <p>
     * About the jsesh namespace (http://jsesh.qenherkhopeshef.org/jseshg/1.0)
     * This namespace is currently used for informations which are not plain
     * SVG. This includes author and the like.
     *
     * @param o
     * @param encoding the name of the encoding to use. We suggest "UTF-8".
     * @throws java.io.IOException
     */
    public void exportToSVG(OutputStream o, String encoding) throws IOException {
        exportToSVG(o, encoding, false);
    }

    /**
     * Write the sign description as a string fit for a SVG path.
     *
     * @param out
     * @param precision number of digit to retain.
     * @throws IOException
     */
    public void writeSVGPath(Writer out, int precision) throws IOException {
        DoubleFormatter formatter = new DoubleFormatter(precision);
        PathIterator iter = shape.getPathIterator(null);

        while (!iter.isDone()) {
            float coords[] = new float[6];
            int type = iter.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    out.write(" Z ");
                    break;
                case PathIterator.SEG_CUBICTO:
                    out.write(" C ");
                    formatter.outputNumbers(out, coords, 6);
                    // outputNumbers(out, coords, 6,precision);
                    break;
                case PathIterator.SEG_LINETO:
                    out.write(" L ");
                    // outputNumbers(out, coords, 2, precision);
                    formatter.outputNumbers(out, coords, 2);
                    break;
                case PathIterator.SEG_MOVETO:
                    out.write(" M ");
                    // outputNumbers(out, coords, 2, precision);
                    formatter.outputNumbers(out, coords, 2);
                    break;
                case PathIterator.SEG_QUADTO:
                    out.write(" Q ");
                    // outputNumbers(out, coords, 4, precision);
                    formatter.outputNumbers(out, coords, 4);
                    break;
            }
            iter.next();
        }
    }

    public void setZone(int i, LigatureZone z) {
        if (zones == null) {
            zones = new LigatureZone[3];
        }
        zones[i] = z;
    }

    /**
     * Return an empty area suitable to fit ligatured group on this sign.
     *
     * @param i ordinal number of the area. Currently : 0 or 1 ("before" or
     * "after").
     * @return a rectangle, or null
     */
    public LigatureZone getZone(int i) {
        if (zones == null) {
            return null;
        } else {
            return zones[i];
        }
    }

    /**
     * Transform a sign globally; font changing method, not for use while
     * drawing.
     */
    public void flipHorizontally() {
        Shape newShape = new AffineTransform(-1, 0, 0, 1, (float) getBbox()
                .getWidth(), 0).createTransformedShape(shape);
        if (zones != null) {
            double w = getBbox().getWidth();
            for (int i = 0; i < zones.length; i++) {
                if (zones[i] != null) {
                    Rectangle2D r = zones[i].getBox();
                    // Apply symetry to the rectangle. Note that the "x" value
                    // is the x of the former right corner.
                    Rectangle2D.Double newRectangle = new Rectangle2D.Double(w
                            - r.getMaxX(), r.getMinY(), r.getWidth(), r
                            .getHeight());
                    zones[i].setBox(newRectangle);
                    zones[i].setHorizontalGravity(zones[i]
                            .getHorizontalGravity().flip());
                }
            }
        }
        setShape(newShape);
        // fixShape();
    }

    /**
     * Transform a sign globally; font changing method, not for use while
     * drawing.
     */
    public void flipVertically() {
        Shape newShape = new AffineTransform(1, 0, 0, -1, 0, (float) getBbox()
                .getHeight()).createTransformedShape(shape);
        if (zones != null) {
            double h = getBbox().getHeight();
            for (int i = 0; i < zones.length; i++) {
                if (zones[i] != null) {
                    Rectangle2D r = zones[i].getBox();
                    // Apply symetry to the rectangle. Note that the "x" value
                    // is the x of the former right corner.
                    Rectangle2D.Double newRectangle = new Rectangle2D.Double(r
                            .getMinX(), h - r.getMaxY(), r.getWidth(), r
                            .getHeight());
                    zones[i].setBox(newRectangle);
                    zones[i].setVerticalGravity(zones[i].getVerticalGravity()
                            .flip());
                }
            }
        }
        setShape(newShape);
        // fixShape();
    }

    /**
     * Are there ligatures zones for this sign ?
     * <p>
     * Ligature zones can be either computed or set in the sign description. If
     * this method returns false, the software can create ligature zones for the
     * sign.
     *
     * @return true if ligature zones are either defined or explicitely non
     * existant.
     */
    public boolean hasZones() {
        return (zones != null);
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * @param documentation the documentation to set
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * @return the license
     */
    public String getLicense() {
        return license;
    }

    /**
     * @param license the license to set
     */
    public void setLicense(String license) {
        this.license = license;
    }

} // ShapeChar
