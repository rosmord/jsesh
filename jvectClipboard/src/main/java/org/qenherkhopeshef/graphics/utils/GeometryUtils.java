package org.qenherkhopeshef.graphics.utils;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Utility class for geometry.
 * @author rosmord
 *
 */
public class GeometryUtils {
	
	/**
	 * Compute the algebric area of a polygon.
	 * 
	 * @param points a list of Points2D.
	 * @return the algebric area.
	 */
	public static double algebricArea(List points) {
		double area = 0;
		if (points.size() != 0) {
			Point2D first = (Point2D) points.get(0);
			Point2D old = first;
			for (int i = 1; i < points.size(); i++) {
				Point2D curr = (Point2D) points.get(i);
				area += (curr.getX() - old.getX()) * (curr.getY() + old.getY());
				old = curr;
			}
			area += (first.getX() - old.getX()) * (first.getY() + old.getY());
		}
		return area;
	}
	
}
