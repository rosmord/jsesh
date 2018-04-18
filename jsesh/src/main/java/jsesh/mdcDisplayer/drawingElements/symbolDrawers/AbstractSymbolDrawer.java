package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import jsesh.mdcDisplayer.drawingElements.ViewBox;

public abstract class AbstractSymbolDrawer implements SymbolDrawerDelegate {

	/**
	 * The size of the pencil stroke.
	 */
	protected float strokeWidth;

	/**
	 * Base class for symbol drawer, with optionnal initial translation.
	 */
	public AbstractSymbolDrawer() {
		super();
	}

	protected abstract CombinedPath buildShapeForDrawing(float width,
			float height);

	public final void draw(Graphics2D g2d, int angle, ViewBox viewBox,
			float strokeWidth) {
		// The problem with strokes : if we want a reasonable rendering,
		// strokes should be somehow scale independant (well, not that
		// completely).
		this.strokeWidth = strokeWidth;
		if (angle == 0) {
			CombinedPath combinedPath= buildShapeForDrawing(viewBox.getWidth(),
					viewBox.getHeight() );
			GeneralPath pol = combinedPath.getActualPath();
			g2d.fill(g2d.getStroke().createStrokedShape(pol));
		} else {
	
			// Now, the problem is that we need to compute the correct
			// original size for our drawing...
			// We will start with the "normal" size and find the possible
			// scale...
			// 
			// It would be possible to compute the original size from the
			// viewBox,
			// except for 45° angles.
			//
			// Normally, the new Width and new Height for the Bounding box
			// of a rotated rectangle are:
			// NW= w .|cos alpha| + h . |sin alpha|
			// NH= w .|sin alpha| + h . |cos alpha|
			// Now, solving the system gives...
			// w= (nW .|cos alpha| -nH . |sin alpha|) / (2 cos^2 alpha -1)
			// 
			// The system can't be solved if alpha= 45°.
			//
			CombinedPath combinedPath= buildShapeForDrawing(getBaseWidth()
					* viewBox.getXScale(),
					SpecialSymbolDrawer.EDITOR_MARKUP_HEIGHT
							* viewBox.getYScale());
			
			GeneralPath pol = combinedPath.getActualPath();
			
			// Add a transform operation to pol, it will be more logical here...
			pol.transform(AffineTransform.getRotateInstance((float) (angle
					* Math.PI / 180)));
			
			GeneralPath boundingPath= combinedPath.getBoundingPath();
			if (! combinedPath.actualPathUsed) {
				boundingPath.transform(AffineTransform.getRotateInstance((float) (angle
						* Math.PI / 180)));
			}
			// Move it to 0,0 :
			// Bounds should be computed on the basis of a special "bounding box" path,
			// to account for instance for the white space on [?
			Rectangle2D bounds = boundingPath.getBounds2D();
			float halfStroke= strokeWidth/2f;
			pol.transform(AffineTransform.getTranslateInstance(-bounds
					.getMinX()+ halfStroke, -bounds.getMinY()+ halfStroke));

			g2d.draw(pol);
		}
	}

	public float getBaseWidth() {
		return 3;
	}
	
	/**
	 * Build a rectangular path for easy creation of bounding path
	 * @param width
	 * @param height
	 * @return
	 */
	protected static GeneralPath buildRectangle(float width, float height) {
		GeneralPath boundingPath= new GeneralPath();
		boundingPath.moveTo(0, 0);
		boundingPath.lineTo(width, 0);
		boundingPath.lineTo(width, height);
		boundingPath.lineTo(0, height);
		boundingPath.lineTo(0, 0);
		return boundingPath;
	}

}