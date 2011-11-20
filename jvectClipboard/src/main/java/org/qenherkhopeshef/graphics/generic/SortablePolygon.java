package org.qenherkhopeshef.graphics.generic;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Polygons sortables by minimal abscisse.
 * 
 * @author rosmord
 * 
 */
public class SortablePolygon implements Comparable {
	public double minx;

	public List points;

	public SortablePolygon(List points) {
		this.points = points;
		if (points.size() > 0) {
			minx = ((Point2D) points.get(0)).getX();
			for (int k = 1; k < points.size(); k++) {
				double x = ((Point2D) points.get(k)).getX();
				if (x < minx)
					minx = x;
			}
		}
	}

	public int compareTo(Object arg0) {
		SortablePolygon pol2 = (SortablePolygon) arg0;
		double diff = (minx - pol2.minx);
		int result;
		if (diff < 0)
			result = -1;
		else if (diff > 0)
			result = 1;
		else
			result = 0;
		return result;
	}
}