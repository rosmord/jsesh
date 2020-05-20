package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import jsesh.hieroglyphs.graphics.LigatureZone;

/**
 * This class is responsible for everything which concerns the graphical
 * appearance of symbols (hieroglyphs, editorial markup...)
 *
 * It doesn't deal with scaling and the like, as scaling is provided by the
 * graphics environment.
 *
 * <p>
 * This interface should probably not contain methods like getHeightOfA1 or
 * isKnown, which is really very specific to hieroglyphs.
 * <p>
 * The reason is historical: this interface was in the beginning only devoted to
 * glyphs. But now, the environment for hieroglyphs has been extended (to
 * philological markup and even text recently).
 * <p>
 * It doesn't deal with unkown or undefined codes.
 *
 * This file is free Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 *
 *
 * (c) Serge Rosmorduc
 *
 * @author Serge Rosmorduc
 *
 */
public interface HieroglyphsDrawer {

    /**
     * draws a hieroglyph, described by its code, on the drawing surface g. The
     * view contains information about the scale, etc...
     *
     * @param g : where we are going to draw
     * @param code the hieroglyph code in Manuel de Codage
     * @param angle angle applied to the glyph, in degrees.
     *
     * @param box gives information about the desired geometry for the symbol.
     *
     */
    void draw(Graphics2D g, String code, int angle, ViewBox box);

    /**
     * Gets a sign bounding box, or null if ! isKnown(code). The bounding box is
     * the "natural" box for this sign. In some cases, the sign will be larger.
     *
     * <p>
     * Note about "fixed" : a few signs (currently, mainly editor markup) may
     * have two size: an elastic one (which is very small, but can grow), and a
     * "natural" one, which is fixed size, and used when the sign is more or
     * less alone. Most signs won't care about the value of fixed.
     *
     * @param code
     * @param angle the rotation angle applied to this sign (in degree,
     * clockward).
     * @param fixed Should the box be prepared for a fixed size sign.
     * @return sign bounding box, or null if ! isKnown(code).
     */
    Rectangle2D getBBox(String code, int angle, boolean fixed);

    /**
     * A try to test replacing bounding boxes by actual shapes. Should only be
     * called if needed (rotations, complex sign placement). The shape needs not
     * to be the real sign shape. It can be some sort of "bounding shape".
     *
     * @param code : the sign code.
     * @return a bounding shape, or null if ! isKnown(code)
     */
    Shape getShape(String code);

    /**
     * Get the sign area. The area of a sign is the region enclosed in its outer
     * boundaries.
     *
     * @param code
     *
     * @param x
     * @param y
     * @param xscale
     * @param yscale
     * @param angle
     * @param reversed
     * @return the global area occupied by this sign.
     */
    Area getSignArea(String code, double x, double y, double xscale, double yscale,
            int angle, boolean reversed);

    /**
     * Does this code correspond to a known sign ?
     *
     * @param code
     * @return true if the sign is known.
     */
    boolean isKnown(String code);

    /**
     * Return areas fit for inserting ligatured groups in this sign.
     * <b>The area coordinates are set for an unscaled sign.</b>
     * <p>
     * Typical case : G1 sign, with two areas, one in the bottom-start corner of
     * the sign, the other in the top-end corner. We might actually propose an
     * half automated system.
     *
     * @param i the number of the area : 0 or 1 or 2
     * @param code the code of the sign.
     * @return an optional rectangle if an area is available.
     */
    Optional<LigatureZone> getLigatureZone(int i, String code);

    /**
     * Returns the height of A1 in this font set.
     *
     * @return
     */
    double getHeightOfA1();

    /**
     * Select the use (if possible) of a bolder, small body font.
     *
     * @param smallBody
     */
    void setSmallBodyUsed(boolean smallBody);

    /**
     * Are we requesting the use of a "small body" font.
     * @return 
     */
    boolean isSmallBodyUsed();

    /**
     * Returns the length of 1 "group" unit. Groups units are relative to the
     * size of the fonts. More precisely, they are 1/1000 of the height of the
     * A1 sign.
     *
     * @return
     */
    double getGroupUnitLength();
}
