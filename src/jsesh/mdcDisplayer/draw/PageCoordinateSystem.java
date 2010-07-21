package jsesh.mdcDisplayer.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;

/**
 * Represents the coordinate system of the original page in a graphic environment.
 * <p>Objects of this class can be used when one wants to convert back the coordinates 
 * of a given shape, currently expressed in a relative reference system, and get them 
 * in the original page system. 
 * <p> This is usefull when it comes to alignment and the like.
 * @author rosmord
 *
 */
public class PageCoordinateSystem {
	private AffineTransform originalTransform;
	private AffineTransform inverseOriginal;

	
	public PageCoordinateSystem(Graphics2D g) {
		setOriginalTransform(g.getTransform());
	}
	
	public PageCoordinateSystem(AffineTransform originalTransform) {
		setOriginalTransform(originalTransform);
	}

	public PageCoordinateSystem() {
		this(new AffineTransform());
	}

	/**
	 * Compute a transform from the current local coordinates to the global page
	 * coordinates. If we have a shape in a local (translated, reduced, etc...)
	 * system, we might want to compute it in the original page coordinates (for
	 * instance if we need to layout things on a fixed grid in page space).
	 * 
	 * @return
	 */
	public AffineTransform getTransformToPageCoordinates(Graphics2D g) {
		AffineTransform f = g.getTransform();
		f.preConcatenate(inverseOriginal);
		return f;
	}
	
	/**
	 * The transform applied to the graphical context to bring it to the desired
	 * text shape. This is used when scaling-independant elements need to be
	 * drawn.
	 * 
	 * @param originalTransform
	 */

	private void setOriginalTransform(AffineTransform originalTransform) {
		this.originalTransform = originalTransform;
		try {
			inverseOriginal = originalTransform.createInverse();
		} catch (NoninvertibleTransformException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Returns the transform applied <em>before starting the drawing</em>.
	 * Setting it as transform for a graphic context sends you back in the page reference system.
	 * @return
	 */
	public AffineTransform getOriginalTransform() {
		return originalTransform;
	}
	
	/**
	 * Transform an area so that its coordinates are expressed in terms of the original reference system.
	 * <p> we take a graphic context, and an area expressed in this context.
	 * <p> after the call, the area is expressed in the original reference system.
	 * @param currentG the current graphic context.
	 * @param a the area which will be transformed.
	 */
	public void moveAreaToPageReferenceSystem(Graphics2D currentG, Area a) {
		a.transform(getTransformToPageCoordinates(currentG));
	}
	
	/**
	 * Sets the current reference system of a graphic environment to the original page reference system.
	 * @param g
	 */
	public void moveBackToPageReferenceSystem(Graphics2D g) {
		g.setTransform(getOriginalTransform());
	}

	/**
	 * Build a new coordinate system, with the same scale as this one, but ignoring translations.
	 * This is useful if computing "tiles" which are supposed to be re-used.
	 * (although this is somehow questionnable).
	 * @return
	 */
	public PageCoordinateSystem createZeroTranslationCoordinateSystem() {
		double[] m = new double[6];
		originalTransform.getMatrix(m);
		m[4] = 0;
		m[5] = 0;
		AffineTransform zeroPointTransform = new AffineTransform(
				m);
		return new PageCoordinateSystem(zeroPointTransform);
	}
}
