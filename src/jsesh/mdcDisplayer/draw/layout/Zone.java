/*
 * Created on 21 févr. 2005 by rosmord
 *
 * This file is distributed according to the GNU LESSER PUBLIC LICENCE
 */
package jsesh.mdcDisplayer.draw.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdcDisplayer.mdcView.RelativePosition;

/**
 * @author rosmord
 *
 * TODO DOCUMENT THIS CLASS
 */
public class Zone {

    /**
     * Current insertion position for views in zone.
     */
    private Point2D currentPointInZone;
    
    /**
     * Relative position of the zone in the document area.
     */
    private RelativePosition zoneStart;
    
    private Rectangle2D zoneArea;

    public Zone(RelativePosition zoneStart) {
        this.zoneStart= zoneStart;
        currentPointInZone= new Point2D.Double();
        zoneArea= new Rectangle2D.Double();
    }
    
    public void add(Rectangle2D box) {
        zoneArea.add(box);
    }
    
    public double getWidth() {
        return zoneArea.getWidth();
    }
    
    public double getHeight() {
        return zoneArea.getHeight();
    }

    /**
     * @return Returns the currentPointInZone.
     */
    public Point2D getCurrentPointInZone() {
        return currentPointInZone;
    }

    /**
     * @param zoneStart The zoneStart to set.
     */
    public void setZoneStart(RelativePosition zoneStart) {
        this.zoneStart = zoneStart;
    }

    /**
     * @return Returns the zoneStart.
     */
    public RelativePosition getZoneStart() {
        return zoneStart;
    }

    /**
     * @return
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
}
