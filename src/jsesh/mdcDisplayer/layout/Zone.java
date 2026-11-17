/*
 * Created on 21 f�vr. 2005 by rosmord
 *
 * This file is distributed according to the GNU LESSER PUBLIC LICENCE
 */
package jsesh.mdcDisplayer.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * <b>RE-WRITE this text</b>.
 * A simple text area (line, column...).
 * Zones are places where views will be added. In fact, they are used to keep track of a line or a column's size.
 * 
 * A limitation of zones is that when absolute positioning is used in a zone, there are constraints :
 * there should not be a point relative to the west side and a point relative to the east, nor a point relative to the north and another to the south.
 * However, it's possible to have a point relative to the north and another to the east.
 * 
 * <p> Zones work as "macro"-MDCviews. That is, a zone has a start point (which will be used to place it), and
 * a last point, which is always relative to the start point. Note that the "Next point" is always a simple relative translation. 
 * 
 * <p> NOTE ON NEXT LINES : the idea of end point is nice, but it doesn't work well when a relative position is "rooted"  
 * and a last point.
 * 
 * Normally, the starting point is modified by the user when the zone is completed. 
 * The last point encodes a position relative to the start point. It's normally a simple translation.
 * It allows to compute where we will be after the zone is finished. 
 * <p align="center">
 * 	<img src="../../../images/zone.png">
 * </p>
 * @author rosmord
 */
public class Zone {

    /**
     * Current insertion position for views in zone.
     */
    private Point2D.Double currentPoint;
    
    private Rectangle2D zoneArea;
    
    private ArrayList views;

    /**
     * build a new zone. 
     * @param minWidth
     * @param minHeight
     */
    public Zone(double minWidth, double minHeight) {        
        currentPoint= new Point2D.Double();
        zoneArea= new Rectangle2D.Double(0f,0f,minWidth, minHeight);
        views= new ArrayList();
    }
    
    /**
     * Add a view's bounding box to the current zone.
     * All the parameters for this view should be set. In particular, <em>both</em> the startPoint and the nextViewPosition 
     * should have their values. These values will be relative to the zone's first point, and all views in the zone will be translated when the zone is finally added 
     * to the document.
     * @param view : the view to add.
     */
    public void add(MDCView view) {
    	// Sets the view position
    	view.getPosition().setLocation(currentPoint.x + view.getDeltaBaseX(), currentPoint.y + view.getDeltaBaseY());
        // Update the zone size
        zoneArea.add(new Rectangle2D.Double(view.getPosition().x,
               view.getPosition().y, view.getWidth(),
                view.getHeight()));
        views.add(view);
    }
    
    /*public void add(Rectangle2D box) {
        zoneArea.add(box);
    }*/
    
    public double getWidth() {
        return zoneArea.getWidth();
    }
    
    public double getHeight() {
        return zoneArea.getHeight();
    }

    /**
     * @return true if zone is empty.
     */
    public boolean isEmpty() {
        return this.zoneArea.isEmpty();
    }
    
    public double getMinY() {
        return zoneArea.getMinY();
    }
    
    public double getMinX() {
        return zoneArea.getMinX();
    }
    
    public double getMaxX() {
    	return zoneArea.getMaxX();
    }

	/**
	 * @return
	 */
	public Point2D getCurrentPoint() {
		return currentPoint;
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void moveCurrentPoint(double dx, double dy) {
		currentPoint.setLocation(currentPoint.x+dx, currentPoint.y+dy);
	}

	/**
	 * @param zoneStart
	 */
	public void translateBy(Point2D zoneStart) {
		for (int i=0; i< views.size(); i++) {
			MDCView v= (MDCView) views.get(i);
			v.getPosition().x+= zoneStart.getX();
			v.getPosition().y+= zoneStart.getY();
		}
	}
 
}
