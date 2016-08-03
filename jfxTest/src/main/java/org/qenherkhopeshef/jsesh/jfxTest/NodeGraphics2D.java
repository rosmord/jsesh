/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.util.Properties;
import javafx.scene.layout.Region;
import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;

/**
 * A graphics 2D which will create a node-oriented representation.
 * <p> Probably not for the final version ?
 * @author rosmord
 */
public class NodeGraphics2D extends BaseGraphics2D {

    Region root = new Region();

    public NodeGraphics2D() {
    }

    public NodeGraphics2D(BaseGraphics2D g) {
        super(g);
    }

    public Region getRoot() {
        return root;
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    @Override
    public Graphics create() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return new NodeGraphics2D(this);
    }

    @Override
    public void dispose() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return false;
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fill(Shape s) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        addPath(s);
    }

    private void addPath(Shape shape) {
        boolean closed = false;

        PathIterator iter = shape.getPathIterator(null);

        Point2D first = null;

        while (!iter.isDone()) {
            double coords[] = new double[6];
            int type = iter.currentSegment(coords);
            Point2D points[] = getPointsInDeviceSpace(coords); // create new points
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    //out.closePath();
                    closed = true;
                    break;
                case PathIterator.SEG_CUBICTO:
                    //out.bezierCurveTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
                    //out.cubicTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
                    break;
                case PathIterator.SEG_LINETO:
                    //out.lineTo(points[0].getX(), points[0].getY());
                    break;
                case PathIterator.SEG_MOVETO:
                    first = points[0];
                    //out.moveTo(first.getX(), first.getY());
                    break;
                case PathIterator.SEG_QUADTO:
                    //out.quadraticCurveTo(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY());
                    break;
                default:
                    throw new RuntimeException(
                            "unexpected constant in path iterator");
            }
            iter.next();
        }

    }

    @Override
    public void setPaintMode() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setXORMode(Color c1) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperties(Properties properties) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
