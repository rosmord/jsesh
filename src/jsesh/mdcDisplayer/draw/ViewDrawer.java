package jsesh.mdcDisplayer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;

import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.RelativePosition;

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
 *         // save :
 *         	Graphics2D tmpg= (Graphics2D)g.create();
 *         ... work on tmpg, not g.
 *          //restoration simply means using &quot;g&quot; again. 
 *          //free the resource :
 *          tmpg.dispose();
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
	private boolean clip;

	private boolean debug = false;

	private ElementDrawer elementDrawer;

	/**
	 * A map of bitmaps, which serves as a cache for already drawn signs.
	 */
	private PictureCache cache;

	/**
	 * A caret to draw, if wanted.
	 */
	private MDCCaret cursor = null;

	/**
	 * Create a drawer
	 */

	public ViewDrawer() {
		this(new SimpleElementDrawer());
	}

	public ViewDrawer(ElementDrawer e) {
		elementDrawer = e;
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
	 * Draws a view as DrawView(), but also draws a cursor at the right
	 * position.
	 * 
	 * @param g2d
	 * @param v
	 * @param cursor
	 */
	public void DrawViewAndCursor(Graphics2D g2d, MDCView view,
			MDCCaret cursor, DrawingSpecifications ds) {
		elementDrawer.setDrawingSpecification(ds);
		elementDrawer.reset();
		this.cursor = cursor;
		drawView(g2d, view, 0);
		this.cursor= null; // To avoid short-term memory leak.
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
		elementDrawer.setDrawingSpecification(ds);
		elementDrawer.reset();
		this.cursor = cursor;
		drawView(g2d, view, 0, start.getIndex(), end.getIndex());
	}

	/**
	 * Draws a view element.
	 * 
	 * <P>This recursive method does the actual drawing. Normally, we don't do any
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
	 * @param v
	 */

	private void drawView(Graphics2D g, MDCView v, int startPos, int endPos,
			int depth) {
		double yMin, yMax;

		Graphics2D currentG = g;
		// For top level items, with bitmap output, we may use caching.
		// with caching, the picture of a view is drawn into a bitmap,
		// which is then copied to g.
		// this doesn't speed up the initial drawing, but it gives a
		// definite boost for small modifications, given that the actual drawing
		// of splines and the like is much more time consuming in practice than
		// anything else.

		BufferedImage img = null;
		Color oldBackground= g.getBackground(); 

		// The clipping functions allow us not to draw the text which is not
		// visible.
		if (clip) {
			Rectangle2D r = (Rectangle2D) g.getClipBounds();
			// If the view stands before the visible area, do nothing.
			// If the view stands after the visible area, do nothing.

			if (r != null && (v.getHeight() < r.getY() || 0 > r.getMaxY()))
				return;
		}

		// TODO : move this code up, in a drawing loop for TopItemList ?
		// (this would be better anyway, for the test would be simpler,
		// without need for "depth")
		// Code for TopItemLists elements.
		if (depth == 1 && v.getModel() instanceof TopItem) {
			// draw from the cache, if available.
			if (isCached() && drawFromCache(g, v)) {
				// Conditional cursor drawing : if the current position
				// corresponds to the cursor, draw it:
				testAndDrawCursor(g, v);
				return;
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
						//currentG.setBackground(g.getBackground());
						currentG.setBackground(new Color(255,255,255,0));
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
			if (topItem.getState().isShaded())
			{
				g.setBackground(elementDrawer.getDrawingSpecifications().getGrayColor());
				shadeView(currentG, v);
			}
		}

		if (elementDrawer.getDrawingState().isRed())
			currentG.setColor(elementDrawer.getDrawingSpecifications()
					.getRedColor());
		else
			currentG.setColor(elementDrawer.getDrawingSpecifications()
					.getBlackColor());

		// Temporary graphic state :
		Graphics2D tmpG = (Graphics2D) currentG.create();

		ModelElement e = v.getModel();

		// Move, scale, etc... according to the current view
		tmpG.transform(v.getAffineTransform());
		// If this element contains sub-views
		// we draw them
		elementDrawer.drawElement(v, tmpG, false);
		
		if (v.getNumberOfSubviews() != 0) {
			// The base transformation.
			AffineTransform baseTransform = tmpG.getTransform();

			//			The current reference point :

			Point2D.Float ref= new Point2D.Float(0,0);

			int end = (endPos < v.getNumberOfSubviews() ? endPos : v
					.getNumberOfSubviews());

			for (int i = startPos; i < endPos && i < end; i++) {
				MDCView subv = v.getSubView(i);
				moveToReferenceNextPoint(ref,v,i);
				
				//Graphics2D subViewG= (Graphics2D) tmpG.create();
				Graphics2D subViewG = (Graphics2D) tmpG.create();

				// Final translation takes the view's dx,dy into account :
				subViewG.translate(ref.x + subv.getDx(), ref.y + subv.getDy());
				drawView(subViewG, subv, depth + 1);
				//subViewG.setTransform(baseTransform);
				subViewG.dispose();
				// When drawing the TopItemList, outline the current selection :
				if (v.getModel() instanceof TopItemList) {
					drawSelection(tmpG, i, ref.x, ref.y, subv);
				}
				// Position of the next element :
			}
		}
		//		Delegates actual element drawing to the ElementDrawer
		elementDrawer.drawElement(v, tmpG, true);
		// restore old coordinate system.
		tmpG.dispose();
		//g.setTransform(oldTransform);

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
	}

	/**
	 * Moves the reference point ref to the start position for subview number i of v.
	 * <p> Doesn't take the subview's dx,dy into account, as they have no influence on the current position.
	 * 
	 * <p> Typically, we can use it like this :
	 * <pre>
	 * 		Point2D.Float ref= new Point2D.Float(0,0);
	 *		for (int i = 0; i < v.getNumberOfSubViews(); i++) {
	 *			moveToReferenceNextPoint(ref,v,i);
	 *		}
	 * </pre>
	 * @param ref the point to be moved. Should correspond to the position of the previous subview.
	 * @param v
	 * @param i
	 */
	private void moveToReferenceNextPoint(Point2D.Float ref, MDCView v, int i) {
		RelativePosition previous; // position set by the previous view
		if (i == 0) {
			previous= v.getFirstSubViewPosition();
		} else {
			previous= v.getSubView(i-1).getNextViewPosition();
		}
		MDCView subv= v.getSubView(i);
		// First, move the references according to the specifications
		// in the previous subview.
		ref.x = previous.computeXTranslation(v, subv, ref.x);
		ref.y = previous.computeYTranslation(v, subv, ref.y);

		// Now, move the references according to our own specifications.

		ref.x = subv.getStartPoint().computeXTranslation(v, subv, ref.x);
		ref.y = subv.getStartPoint().computeYTranslation(v, subv, ref.y);
	}

	/**
	 * @param tmpG
	 * @param i :
	 *            the position in the current TopItemList
	 */
private void drawSelection(Graphics2D g, int i, float x, float y, MDCView v) {
		if (cursor != null && cursor.hasMark()) {
			int a = Math.min(cursor.getInsert().getIndex(), cursor.getMark()
					.getIndex());
			int b = Math.max(cursor.getInsert().getIndex(), cursor.getMark()
					.getIndex());
			if (a <= i && i < b) {
				float w= v.getWidth();
				float h= v.getHeight();
				if (v.getNextViewPosition().getXAnchor() == RelativePosition.PREVIOUS &&
					v.getNextViewPosition().getYAnchor() == RelativePosition.PREVIOUS)
				{
					w= v.getNextViewPosition().getDx();
					// TODO : take into account vertical text ???
					// AND right-to-left text also. Basically, use 
					// the values of dx and dy.
					//h= h+ v.getNextViewPosition().getDy();
				}
					
				g.setColor(new Color(0, 0, 255, 50));
				g.fill(new Rectangle2D.Double(x, y, w, h));
			}
		}
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
		if (cursor != null && cursor.getInsert() != null) {
			if (v.getModel() == cursor.getInsert().getPosition()
					.getElementAfter()) {
				g.setColor(elementDrawer.getDrawingSpecifications()
						.getCursorColor());
				g.setStroke(elementDrawer.getDrawingSpecifications()
						.getWideStroke());
				// IMPORTANT : get sure the cursor is correctly drawn, even if
				// startPoint is absolute.
				/*
				 * g2d.draw( new Line2D.Double( v.getStartPoint().getDx(),
				 * v.getStartPoint().getDy(), v.getStartPoint().getDx(),
				 * elementDrawer.getDrawingSpecifications().getMaxCadratHeight()));
				 */
				g.draw(new Double(0, -v.getDy(), 0, elementDrawer
						.getDrawingSpecifications().getMaxCadratHeight()));
			} else if (v.getModel() == cursor.getInsert().getPosition()
					.getElementBefore()
					&& v.getParent() != null
					&& v == v.getParent().getLastSubView()) {
				g.setColor(elementDrawer.getDrawingSpecifications()
						.getCursorColor());
				g.setStroke(elementDrawer.getDrawingSpecifications()
						.getWideStroke());
				g.draw(new Double(v.getWidth(), 0, v.getWidth(), elementDrawer
						.getDrawingSpecifications().getMaxCadratHeight()));
			}
		}
	}

	/**
	 * @param g
	 * @param v
	 * @return
	 */
	private boolean drawFromCache(Graphics2D g, MDCView v) {
		boolean result;
		BufferedImage img = (BufferedImage) cache.get(v.getModel());
		result = (img != null);
		if (result) {
			AffineTransform t = g.getTransform();
			g.setTransform(AffineTransform.getTranslateInstance(t
					.getTranslateX(), t.getTranslateY()));
			g.drawImage(img, -1, -1, null);
			g.setTransform(t);
		}
		return result;
	}

	/**
	 * actual drawing of a whole view.
	 * 
	 * @param g
	 * @param v
	 * @param depth
	 */
	private void drawView(Graphics2D g, MDCView v, int depth) {
		drawView(g, v, 0, v.getNumberOfSubviews(), depth);
	}

	/**
	 * Draws a gray rectangle over a view.
	 * 
	 * @param g
	 * @param v
	 */
	private void shadeView(Graphics2D g, MDCView v) {

		Paint oldPaint = g.getPaint();
		g.setPaint(elementDrawer.getDrawingSpecifications().getGrayColor());

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
			g.draw(new Line2D.Double(0, 0, v.getWidth(), v.getHeight()));

		}
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
	 * @return the position corresponding to the point, or null if the click corresponded to nothing..
	 */

	public MDCPosition getPositionForPoint(MDCView v, Point clickPoint,
			DrawingSpecifications ds) {
		int pos = -1;

		// NOTE : find the correct place for getPosition !
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

			//			The current reference point :
			Point2D.Float ref= new Point2D.Float(0,0);

			int i = 0;
			while (pos == -1 && i < v.getNumberOfSubviews()) {
				
				MDCView subv = v.getSubView(i);

				moveToReferenceNextPoint(ref,v,i);
				// Test if point falls into the current subview. If it's the
				// case,
				// sets pos :

				float x, y;
				x = ref.x + subv.getDx();
				y = ref.y + subv.getDy();

				if (point.x >= x && point.y >= y
						&& point.x < x + subv.getWidth() + ds.getSmallSkip()
						&& point.y < y + subv.getHeight()) {
					// TODO : the exact position choosed depends on the text
					// orientation.
					if (point.x < x + subv.getWidth() / 2.0f)
						pos = i;
					else
						pos = i + 1;
				}
				i++;
			}
		}
		//System.out.println("position : " + pos);
		if (pos == -1) 
			return null;
		else
			return new MDCPosition((TopItemList) v.getModel(), pos);
	}

	/**
	 * @return
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param b
	 */
	public void setDebug(boolean b) {
		debug = b;
	}

	/**
	 * Ask for clipping to be used to speed the drawing.
	 * 
	 * @param b
	 */
	public void setClip(boolean clip) {
		this.clip = clip;
	}

	/**
	 * @return
	 */
	public boolean isClip() {
		return clip;
	}

	/**
	 * Empties the cache. Should be called, in particular, when the scale is
	 * changed.
	 */
	void resetCache() {
		setCached(isCached());
		System.gc();
	}

	/**
	 * Request or prevent image caching. Image caching is only possible for
	 * raster output. In practice, its only use is for interactive displays of
	 * hieroglyphs.
	 * 
	 * @param c
	 */
	public void setCached(boolean c) {
		if (c)
			cache = new PictureCache(2000);
		else
			cache = null;
	}

	public boolean isCached() {
		return (cache != null);
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
	 * @return
	 */
	public boolean isPaged() {
		return elementDrawer.isPaged();
	}
	
	/**
	 * @param paged
	 */
	public void setPaged(boolean paged) {
		elementDrawer.setPaged(paged);
	}
	
	public boolean isShadeAfter() {
		return elementDrawer.isShadeAfter();
	}
	
	/**
	 * If ShadeAfter is true, the shading should be drawn <em>after</em> the shaded elements.
	 * For systems which support transparency, shadeAfter at <code>true</code> might be a good choice.
	 * Older systems (in the present case, wmf), should use shadeAfter= false.
	 * @param shadeAfter
	 */
	public void setShadeAfter(boolean shadeAfter) {
		elementDrawer.setShadeAfter(shadeAfter);
	}

	/**
	 * Returns the display coordinates of a given text position.
	 * @param position
	 */
	public Point2D getPointForPosition(MDCView v, MDCPosition position) {
		int i;
		Point2D.Float ref= new Point2D.Float(0,0);
		for (i=0; i < position.getIndex(); i++) {
			moveToReferenceNextPoint(ref, v, i);
		}
		// For the last index, we should take into account only the part that corresponds to
		// getNextViewPosition()
		if (i > 1) {
			RelativePosition relpos= v.getSubView(i-1).getNextViewPosition();
			MDCView dummyView= new MDCView(null);
			ref.x= relpos.computeXTranslation(v,dummyView,ref.x);
			ref.y= relpos.computeYTranslation(v,dummyView,ref.y);
		}
		return ref;
	}
	
	/**
	 * Returns A rectangle that contains parts of the signs which surround position.
	 * <p>it deals with the cases where  the first of these signs is a break (line or page).
	 * 
	 * @param position
	 */
	public Rectangle2D getRectangleAroundPosition(MDCView v, MDCPosition position, DrawingSpecifications specs) {
		int i;
		// Move to position i-1, computing the coordinates.
		Point2D.Float ref= new Point2D.Float(0,0);
		for (i=0; i < position.getIndex() && i < v.getNumberOfSubviews(); i++) {
			moveToReferenceNextPoint(ref, v, i);
		}
		// position is the next index. The rectangle should include both 
		// views i-1 and i (if they exist).
		Rectangle2D result= null;
		
		if (i-1 > 0) {
			MDCView subv= v.getSubView(i-1);
			// Artificially place the view to the next drawing position.
			MDCView dummyView= new MDCView(null);
			double x= subv.getNextViewPosition().computeXTranslation(v,dummyView,(float) ref.getX());
			double y= subv.getNextViewPosition().computeYTranslation(v,dummyView,(float) ref.getY());
			double w= 2;
			double h= specs.getMaxCadratHeight();
			result= new Rectangle2D.Double(x,y,w, h);
		}
		
		if (i< v.getNumberOfSubviews()) {
			MDCView subv= v.getSubView(i);
			moveToReferenceNextPoint(ref, v, i);
			Rectangle2D r2;
			r2= new Rectangle2D.Double(ref.getX(), ref.getY(), 1, specs.getMaxCadratHeight());
	
			if (result== null) {
				result= r2;
			} else {
				result.add(r2);
			}
		}
		
		if (result== null)
			result= new Rectangle2D.Double();
		//System.out.println(result);
		return result;
	}
}