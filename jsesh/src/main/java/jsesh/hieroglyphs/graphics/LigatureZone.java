package jsesh.hieroglyphs.graphics;

import java.awt.geom.Rectangle2D;

import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * A zone near hieroglyph which will be used for ligatures. The zone is defined
 * by a rectangular area (could be something else in the future), and a
 * "gravity" which states where the groups which will be ligatured in this zone
 * will stick. Default gravity is "center" for both directions.
 *
 * @author rosmord
 */
public class LigatureZone implements Cloneable {

    private Rectangle2D box;
    private VerticalGravity verticalGravity;
    private HorizontalGravity horizontalGravity;

    public LigatureZone(Rectangle2D box, VerticalGravity verticalGravity,
            HorizontalGravity horizontalGravity) {
        super();
        this.box = box;
        this.verticalGravity = verticalGravity;
        this.horizontalGravity = horizontalGravity;
    }

    public LigatureZone(double x, double y, double w, double h) {
        this(new Rectangle2D.Double(x, y, w, h));
    }

    public LigatureZone(Rectangle2D box) {
        super();
        this.box = box;
        this.verticalGravity = VerticalGravity.CENTER;
        this.horizontalGravity = HorizontalGravity.CENTER;
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public Object clone() {
        LigatureZone result;
        try {
            result = (LigatureZone) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        result.box = new Rectangle2D.Double(box.getMinX(), box.getMinY(), box
                .getWidth(), box.getHeight());
        return result;
    }

    /**
     * @return the horizontalGravity
     */
    public HorizontalGravity getHorizontalGravity() {
        return horizontalGravity;
    }

    /**
     * @param horizontalGravity the horizontalGravity to set
     */
    public void setHorizontalGravity(HorizontalGravity horizontalGravity) {
        this.horizontalGravity = horizontalGravity;
    }

    /**
     * @return the verticalGravity
     */
    public VerticalGravity getVerticalGravity() {
        return verticalGravity;
    }

    /**
     * @param verticalGravity the verticalGravity to set
     */
    public void setVerticalGravity(VerticalGravity verticalGravity) {
        this.verticalGravity = verticalGravity;
    }

    /**
     * @return the zone
     */
    public Rectangle2D getBox() {
        return box;
    }

    /**
     * @param zone the zone to set
     */
    public void setBox(Rectangle2D zone) {
        this.box = zone;
    }

    /**
     * @return @see java.awt.geom.RectangularShape#getHeight()
     */
    public double getHeight() {
        return box.getHeight();
    }

    /**
     * @return @see java.awt.geom.RectangularShape#getMaxX()
     */
    public double getMaxX() {
        return box.getMaxX();
    }

    /**
     * @return @see java.awt.geom.RectangularShape#getMaxY()
     */
    public double getMaxY() {
        return box.getMaxY();
    }

    /**
     * @return @see java.awt.geom.RectangularShape#getMinX()
     */
    public double getMinX() {
        return box.getMinX();
    }

    /**
     * @return @see java.awt.geom.RectangularShape#getMinY()
     */
    public double getMinY() {
        return box.getMinY();
    }

    /**
     * @return @see java.awt.geom.RectangularShape#getWidth()
     */
    public double getWidth() {
        return box.getWidth();
    }

    public void placeView(MDCView subView) {
        subView.scaleToFitDimensions(getWidth(), getHeight());
        double xpos = 0;
        double ypos = 0;

        if (HorizontalGravity.START.equals(getHorizontalGravity())) {
            xpos = getMinX();
        } else if (HorizontalGravity.END.equals(getHorizontalGravity())) {
            xpos = getMinX() + getWidth() - subView.getWidth();
        } else {
            xpos = getMinX() + (getWidth() - subView.getWidth()) / 2.0;
        }

        if (VerticalGravity.TOP.equals(getVerticalGravity())) {
            ypos = getMinY();
        } else if (VerticalGravity.BOTTOM.equals(getVerticalGravity())) {
            ypos = getMinY() + getHeight() - subView.getHeight();
        } else {
            ypos = getMinY() + (getHeight() - subView.getHeight()) / 2.0;
        }
        subView.getPosition().setLocation(xpos, ypos);
    }

    /**
     * Returns a scaled version of the ligature zone.
     *
     * @param scale scale of the sign.
     * @return a scaled version of the zone.
     */
    public LigatureZone scale(float scale) {
        LigatureZone scaledCopy = (LigatureZone) clone();
        scaledCopy.box = new Rectangle2D.Double(
                getMinX() * scale, getMinY() * scale, 
                getWidth() * scale, getHeight() * scale
        );
        return scaledCopy;
    }

}
