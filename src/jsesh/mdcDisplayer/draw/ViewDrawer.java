package jsesh.mdcDisplayer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.RelativePosition;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * Knows how to draw a view and its subviews.
 * 
 * A viewdrawer delegates the actual drawing of its elements to an
 * ElementDrawer.
 * 
 * <p>
 * An important point is that in order to be able to use the VectorGraphics
 * library, the following convention must be kept : when the current
 * transformation for the graphic context must be modified, we shall do it this
 * way :
 * 
 * <pre>
 *               // save :
 *               	Graphics2D tmpg= (Graphics2D)g.create();
 *               ... work on tmpg, not g.
 *                //restoration simply means using &quot;g&quot; again. 
 *                //free the resource :
 *                tmpg.dispose();
 * </pre>
 * 
 * We used to save the current transform instead, but it doesn't work in
 * VectorGraphics. This is documented in the class :
 * org.freehep.graphics2d.VectorGraphics.
 * 
 * <p>
 * If it slows the system (which is not completely sure), a possible system
 * would be to use "Saver" class, which would implement a memento design pattern
 * for Graphics2D. The actual implementation would be one of these two.
 * 
 * This file is free Software (c) Serge Rosmorduc
 * 
 * @author rosmord
 * 
 */

public class ViewDrawer {

	/**
	 * A map of bitmaps, which serves as a cache for already drawn signs.
	 */
	private PictureCache cache;

	private boolean clip;

	/**
	 * A caret to draw, if wanted.
	 */
	private MDCCaret cursor = null;

	private boolean debug = false;

	private ElementDrawer elementDrawer;

	private DrawingSpecifications drawingSpecifications;

	/**
	 * Draws a view element.
	 * 
	 * <P>
	 * This recursive method does the actual drawing. Normally, we don't do any
	 * computation at that stage. All necessary information is stored in the
	 * ContainerView element. The ModelElement specific part of the code should
	 * in most cases do nothing, except for elements which actually have a
	 * drawing associated with them. For instance, the Cadrat element's specific
	 * code is empty, and the Cartouche's code does only draw the cartouche
	 * itself.
	 * 
	 * <p>
	 * As the upper level of painting is just a little different from the lower
	 * ones, we choosed to implement it here as well.
	 * 
	 * <p>
	 * a supler system should be provided to redraw only <em> part </em> of a
	 * view.
	 * 
	 * @param g
	 * @param v
	 * @param startPos
	 * @param endPos
	 * @param depth
	 */

	/**
	 * This rectangle is in fact a local variable of drawView. We have placed it
	 * here, because we want to avoid garbage collection of rectangles.
	 */
	transient Rectangle temporaryRectangle = new Rectangle();

	/**
	 * Temporary affine transformation for the following method (to avoid gc)
	 */
	private AffineTransform temporaryTransform = null;

	/**
	 * Create a drawer
	 */

	public ViewDrawer() {
		this(new SimpleElementDrawer());
	}

	public ViewDrawer(ElementDrawer e) {
		setClip(false);
		elementDrawer = e;
		drawingSpecifications = null;
		setCached(false);
	}

	/**
	 * Draws a view and its subviews.
	 * 
	 * @param g
	 * @param view
	 * @param ds
	 */

	public void draw(Graphics2D g, MDCView view, DrawingSpecifications ds) {
		DrawViewAndCursor(g, view, null, ds);
	}

	/**
	 * Draws a view and its subviews between two positions.
	 * <p>
	 * Note that positions fall <em>between</em> signs, so the first cadrat
	 * stands between positions 0 and 1.
	 * 
	 * @param g
	 * @param view
	 * @param ds
	 * @param start
	 *            First position
	 * @param end
	 *            last position
	 */

	public void draw(Graphics2D g, MDCView view, DrawingSpecifications ds,
			MDCPosition start, MDCPosition end) {
		DrawViewAndCursor(g, view, null, ds, start, end);
	}

