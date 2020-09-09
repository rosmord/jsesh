
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.utils;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Various geometry oriented methods.
 * @author rosmord
 */
public class GeometryHelper {
    
    /**
     * Returns a vector between two points.
     * @param initial
     * @param terminal
     * @return 
     */
    public static Point2D computeVector(Point2D initial, Point2D terminal) {
        double x = terminal.getX() - initial.getX();
        double y = terminal.getY() - initial.getY();
        return new Point2D.Double(x, y);
    }
    
    /**
     * Compute angle (oa,ob), in degrees.
     * If either oa or ob is the null vector, not defined.
     * @param o the center of the angle
     * @param a 
     * @param b
     * @return the angle, if it can be defined.
     */
    public static Optional<Integer> computeAngle(Point2D o, Point2D a, Point2D b) {        
        Point2D v1 = GeometryHelper.computeVector(o, a);
        Point2D v2 = GeometryHelper.computeVector(o, b);
        // Normalize them and compute cos and sin.
        double d1 = v1.distance(0, 0);
        double d2 = v2.distance(0, 0);
        if (d1 >= 0 && d2 >= 0) {
            double cos = ensureLimits((v1.getX() * v2.getX() + v1.getY() * v2.getY())
                    / (d1 * d2));
            double sin = ensureLimits((v1.getX() * v2.getY() - v1.getY() * v2.getX())
                    / (d1 * d2));
            // Use cos and sin to compute the angle (in radian)
            double alpha = Math.acos(cos); // angle in radian.

            if (sin < 0.0) {
                alpha = 2 * Math.PI - alpha;
            }
            int angle = (int) ((alpha * 180.0 / Math.PI));            
            return Optional.of(angle);
        } else {
            return Optional.empty();
        }
    }

     /**
     * Ensure the argument is between -1 and 1. Cos and sin are supposed to be
     * between -1 and 1, but small approximations might lead to values out of
     * range.
     *
     * @param d
     * @return d or -1 or 1, if d is out of range.
     */
    private static double ensureLimits(double d) {
        if (d < -1.0) {
            return -1.0;
        } else if (d > 1.0) {
            return 1.0;
        } else {
            return d;
        }
    }
    
    
    /**
     * Ensures an angle stands between 0 and 359.
     * @param angle
     * @return 
     */
   public static int normalizeAngle(int angle) {
       if (angle < 0) {
           return (angle % 360) + 360 ; // % will return a negative number here.
       } else if (angle >= 360) {
           return angle % 360;
       } else {
           return angle;
       }
   }

   /**
    * Computes the ratio of length oa/ob.
    * @param o
    * @param a
    * @param b
    * @return 
    */
    public static double lengthRatio(Point2D o, Point2D a, Point2D b) {
        double d1Sq = o.distanceSq(a);
        double d2Sq = o.distanceSq(b);
        return Math.sqrt(d1Sq/d2Sq);
    }
    
    /**
     * Computes the image of point p by an affine homothetie.
     * @param center center of the homothetie
     * @param ratio ratio of the homothetie
     * @param p point for which we want an image.
     * @return 
     */
    public static Point2D homotheticImage(Point2D center, double ratio, Point2D p) {
        double dx = p.getX() - center.getX();
        double dy = p.getY() - center.getY();
        return new Point2D.Double(
                center.getX() + ratio * dx,
                center.getY() + ratio * dy
        );
    }

    public static Point2D clonePoint(Point2D position) {
        return new Point2D.Double(position.getX(), position.getY());
    }
}
