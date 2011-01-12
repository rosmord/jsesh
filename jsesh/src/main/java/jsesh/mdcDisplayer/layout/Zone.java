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
 * Zones are places where views will be added. 
 * In fact, they are used to keep track of a line or a column's size and content.
 *
 * Basically, a zone has a certain size, contains a number of views (with a position), 
 * and holds an "insertion" point where new views would be placed. The zone itself is rather stupid,
 * and moving the insertion point, for instance, is the responsability of the class' user. 
 * 
 * <p align="center">
 * 	<img src="../../../images/zone.png">
 * </p>
 * 
 * Zones have been much simplified comparatively with earlier versions.
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
    
    /**
     * Computes the size the zone would be if a given view was added to it.
     * Does not actually add the view.
     * @param view
     * @return the size the zone would be if it included the view.
     */
    public Rectangle2D computeNewSizeWith (MDCView view) {
    	Rectangle2D newArea= (Rectangle2D) zoneArea.clone();
    	// computes the view position
        double x= currentPoint.x + view.getDeltaBaseX();
        double y= currentPoint.y + view.getDeltaBaseY();
    	// Update the zone size
        newArea.add(new Rectangle2D.Double(x,
               y, view.getWidth(),
                view.getHeight()));
        return newArea;
    }
    
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
	 * Translates all views in a zone by a certain amount.
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