	/**
	 * Draws colored rectangle showing the structure of a view for debugging
	 * purporses.
	 * 
	 * @param g
	 * @param v
	 */
	private void drawDebug(Graphics2D g, MDCView v) {

		if (v.getSubViews() != null && v.getSubViews().size() != 0) {
			if (v.getModel() instanceof Cadrat) {
				g.setColor(Color.GREEN);
				g.setStroke(new BasicStroke(0.4f));
				g.draw(new Line2D.Double(0, v.getHeight(), v.getWidth(), 0));
			} else if (v.getModel() instanceof TopItemList) {
				g.setStroke(new BasicStroke(1f));
				g.setColor(Color.MAGENTA);

			} else {

				g.setStroke(new BasicStroke(0.1f));
				g.setColor(Color.RED);
			}
			g.draw(new Rectangle2D.Double(0, 0, v.getWidth(), v.getHeight()));

		} else {
			g.setColor(Color.YELLOW);
			g.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, (float) 0.5));
			g.draw(new Line2D.Double(0, 0, v.getWidth(), v.getHeight()));
			g.draw(new Rectangle2D.Double(0, 0, v.getWidth(), v.getHeight()));

		}
	}

	/**
	 * Draws a view from the cache, if available.
	 * 
	 * @param g
	 * @param v
	 * @return true if the view was in cache.
	 */

	private boolean drawFromCache(Graphics2D g, MDCView v) {
		boolean result;
		BufferedImage img = (BufferedImage) cache.get(v.getModel());
		result = (img != null);
		if (result) {
			AffineTransform t = g.getTransform();
			if (temporaryTransform == null)
				temporaryTransform = new AffineTransform();
			temporaryTransform.setToTranslation(t.getTranslateX(), t
					.getTranslateY());
			// g.setTransform(AffineTransform.getTranslateInstance(t
			// .getTranslateX(), t.getTranslateY()));
			g.setTransform(temporaryTransform);
			g.drawImage(img, -1, -1, null);
			g.setTransform(t);
		}
		return result;
	}

	/**
	 * Highlight the view v if it belongs to the selection.
	 * 
	 * @param g
	 * @param i
	 *            the position in the current TopItemList
	 * @param x
	 *            x position of the view
	 * @param y
	 *            y position of the view
	 * @param v
	 *            the view
	 */
	private void drawSelection(Graphics2D g, int i, float x, float y, MDCView v) {
		if (cursor != null && cursor.hasMark()) {
			int a = Math.min(cursor.getInsert().getIndex(), cursor.getMark()
					.getIndex());
			int b = Math.max(cursor.getInsert().getIndex(), cursor.getMark()
					.getIndex());
			if (a <= i && i < b) {
				float w = v.getWidth();
				float h = v.getHeight();
				
				// h and w are OK, but the inter-cadrat space is not filled.
				// To effectively fill this space, we would need to keep data on the previous "View Rectangle".
				
				if (v.getNextViewPosition().getXAnchor() == RelativePosition.PREVIOUS
						&& v.getNextViewPosition().getYAnchor() == RelativePosition.PREVIOUS) {
					if (drawingSpecifications.getTextOrientation().isHorizontal()) {
						if (drawingSpecifications.getTextDirection().isLeftToRight())							
							w += drawingSpecifications.getSmallSkip();
						else {
							w += drawingSpecifications.getSmallSkip();
						}
					} else {
						h+= drawingSpecifications.getSmallSkip();						
					}
					// TODO : take into account vertical text ???
					// AND right-to-left text also. Basically, use
					// the values of dx and dy.
					// h= h+ v.getNextViewPosition().getDy();
				}

				g.setColor(new Color(0, 0, 255, 50));
				g.fill(new Rectangle2D.Double(x, y, w, h));
			}
		}
	}

	/**
	 * actual drawing of a whole view.
	 * 
	 * @param g
	 * @param v
	 * @param depth
	 */
	private boolean drawView(Graphics2D g, MDCView v, int depth) {
		return drawView(g, v, 0, v.getNumberOfSubviews(), depth);
	}

	private boolean drawView(Graphics2D g, MDCView v, int startPos, int endPos,
			int depth) {

		Graphics2D currentG = g;
		// For top level items, with bitmap output, we may use caching.
		// with caching, the picture of a view is drawn into a bitmap,
		// which is then copied to g.
		// this doesn't speed up the initial drawing, but it gives a
		// definite boost for small modifications, given that the actual drawing
		// of splines and the like is much more time consuming in practice than
		// anything else.

		BufferedImage img = null;
		Color oldBackground = g.getBackground();

		// TODO : move this code up, in a drawing loop for TopItemList ?
		// (this would be better anyway, for the test would be simpler,
		// without need for "depth")
		// Code for TopItemLists elements.
		if (depth == 1 && v.getModel() instanceof TopItem) {

			// The clipping functions allow us not to draw the text which is not
			// visible.
			if (clip) {
				temporaryRectangle = g.getClipBounds(temporaryRectangle);
				// If the view stands before the visible area, do nothing.
				// If the view stands after the visible area, do nothing.

				if (temporaryRectangle != null
						&& (v.getHeight() < temporaryRectangle.getMinY() || 0 > temporaryRectangle
								.getMaxY()) || 0 > temporaryRectangle.getMaxX()
						|| v.getWidth() < temporaryRectangle.getMinX())
					return false;
			}

			// draw from the cache, if available.
			if (isCached() && drawFromCache(g, v)) {
				// Conditional cursor drawing : if the current position
				// corresponds to the cursor, draw it:
				testAndDrawCursor(g, v);
				return true;
			}

			// If we are caching, fetch a bitmap to draw to.
			if (isCached()) {
				Point2D o = new Point2D.Float();
				Point2D p = new Point2D.Float();
				currentG.getTransform().transform(new Point2D.Float(0, 0), o);
				currentG.getTransform().transform(
						new Point2D.Float(v.getWidth(), v.getHeight()), p);
				int x = (int) Math.ceil(p.getX() - o.getX());
				int y = (int) Math.ceil(p.getY() - o.getY());
				if (x > 0 && y > 0) {
					// Create the image
					img = cache.createImage(x + 2, y + 2);
					// If got one, we will draw in it.
					// if we couldn't get one, we will draw into the old graphic
					// context.
					if (img != null) {
						currentG = img.createGraphics();
						currentG.setRenderingHints(g.getRenderingHints());
						// currentG.setBackground(g.getBackground());
						currentG.setBackground(new Color(255, 255, 255, 0));
						currentG.clearRect(0, 0, img.getWidth(), img
								.getHeight());
						currentG.translate(1, 1);
						currentG.scale(g.getTransform().getScaleX(), g
								.getTransform().getScaleY());
					}
				}
			}
			TopItem topItem = (TopItem) v.getModel();
			elementDrawer.setDrawingState(topItem.getState());
			// For all Top Items, and only for them :
			if (topItem.getState().isShaded()) {
				g.setBackground(drawingSpecifications.getGrayColor());
				shadeView(currentG, v);
			}
		}

		if (elementDrawer.getDrawingState().isRed())
			currentG.setColor(drawingSpecifications.getRedColor());
		else
			currentG.setColor(drawingSpecifications.getBlackColor());

		// Temporary graphic state :
		Graphics2D tmpG = (Graphics2D) currentG.create();

		// Move, scale, etc... according to the current view
		tmpG.transform(v.getAffineTransform());

		// Part of the element drawn before the element's subviews
		// This is mainly used for shading : shading can be done before or after
		// the sign has been drawn.
		elementDrawer.drawElement(v, tmpG, false);

		// If this element contains sub-views
		// we draw them
		if (v.getNumberOfSubviews() != 0) {

			// The current reference point :

			Point2D.Float ref = new Point2D.Float(0, 0);

			int end = (endPos < v.getNumberOfSubviews() ? endPos : v
					.getNumberOfSubviews());

			for (int i = startPos; i < endPos && i < end; i++) {
				MDCView subv = v.getSubView(i);
				moveToReferenceNextPoint(ref, v, i);

				// Graphics2D subViewG= (Graphics2D) tmpG.create();
				Graphics2D subViewG = (Graphics2D) tmpG;

				// Final translation takes the view's dx,dy into account :
				subViewG.translate(ref.x + subv.getDx(), ref.y + subv.getDy());
				boolean wasDrawn = drawView(subViewG, subv, depth + 1);
				subViewG
						.translate(-ref.x - subv.getDx(), -ref.y - subv.getDy());
				// subViewG.setTransform(baseTransform);
				// subViewG.dispose();
				// When drawing the TopItemList, outline the current selection :
				if (wasDrawn && v.getModel() instanceof TopItemList) {
					drawSelection(tmpG, i, ref.x, ref.y, subv);
				}
				// Position of the next element :
			}
		}
		// Delegates actual element drawing to the ElementDrawer
		elementDrawer.drawElement(v, tmpG, true);
		// restore old coordinate system.
		tmpG.dispose();
		// g.setTransform(oldTransform);

		if (img != null) {
			cache.put(v.getModel(), img);
			drawFromCache(g, v);
		}

		testAndDrawCursor(g, v);

		if (img != null) {
			currentG.dispose();
		}

		if (g.getBackground() != oldBackground)
			g.setBackground(oldBackground);
		// Temporary : draw a red rectangle around the view
		if (debug)
			drawDebug(g, v);
		return true;
	}

	/**
	 * Draws a view as DrawView(), but also draws a cursor at the right
	 * position.
	 * 
	 * @param g2d
	 * @param view
	 * @param cursor
	 * @param ds
	 */
	public void DrawViewAndCursor(Graphics2D g2d, MDCView view,
			MDCCaret cursor, DrawingSpecifications ds) {
		drawingSpecifications = ds;
		elementDrawer.prepareDrawing(drawingSpecifications);
		this.cursor = cursor;
		drawView(g2d, view, 0);
		this.cursor = null; // To avoid short-term memory leak.
		elementDrawer.cleanup();
	}

	/**
	 * Draws a view, its subviews, and the cursor between two positions.
	 * <p>
	 * Note that positions fall <em>between</em> signs, so the first cadrat
	 * stands between positions 0 and 1.
	 * 
	 * @param g2d
	 * @param view
	 * @param cursor :
	 *            the cursor position. If it's null, the cursor is not drawn.
	 * @param ds
	 * @param start
	 * @param end
	 */
	public void DrawViewAndCursor(Graphics2D g2d, MDCView view,
			MDCCaret cursor, DrawingSpecifications ds, MDCPosition start,
			MDCPosition end) {
		drawingSpecifications = ds;
		elementDrawer.prepareDrawing(drawingSpecifications);
		this.cursor = cursor;
		drawView(g2d, view, 0, start.getIndex(), end.getIndex());
		this.cursor = null; // To avoid short-term memory leak.
		elementDrawer.cleanup();
	}

	/**
	 * flushes the cache if there is one.
	 * <p>
	 * should be called if the case contents becomes obsolete, e.g. if the
	 * drawing scale is changed.
	 */
	public void flushCache() {
		if (isCached())
			cache.reset();
	}

	/**
	 * Returns the display coordinates of a given text position.
	 * 
	 * @param v
	 * @param position
	 * @return the display coordinates of a given text position.
	 */
	public Point2D getPointForPosition(MDCView v, MDCPosition position) {
		int i;
		Point2D.Float ref = new Point2D.Float(0, 0);
		for (i = 0; i < position.getIndex(); i++) {
			moveToReferenceNextPoint(ref, v, i);
		}
		// For the last index, we should take into account only the part that
		// corresponds to
		// getNextViewPosition()
		if (i > 1) {
			RelativePosition relpos = v.getSubView(i - 1).getNextViewPosition();
			// MDCView dummyView= new MDCView(null);
			ref.x = relpos.computeXTranslation(v, ref.x);
			ref.y = relpos.computeYTranslation(v, ref.y);
		}
		return ref;
	}

	/**
	 * Mapping from display space to model space, mostly to manage mouse clicks.
	 * 
	 * @param v
	 *            a view for a <em>TopItemList</em> (this might and should
	 *            change ?)
	 * @param clickPoint :
	 *            a point, its coordinates expressed in display space. Note that
	 *            in the case of JMDCDisplayer, display space scaling should be
	 *            applied before calling this function, as the drawer knows
	 *            nothing of it.
	 * @param ds
	 *            DrawingSpecifications
	 * @return the position corresponding to the point, or null if the click
	 *         corresponded to nothing..
	 */

	public MDCPosition getPositionForPoint(MDCView v, Point clickPoint,
			DrawingSpecifications ds) {
		int pos = -1;
		// The main idea is that we visit all positions until
		// we see we are too far.
		// For line orientation, this means either :
		// a) the vertical position of the current view fits clicPoint,
		// but the horizontal position is either too large or too small
		// (depending on the text orientation).
		// or
		// b) the vertical position is too large.
		// In this cases, the position will be the previous position.
		//
		// For text in column, the system is the same, only reversed.

		if (v.getNumberOfSubviews() != 0) {
			Point2D.Float point = new Point2D.Float();
			// First, compute the point's position in terms of v reference
			// system :

			try {
				v.getAffineTransform().inverseTransform(
						new Point2D.Float(clickPoint.x, clickPoint.y), point);
			} catch (NoninvertibleTransformException e) {
				// Should not happen !
				e.printStackTrace();
			}

			// The current reference point :
			Point2D.Float ref = new Point2D.Float(0, 0);

			int i = 0;
			while (pos == -1 && i < v.getNumberOfSubviews()) {

				MDCView subv = v.getSubView(i);

				moveToReferenceNextPoint(ref, v, i);
				// Test if point falls into the current subview. If it's the
				// case,
				// sets pos :

				float x, y;
				x = ref.x + subv.getDx();
				y = ref.y + subv.getDy();

				// First test, the same in all cases : does the point fall
				// inside a view ?
				if (point.x >= x && point.y >= y
						&& point.x < x + subv.getWidth() + ds.getSmallSkip()
						&& point.y < y + subv.getHeight()+ ds.getSmallSkip()) {
					// TODO : the exact position choosed depends on the text
					// orientation.
					if (ds.getTextOrientation().isHorizontal()) {
						if (point.x < x + subv.getWidth() / 2.0f)
							if (ds.getTextDirection().isLeftToRight())
								pos = i;
							else
								pos = i + 1;
						else if (ds.getTextDirection().isLeftToRight())
							pos = i + 1;
						else
							pos = i;
					} else { // Columns
						if (point.y < y + subv.getHeight() / 2.0f)
							pos = i;
						else
							pos = i + 1;

					}
				} else if (ds.getTextOrientation().isHorizontal()) {
					// Text for end of line.
					// if (ds.getTextDirection().isLeftToRight()) {
					if (y > point.y)
						pos = (i > 0 ? i - 1 : 0);
					// }
				} else {
					if (ds.getTextDirection().isLeftToRight()) {
						if (x > point.x)
							pos = i;
					} else {
						if (x + ds.getMaxCadratWidth() < point.x)
							pos = i;
					}
				}
				i++;
			}
		}

		// If nothing was found, chose the last cadrat.

		if (pos == -1)
			return new MDCPosition((TopItemList) v.getModel(), v.getModel()
					.getNumberOfChildren());
		else
			return new MDCPosition((TopItemList) v.getModel(), pos);
	}

	/**
	 * Returns A rectangle that contains parts of the signs which surround
	 * position.
	 * <p>
	 * it deals with the cases where the first of these signs is a break (line
	 * or page).
	 * 
	 * @param v
	 * @param position
	 * @param specs
	 * @return A rectangle that contains parts of the signs which surround
	 *         position. TODO : improve this method.
	 */
	public Rectangle2D getRectangleAroundPosition(MDCView v,
			MDCPosition position, DrawingSpecifications specs) {
		int i;
		// Move to position i-1, computing the coordinates.
		Point2D.Float ref = new Point2D.Float(0, 0);
		for (i = 0; i < position.getIndex() && i < v.getNumberOfSubviews(); i++) {
			moveToReferenceNextPoint(ref, v, i);
		}
		// position is the next index. The rectangle should include both
		// views i-1 and i (if they exist).
		Rectangle2D result = null;

		if (i - 1 > 0) {
			MDCView subv = v.getSubView(i - 1);
			// Artificially place the view to the next drawing position.
			double x = subv.getNextViewPosition().computeXTranslation(v,
					(float) ref.getX());
			double y = subv.getNextViewPosition().computeYTranslation(v,
					(float) ref.getY());
			double w = 2;
			double h = specs.getMaxCadratHeight();
			result = new Rectangle2D.Double(x, y, w, h);
		}

		if (i < v.getNumberOfSubviews()) {
			moveToReferenceNextPoint(ref, v, i);
			Rectangle2D r2;
			r2 = new Rectangle2D.Double(ref.getX(), ref.getY(), 1, specs
					.getMaxCadratHeight());

			if (result == null) {
				result = r2;
			} else {
				result.add(r2);
			}
		}

		if (result == null)
			result = new Rectangle2D.Double();
		// System.out.println(result);
		return result;
	}

	public boolean isCached() {
		return (cache != null);
	}

	/**
	 * @return true if clipping is enabled.
	 */
	public boolean isClip() {
		return clip;
	}

	/**
	 * @return true if debug mode.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @return true if we are in paging mode.
	 */
	public boolean isPaged() {
		return elementDrawer.isPaged();
	}

	public boolean isShadeAfter() {
		return elementDrawer.isShadeAfter();
	}

	/**
	 * Moves the reference point ref to the start position for subview number i
	 * of v.
	 * <p>
	 * Doesn't take the subview's dx,dy into account, as they have no influence
	 * on the current position.
	 * 
	 * <p>
	 * Typically, we can use it like this :
	 * 
	 * <pre>
	 * Point2D.Float ref = new Point2D.Float(0, 0);
	 * for (int i = 0; i &lt; v.getNumberOfSubViews(); i++) {
	 * 	moveToReferenceNextPoint(ref, v, i);
	 * }
	 * </pre>
	 * 
	 * @param ref
	 *            the point to be moved. Should correspond to the position of
	 *            the previous subview.
	 * @param v
	 * @param i
	 */
	private void moveToReferenceNextPoint(Point2D.Float ref, MDCView v, int i) {
		RelativePosition previous; // position set by the previous view
		if (i == 0) {
			previous = v.getFirstSubViewPosition();
		} else {
			previous = v.getSubView(i - 1).getNextViewPosition();
		}
		MDCView subv = v.getSubView(i);
		// First, move the references according to the specifications
		// in the previous subview.
		ref.x = previous.computeXTranslation(v, ref.x);
		ref.y = previous.computeYTranslation(v, ref.y);

		// Now, move the references according to our own specifications.

		ref.x = subv.getStartPoint().computeXTranslation(v, ref.x);
		ref.y = subv.getStartPoint().computeYTranslation(v, ref.y);
	}

	/*
	 * Empties the cache. Should be called, in particular, when the scale is
	 * changed.
	 * 
	 * void resetCache() { setCached(isCached()); System.gc(); }
	 */

	/**
	 * Request or prevent image caching. Image caching is only possible for
	 * raster output. In practice, its only use is for interactive displays of
	 * hieroglyphs.
	 * 
	 * @param c
	 */
	public void setCached(boolean c) {
		if (c)
			cache = new PictureCache(1000);
		else
			cache = null;
	}

	/**
	 * Ask for clipping to be used to speed the drawing.
	 * 
	 * @param clip
	 */

	public void setClip(boolean clip) {
		this.clip = clip;
	}

	/**
	 * @param b
	 */
	public void setDebug(boolean b) {
		debug = b;
	}

	/**
	 * @param paged
	 */
	public void setPaged(boolean paged) {
		elementDrawer.setPaged(paged);
	}

	/**
	 * If ShadeAfter is true, the shading should be drawn <em>after</em> the
	 * shaded elements. For systems which support transparency, shadeAfter at
	 * <code>true</code> might be a good choice. Older systems (in the present
	 * case, wmf), should use shadeAfter= false.
	 * 
	 * @param shadeAfter
	 */
	public void setShadeAfter(boolean shadeAfter) {
		elementDrawer.setShadeAfter(shadeAfter);
	}

	/**
	 * Draws a gray rectangle over a view.
	 * 
	 * @param g
	 * @param v
	 */
	private void shadeView(Graphics2D g, MDCView v) {

		Paint oldPaint = g.getPaint();
		g.setPaint(drawingSpecifications.getGrayColor());

		// Note that we used to take the view's dx and dy into account, but this
		// is done
		// even before drawing the view.
		g.fill(new Rectangle2D.Float(0, 0, v.getWidth(), v.getHeight()));

		// Fill the gap between this view and the next one, if possible and
		// necessary.

		// We fill gaps between two adjacent cadrats.
		if (v.getNextViewPosition().getXAnchor() == RelativePosition.PREVIOUS) {
			Rectangle2D.Float rect = new Rectangle2D.Float();
			// Shade the inter-cadrat space
			rect.x = v.getWidth();
			rect.y = 0f;
			rect.height = v.getHeight();
			rect.width = (v.getNextViewPosition().getDx() - v.getWidth());
			g.fill(rect);
		}

		g.setPaint(oldPaint);

	}

	/**
	 * Conditional cursor drawing : if the current position corresponds to the
	 * cursor, draw it. When the cursor is at the last position, the first test
	 * never works. (we might want to use some sentinel to correct this). so we
	 * have a special case. For empty text, we will write some specific code (at
	 * upper levels ?)
	 * 
	 * @param g
	 * @param v
	 */

	private void testAndDrawCursor(Graphics2D g, MDCView v) {
		// Will perhaps be passed as arguments or taken from fields like
		// currentTextDirection.
		TextDirection textDirection = drawingSpecifications.getTextDirection();
		TextOrientation textOrientation = drawingSpecifications
				.getTextOrientation();

		if (cursor != null && cursor.getInsert() != null) {
			if (v.getModel() == cursor.getInsert().getPosition()
					.getElementAfter()) {
				g.setColor(drawingSpecifications.getCursorColor());
				g.setStroke(drawingSpecifications.getWideStroke());
				// IMPORTANT : get sure the cursor is correctly drawn, even if
				// startPoint is absolute.
				/*
				 * g2d.draw( new Line2D.Double( v.getStartPoint().getDx(),
				 * v.getStartPoint().getDy(), v.getStartPoint().getDx(),
				 * drawingSpecifications.getMaxCadratHeight()));
				 */

				if (textOrientation.isHorizontal()) {
					if (textDirection.isLeftToRight())
						g.draw(new Double(0, -v.getDy(), 0,
								drawingSpecifications.getMaxCadratHeight()));
					else
						g.draw(new Double(v.getWidth(), -v.getDy(), v
								.getWidth(), drawingSpecifications
								.getMaxCadratHeight()));
				} else { // columns

					g.draw(new Double(0, v.getDy(), drawingSpecifications
							.getMaxCadratWidth(), v.getDy()));

				}
			} else if (v.getModel() == cursor.getInsert().getPosition()
					.getElementBefore()
					&& v.getParent() != null
					&& v == v.getParent().getLastSubView()) {
				g.setColor(drawingSpecifications.getCursorColor());
				g.setStroke(drawingSpecifications.getWideStroke());
				if (textOrientation.isHorizontal()) {
					if (textDirection.isLeftToRight())
						g.draw(new Double(v.getWidth(), 0, v.getWidth(),
								drawingSpecifications.getMaxCadratHeight()));
					else
						g.draw(new Double(0, 0, 0, drawingSpecifications
								.getMaxCadratHeight()));
				} else {
					g.draw(new Double(0, v.getHeight() + v.getDy(),
							drawingSpecifications.getMaxCadratWidth(), v
									.getHeight()
									+ v.getDy()));
				}
			}
		}
	}
}