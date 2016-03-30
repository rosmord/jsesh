/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

/**
 *
 * @author rosmord
 */
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
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.util.Properties;
import javafx.scene.canvas.GraphicsContext;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;
import org.qenherkhopeshef.graphics.generic.StreamGraphicsConfiguration;

/**
 * Quick hack for producing SVG graphics (and avoiding huuuuuge libraries).
 * Should probably be improved and cleaned up, as it is mainly a cut and paste
 * from WMFGraphics2D. Some kind of "vector graphics" base class would be
 * welcome.
 *
 * <p>
 * NOTE : when using draw operators, the stroke command with CMYK values fails
 * in web browsers. It's perhaps a problem of SVG level ?
 */
public class MyG2D extends BaseGraphics2D {

    /**
     * The device.
     */
    GraphicsContext out;

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
    private boolean alwaysFill = false;

    /**
     * Creates a SVGGraphics2D in the file f.
     */
    public MyG2D(GraphicsContext out) {        
        this.out = out;        
        setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

      /**
     * Creates a SVGGraphics2D in the file f.
     */
    private MyG2D(MyG2D other) {
        super(other);
        this.out= other.out;
    }



    public void draw(Shape shape) {
        if (alwaysFill) {
            super.draw(shape);
        } else if (shape instanceof Line2D) {
            Line2D l = (Line2D) shape;
            Point2D a = this.deviceCoords(l.getP1());
            Point2D b = this.deviceCoords(l.getP2());
            System.err.println(b.getX());
            out.beginPath();
            out.moveTo(a.getX(), a.getY());
            out.lineTo(b.getX(), b.getY());
            out.closePath();
            out.stroke();
        } else {
            addPath(shape);
            //out.setFill(getColor());
            out.stroke();
        }
    }

    private void addPath(Shape shape) {
        boolean closed= false;        
        out.beginPath();

        PathIterator iter = shape.getPathIterator(null);

        Point2D first = null;

        while (!iter.isDone()) {
            double coords[] = new double[6];
            int type = iter.currentSegment(coords);
            Point2D points[] = getPointsInDeviceSpace(coords); // create new points
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    out.closePath();
                    closed= true;
                    break;
                case PathIterator.SEG_CUBICTO:
                    out.bezierCurveTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
                    //out.cubicTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
                    break;
                case PathIterator.SEG_LINETO:
                    out.lineTo(points[0].getX(), points[0].getY());
                    break;
                case PathIterator.SEG_MOVETO:
                    first = points[0];
                    out.moveTo(first.getX(), first.getY());
                    break;
                case PathIterator.SEG_QUADTO:
                    out.quadraticCurveTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY());
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
        out.setFill(javafx.scene.paint.Color.BLACK);
        //out.setForeground(getColor().getRed(), getColor().getGreen(), getColor().getBlue(), getColor().getAlpha());
        //out.setBackground(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), getBackground().getAlpha());
        out.fill();
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
            //out.setStrokeWidth((int) Math.ceil(p.getX() - p0.getX()));
        }
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // NO OP
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

    @Override
    public Graphics create() {       
        return new MyG2D(this);        
    }

    @Override
    public void dispose() {
    }

  

}
