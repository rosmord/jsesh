package org.qenherkhopeshef.graphics.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.StreamGraphicsConfiguration;
import org.qenherkhopeshef.graphics.utils.DoubleDimensions;

/**
 * Quick hack for producing SVG graphics (and avoiding huuuuuge libraries).
 * Should probably be improved and cleaned up, as it is mainly a cut and paste
 * from WMFGraphics2D. Some kind of "vector graphics" base class would be
 * welcome.
 * 
 * <p> NOTE : when using draw operators, the stroke command with CMYK values fails 
 * in web browsers. It's perhaps a problem of SVG level ?
 */
public class SVGGraphics2D extends BaseGraphics2D {

    /**
     * The SVG device.
     */
    SVGLowLevel svgOut;

    // TODO : use this value somewhere...
    //private float currentLineWidth;
    /**
     * Picture dimensions, in px.
     */
    private java.awt.geom.Dimension2D dimensions;

    /**
     * Curves maximal error.
     */
    private double precision = 0.05;

    /**
     * True if the graphic context's disposal should close the file. (when
     * copies are made with create(), their disposal should not close the wmf
     * file).
     */
    private boolean closeOnDispose;

    /**
     * If true, always use fill for drawing. This ensures line width is the one
     * computed by graphics2D.
     */
    private boolean alwaysFill= false;

    /**
     * Creates a SVGGraphics2D in the file f.
     *
     * @param f
     * @param dims : graphics dimensions, in points.
     * @throws IOException
     */
    public SVGGraphics2D(File f, java.awt.geom.Dimension2D dims) throws IOException {
        this(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"), dims);
    }

    /**
     * Creates a SVGGraphics2D in the file fname.
     *
     * @param fname path of the file
     * @param dims dimensions, expressed in points
     * @throws IOException
     */
    public SVGGraphics2D(String fname, java.awt.geom.Dimension2D dims) throws IOException {
        this(new File(fname), dims);
    }

    /**
     * Create a SVGGraphics2D graphics which will be stored in stream. Stream
     * may encapsulate a RandomAccessFile or a simple byte array. Note that the
     * Writer encoding <b>must</b> be UTF-8.
     *
     * @param writer
     * @param dims dimensions, expressed in points
     */
    public SVGGraphics2D(Writer writer, Dimension2D dims) throws IOException {
        closeOnDispose = true;
        dimensions = new DoubleDimensions(dims.getWidth(), dims.getHeight());
        svgOut = new SVGLowLevel(writer, dimensions);
        setFont(new Font("SansSerif", Font.PLAIN, 12));

    }

    private SVGGraphics2D(SVGGraphics2D g) {
        super(g);
        dimensions = g.dimensions;
        svgOut = g.svgOut;
        closeOnDispose = false;
    }

    public void draw(Shape shape) {
        if (alwaysFill) {
            super.draw(shape);
        } else {
            try {
                if (shape instanceof Line2D) {
                    Line2D l = (Line2D) shape;
                    Point2D a = this.deviceCoords(l.getP1());
                    Point2D b = this.deviceCoords(l.getP2());

                    svgOut.drawLine(a.getX(), a.getY(), b
                            .getX(), b.getY());
                } else {
                    addPath(shape);
                    svgOut.setForeground(getColor().getRed(), getColor().getGreen(), getColor().getBlue(), getColor().getAlpha());
                    svgOut.setBackground(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), getBackground().getAlpha());
                    svgOut.drawPath();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addPath(Shape shape) {
        svgOut.startPath();

        PathIterator iter = shape.getPathIterator(null);

        Point2D first = null;

        while (!iter.isDone()) {
            double coords[] = new double[6];
            int type = iter.currentSegment(coords);
            Point2D points[] = getPointsInDeviceSpace(coords); // create new points
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    svgOut.closePath();
                    break;
                case PathIterator.SEG_CUBICTO:
                    svgOut.cubicTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
                    break;
                case PathIterator.SEG_LINETO:
                    svgOut.lineTo(points[0].getX(), points[0].getY());
                    break;
                case PathIterator.SEG_MOVETO:
                    first = points[0];
                    svgOut.moveTo(first.getX(), first.getY());
                    break;
                case PathIterator.SEG_QUADTO:
                    svgOut.quadTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY());
                    break;
                default:
                    throw new RuntimeException(
                            "unexpected constant in path iterator");
            }
            iter.next();
        }
    }

    /**
     * fill a shape.
     *
     * @param shape
     */
    @Override
    public void fill(Shape shape) {
        addPath(shape);
        try {
            svgOut.setForeground(getColor().getRed(), getColor().getGreen(), getColor().getBlue(), getColor().getAlpha());
            svgOut.setBackground(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), getBackground().getAlpha());
            svgOut.fillPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getPrecision() {
        return precision;
    }

    /**
     * Sets the maximal error between theoretical curves and what is drawn. The
     * greater the precision, the nicer the curves... but the larger the file.
     *
     * @param precision
     */
    public void setPrecision(double precision) {
        this.precision = precision;
    }

    @Override
    public void setStroke(Stroke stroke) {
        super.setStroke(stroke);
        if (stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke) stroke;
            //currentLineWidth = bs.getLineWidth();
            float lineWidth = bs.getLineWidth();
            Point2D p = deviceCoords(new Point2D.Float(lineWidth, lineWidth));
            Point2D p0 = deviceCoords(new Point2D.Float(0, 0));
            svgOut.setStrokeWidth((int) Math.ceil(p.getX() - p0.getX()));
        }
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // NO OP
    }

    public Graphics create() {
        return new SVGGraphics2D(this);
    }

    public void dispose() {
        try {
            if (closeOnDispose && svgOut != null) {
                svgOut.close();
                svgOut = null;
            } else {
                svgOut = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        // NO OP
        return false;
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // NO OP

    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return new StreamGraphicsConfiguration(
                new Rectangle(0, 0, 10000, 10000), new AffineTransform());
    }

    public FontRenderContext getFontRenderContext() {
        // IMPORTANT :
        return new FontRenderContext(getTransform(), false, true);
    }

    public void setPaintMode() {
        // NO OP

    }

    public void setXORMode(Color c1) {
        // NO OP
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.graphics.wmfexport.BaseGraphics2D#setProperties(java.util.Properties)
     */
    public void setProperties(Properties properties) {
        // NO OP

    }

    /**
     * Takes coordinates in the current space, and transform them to device
     * space. device space is constant for a specific device.
     *
     * @param p
     * @return
     */
    private Point2D deviceCoords(Point2D p) {
        Point2D res = new Point2D.Double();
        getTransform().transform(p, res);
        return res;
    }

    private Point2D[] getPointsInDeviceSpace(double coords[]) {
        Point2D points[] = new Point2D[coords.length / 2];
        for (int i = 0; i < points.length; i++) {
            Point2D.Double p = new Point2D.Double(coords[i * 2], coords[i * 2 + 1]);
            points[i] = deviceCoords(p);
        }
        return points;
    }

    public boolean isAlwaysFill() {
        return alwaysFill;
    }

    public void setAlwaysFill(boolean alwaysFill) {
        this.alwaysFill = alwaysFill;
    }
    
    /**
     * Ask for usage (or not) of SVG 1.2 CMYK colour.
     * <p> This is very rarely supported by softwares. However, as printing 
     * is our main use-case, we support it.
     * @param useCmyk 
     */
    public void useCmyk(boolean useCmyk) {
        svgOut.setUseCmyk(useCmyk);
    }

}
