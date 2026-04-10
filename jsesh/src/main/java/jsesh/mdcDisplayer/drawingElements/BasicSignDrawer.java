package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

import jsesh.hieroglyphs.signshape.LigatureZone;
import jsesh.mdcDisplayer.layout.ExplicitPosition;
import jsesh.mdcDisplayer.mdcView.ViewBox;

/**
 * Simple interface for basic sign drawing.
 * Mostly for internal use. If we go for modules, won't be exported.
 * 
 * Basically: knows the specific geometry of a sign.
 */
public interface BasicSignDrawer {

    /**
     * Draws the sign.
     * @param g2d
     * @param code
     * @param angle
     * @param currentView
     * @param bodySize
     */
	public void draw(Graphics2D g2d, String code, int angle, ViewBox currentView, HieroglyphBodySize bodySize);

    /**
     * Gets the sign bounding box.
     * @param code
     * @param angle
     * @param fixed
     * @return
     */
	Rectangle2D getBBox(String code, int angle, boolean fixed);

    /**
     * Gets the sign shape.
     * @param code
     * @return
     */
	Shape getShape(String code);

    /**
     * Gets the sign are (a filled shape).
     * 
     * Useful for hit testing.
     * @param code
     * @param x
     * @param y
     * @param xscale
     * @param yscale
     * @param angle
     * @param reversed
     * @return
     */
	Area getSignArea(String code, double x, double y, double xscale,
			double yscale, int angle, boolean reversed);

    /**
     * Is this code known by this drawer?
     * @param code
     * @return
     */
	boolean isKnown(String code);

    /**
     * Gets the various predefined positions for a ligature.
     * @param i the position index, which corresponds to "before" or "after" in the ligature.
     * @param code
     * @return
     */
    Optional<LigatureZone> getLigatureZone(int i, String code);

    /**
     * gets the positions of signs in a pre-recorded ligature (a legacy from tksesh).
     * Mostly obsolete.
     * @param codes
     * @return
     */
    List<ExplicitPosition> getPositions(List<String> codes);

}
