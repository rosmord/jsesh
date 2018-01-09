package jsesh.mdcDisplayer.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import jsesh.editor.caret.MDCCaret;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Knows how to draw a view and its subviews.
 *
 * A viewdrawer delegates the actual drawing of its elements to an
 * ElementDrawer.
 *
 *
 * @author rosmord
 *
 */
public class ViewDrawer {

    /**
     * A map of bitmaps, which serves as a cache for already drawn signs.
     */
    private PictureCache imageCache;

    /**
     * A map of views, which links an element to its view (and reciprocally).
     *
     * REMOVED : it belongs in the document view, which should be created as a
     * class. WeakHashMap<TopItem, MDCView> viewMap= new
     * WeakHashMap<TopItem, MDCView>();
     */
    private boolean clip;

    /**
     * A caret to draw, if wanted.
     */
    private MDCCaret cursor = null;

    private boolean debug = false;

    private ElementDrawer elementDrawer;

    private DrawingSpecification drawingSpecifications;

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
     * Temporary affine transformation for the following method (to avoid gc).
     * (I don't know if it's useful any more. It might have been one day). TODO
     * SUPPRESS THIS VARIABLE..
     */
    private AffineTransform temporaryTransform = null;

    /**
     * The original coordinate system. We need to keep the original
     * transformation, for certain computation are done in the page's
     * coordinates.
     */
    private PageCoordinateSystem pageCoordinateSystem;

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
    public void draw(Graphics2D g, MDCView view, DrawingSpecification ds) {
        drawViewAndCursor(g, view, null, ds);
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
     * @param start First position
     * @param end last position
     */
    public void draw(Graphics2D g, MDCView view, DrawingSpecification ds,
            MDCPosition start, MDCPosition end) {
        drawViewAndCursor(g, view, null, ds, start, end);
    }

    /**
     * Draws colored rectangle showing the structure of a view for debugging
     * purporses.
     *
     * @param g
     * @param v
     */
    private void drawDebug(Graphics2D g, MDCView v) {
        Graphics2D tmpg = (Graphics2D) g.create();
        if (v.getSubViews() != null && v.getSubViews().size() != 0) {
            if (v.getModel() instanceof Cadrat) {
                tmpg.setColor(Color.GREEN);
                tmpg.setStroke(new BasicStroke(0.4f));
                tmpg.draw(new Line2D.Double(0, v.getHeight(), v.getWidth(), 0));
            } else if (v.getModel() instanceof TopItemList) {
                tmpg.setStroke(new BasicStroke(1f));
                tmpg.setColor(Color.MAGENTA);

            } else {

                tmpg.setStroke(new BasicStroke(0.1f));
                tmpg.setColor(Color.RED);
            }
            tmpg
                    .draw(new Rectangle2D.Double(0, 0, v.getWidth(), v
                            .getHeight()));

        } else {
            tmpg.setColor(Color.ORANGE);
            tmpg.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, (float) 0.5));
            tmpg.draw(new Line2D.Double(0, 0, v.getWidth(), v.getHeight()));
            tmpg
                    .draw(new Rectangle2D.Double(0, 0, v.getWidth(), v
                            .getHeight()));
        }
        tmpg.dispose();
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
        BufferedImage img = imageCache.get(v.getModel());
        result = (img != null);
        if (result) {
            AffineTransform t = g.getTransform();
            if (temporaryTransform == null) {
                temporaryTransform = new AffineTransform();
            }
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
     * @param g a graphics2D, whose origin should be v's top left corner.
     * @param i the position in the current TopItemList
     * @param x x position of the view
     * @param y y position of the view
     * @param v the view
     */
    private void drawSelection(Graphics2D g, int i, MDCView v) {
        if (cursor != null && cursor.hasMark()) {
            int a = Math.min(cursor.getInsert().getIndex(), cursor.getMark()
                    .getIndex());
            int b = Math.max(cursor.getInsert().getIndex(), cursor.getMark()
                    .getIndex());
            if (a <= i && i < b) {
                float w = v.getWidth();
                float h = v.getHeight();

                // h and w are OK, but the inter-cadrat space is not filled.
                if (drawingSpecifications.getTextOrientation().isHorizontal()) {
                    if (v.nextIsHorizontallyAdjacent()) {
                        w += drawingSpecifications.getSmallSkip();
                    }
                } else {
                    h += drawingSpecifications.getSmallSkip();
                }
                // TODO : take into account vertical text ???
                // AND right-to-left text also. Basically, use
                // the values of dx and dy.
                // h= h+ v.getNextViewPosition().getDy();

                g.setColor(new Color(0, 0, 255, 50));
                g.fill(new Rectangle2D.Double(0, 0, w, h));
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

    // TODO : this method is too complex and messy.
    // Really move the caching system out. It doesn't belong here.
    private boolean drawView(Graphics2D g, MDCView v, int startPos, int endPos,
            int depth) {

        // Is safer...
        // Graphics2D currentG = (Graphics2D) g.create();
        Graphics2D currentG = (Graphics2D) g;

        // For top level items, with bitmap output, we may use caching.
        // with caching, the picture of a view is drawn into a bitmap,
        // which is then copied to g.
        // this doesn't speed up the initial drawing, but it gives a
        // definite boost for small modifications, given that the actual drawing
        // of splines and the like is much more time consuming in practice than
        // anything else.
        BufferedImage img = null;
        Color oldBackground = g.getBackground();

        // boolean shadedItem= false;
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
                        || v.getWidth() < temporaryRectangle.getMinX()) {
                    return false;
                }
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
                    img = imageCache.createImage(x + 2, y + 2);
                    // If got one, we will draw in it.
                    // if we couldn't get one, we will draw into the old graphic
                    // context.
                    if (img != null) {
                        // Compute a "mock" original transform...
                        elementDrawer
                                .setPageCoordinateSystem(pageCoordinateSystem
                                        .createZeroTranslationCoordinateSystem());

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
                // Set background to grey = hence, the content of the glyphs
                // will be shaded,
                // Which alleviate the limitations of systems like WMF or PICT
                // (which have no transparency)
                // Note that we leave this code here, and don't put it in
                // shadeview,
                // as we want to remember that we must restore the BG
                // afterwards.
                // (ok, we might as well systematically save and restore the BG
                // and the FG before and after drawing).

                // currentG.setBackground(drawingSpecifications.getGrayColor());
                // shadeView(currentG, v);
                // shadedItem= true;
            }
        }

        // If we are not using the cache system, provide the correct
        // transform to g.
        // (BTW, the best way to do this would be to embed g in a class
        // containing the original transform).
        if (img == null) {
            elementDrawer.setPageCoordinateSystem(pageCoordinateSystem);
        }

        if (elementDrawer.getDrawingState().isRed()) {
            currentG.setColor(drawingSpecifications.getRedColor());
        } else {
            currentG.setColor(drawingSpecifications.getBlackColor());
        }

        // Part of the element drawn before the element's subviews
        // This is mainly used for shading : shading can be done before or after
        // the sign has been drawn.
        elementDrawer.drawElement(v, currentG, false);

        // Temporary graphic state :
        Graphics2D tmpG = (Graphics2D) currentG.create();

        // Move, scale, etc... according to the current view
        tmpG.transform(v.getAffineTransform());

        // Draw the element's subviews.
        drawSubViews(v, startPos, endPos, depth, tmpG);
        // restore old coordinate system.
        tmpG.dispose();

        // Part of the element drawn after the subviews ("post" mode).
        elementDrawer.drawElement(v, currentG, true);

        // Save the image in the cache and draws it...
        if (img != null) {
            imageCache.put(v.getModel(), img);
            drawFromCache(g, v);
        }

        // Draw the cursor if needed.
        testAndDrawCursor(g, v);

        // Dispose the graphic from the cache...
        if (img != null) {
            currentG.dispose();
        }

        if (g.getBackground() != oldBackground) {
            g.setBackground(oldBackground);
        }
        // Temporary : draw a red rectangle around the view
        if (debug) {
            drawDebug(g, v);
        }
        return true;
    }

    /**
     * Draws the views contained in a parent view.
     *
     * @param v the parent view
     * @param startPos the first sub view to draw
     * @param endPos the last
     * @param depth the depth of the view.
     * @param g where to draw ?
     */
    private void drawSubViews(MDCView v, int startPos, int endPos, int depth,
            Graphics2D g) {
        // If this element contains sub-views
        // we draw them
        if (v.getNumberOfSubviews() != 0) {

            int end = (endPos < v.getNumberOfSubviews() ? endPos : v
                    .getNumberOfSubviews());

            for (int i = startPos; i < endPos && i < end; i++) {
                MDCView subv = v.getSubView(i);

                // Graphics2D subViewG = (Graphics2D) g;
                // Find the position of the subview's top-left point.
                double subvx, subvy;
                if (v.getDirection().isLeftToRight()) {
                    subvx = subv.getPosition().x;
                    subvy = subv.getPosition().y;
                } else {
                    subvx = v.getInternalWidth() - subv.getPosition().x
                            - subv.getWidth();
                    subvy = subv.getPosition().y;
                }
                g.translate(subvx, subvy);
                // Draw the subview.
                boolean wasDrawn = drawView(g, subv, depth + 1);
                // If the subview was selected, outline it.
                if (wasDrawn && v.getModel() instanceof TopItemList) {
                    drawSelection(g, i, subv);
                }
                g.translate(-subvx, -subvy);
            }
        }
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
    public void drawViewAndCursor(Graphics2D g2d, MDCView view,
            MDCCaret cursor, DrawingSpecification ds) {
        drawViewAndCursor(g2d, view, cursor, ds, 0, view.getNumberOfSubviews());
    }

    /**
     * Draws a view, its subviews, and the cursor between two positions.
     * <p>
     * Note that positions fall <em>between</em> signs, so the first cadrat
     * stands between positions 0 and 1.
     *
     * @param g2d
     * @param view
     * @param cursor : the cursor position. If it's null, the cursor is not
     * drawn.
     * @param ds
     * @param start
     * @param end
     */
    public void drawViewAndCursor(Graphics2D g2d, MDCView view,
            MDCCaret cursor, DrawingSpecification ds, MDCPosition start,
            MDCPosition end) {
        drawViewAndCursor(g2d, view, cursor, ds, start.getIndex(), end
                .getIndex());
    }

    private void drawViewAndCursor(Graphics2D g2d, MDCView view,
            MDCCaret cursor, DrawingSpecification ds, int start, int end) {
        drawingSpecifications = ds;
        elementDrawer.prepareDrawing(drawingSpecifications);
        this.cursor = cursor;
        this.pageCoordinateSystem = new PageCoordinateSystem(g2d);
        // conceptual hack (patch) : if cursor is not empty, and text is empty,
        // draws a cursor.
        if (start == end && cursor != null) {
            drawCursorAtFirstPosition(g2d, view);
        }
        drawView(g2d, view, start, end, 0);
        this.cursor = null; // To avoid short-term memory leak.
        this.pageCoordinateSystem = null;
        elementDrawer.cleanup();
    }

    /**
     * flushes the cache if there is one.
     * <p>
     * should be called if the case contents becomes obsolete, e.g. if the
     * drawing scale is changed.
     */
    public void flushCache() {
        if (isCached()) {
            imageCache.reset();
        }
    }

    /**
     * Returns the display coordinates of a given text position.
     *
     * @param v
     * @param position
     * @return the display coordinates of a given text position.
     */
    public Point2D getPointForPosition(MDCView v, MDCPosition position) {
        Point2D ref;

        if (position.getIndex() == 0) {
            ref = new Point2D.Float(0, 0);
        } else if (position.getIndex() < v.getNumberOfSubviews()) {
            MDCView subv = v.getSubView(position.getIndex());
            ref = new Point2D.Double(subv.getPosition().x, subv.getPosition().y);
        } else {
            MDCView subv = v.getSubView(v.getNumberOfSubviews() - 1);
            ref = new Point2D.Double(subv.getPosition().x + subv.getWidth(),
                    subv.getPosition().y);
        }
        return ref;
    }

    /**
     * Mapping from display space to model space, mostly to manage mouse clicks.
     *
     * @param v a view for a <em>TopItemList</em> (this might and should change
     * ?)
     * @param clickPoint : a point, its coordinates expressed in display space.
     * Note that in the case of JMDCDisplayer, display space scaling should be
     * applied before calling this function, as the drawer knows nothing of it.
     * @param ds DrawingSpecifications
     * @return the position corresponding to the point, or null if the click
     * corresponded to nothing..
     */
    public MDCPosition getPositionForPoint(MDCView v, Point clickPoint,
            DrawingSpecification ds) {
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

            int i = 0;
            while (pos == -1 && i < v.getNumberOfSubviews()) {

                MDCView subv = v.getSubView(i);

                // Test if point falls into the current subview. If it's the
                // case,
                // sets pos :
                double x, y;
                x = subv.getPosition().x;
                if (v.getDirection().equals(TextDirection.RIGHT_TO_LEFT)) {
                    x = v.getInternalWidth() - x - subv.getWidth();
                }
                y = subv.getPosition().y;

                // First test, the same in all cases : does the point fall
                // inside a view ?
                if (point.x >= x && point.y >= y
                        && point.x < x + subv.getWidth() + ds.getSmallSkip()
                        && point.y < y + subv.getHeight() + ds.getSmallSkip()) {
                    // TODO : the exact position choosed depends on the text
                    // orientation.
                    if (ds.getTextOrientation().isHorizontal()) {
                        if (point.x < x + subv.getWidth() / 2.0f) {
                            if (ds.getTextDirection().isLeftToRight()) {
                                pos = i;
                            } else {
                                pos = i + 1;
                            }
                        } else if (ds.getTextDirection().isLeftToRight()) {
                            pos = i + 1;
                        } else {
                            pos = i;
                        }
                    } else { // Columns
                        if (point.y < y + subv.getHeight() / 2.0f) {
                            pos = i;
                        } else {
                            pos = i + 1;
                        }

                    }
                } else if (ds.getTextOrientation().isHorizontal()) {
                    // Text for end of line.
                    // if (ds.getTextDirection().isLeftToRight()) {
                    if (y > point.y) {
                        pos = (i > 0 ? i - 1 : 0);
                    }
                    // }
                } else {
                    if (ds.getTextDirection().isLeftToRight()) {
                        if (x > point.x) {
                            pos = i;
                        }
                    } else {
                        if (x + ds.getMaxCadratWidth() < point.x) {
                            pos = i;
                        }
                    }
                }
                i++;
            }
        }

        // If nothing was found, chose the last cadrat.
        if (pos == -1) {
            return new MDCPosition((TopItemList) v.getModel(), v.getModel()
                    .getNumberOfChildren());
        } else {
            return new MDCPosition((TopItemList) v.getModel(), pos);
        }
    }

    /**
     * Returns A rectangle that surrounds a specific text position.
     * <p>
     * it deals with the cases where one of these signs is a break (line or
     * page).
     *
     * @param v
     * @param position
     * @param specs
     * @return A rectangle that contains the signs which surround position.
     */
    public Rectangle2D getRectangleAroundPosition(MDCView v,
            MDCPosition position, DrawingSpecification specs) {
        // position is the next index. The rectangle should include both
        // views i-1 and i (if they exist).

        int index = position.getIndex();
        Rectangle2D result = new Rectangle2D.Float(0, 0, specs.getSmallSkip(),
                specs.getMaxCadratHeight());

        // non empty view :
        if (v.getNumberOfSubviews() != 0) {
            boolean previousIsBreak = false;
            boolean nextIsBreak = false;

            Rectangle2D previousRectangle = new Rectangle2D.Float(0, 0, specs
                    .getSmallSkip(), specs.getMaxCadratHeight());
            Rectangle2D nextRectangle = new Rectangle2D.Float(0, v.getHeight()
                    - specs.getMaxCadratHeight(), specs.getSmallSkip(), specs
                    .getMaxCadratHeight());

            if (index > 0) {
                previousRectangle = new Rectangle2D.Double(v.getSubView(
                        index - 1).getPosition().x
                        + v.getSubView(index - 1).getWidth(), v.getSubView(
                        index - 1).getPosition().y, 1, v.getSubView(index - 1)
                                .getHeight());
            }
            if (index < v.getNumberOfSubviews()) {
                nextRectangle = new Rectangle2D.Double(v.getSubView(index)
                        .getPosition().x, v.getSubView(index).getPosition().y,
                        1, v.getSubView(index).getHeight());
            }

            if (index <= 0 || v.getSubView(index - 1) == null
                    || v.getSubView(index - 1).getModel().isBreak()) {
                previousIsBreak = true;
            }

            if (index >= v.getNumberOfSubviews() || v.getSubView(index) == null
                    || v.getSubView(index).getModel().isBreak()) {
                nextIsBreak = true;
            }

            if (previousIsBreak) {
                result = nextRectangle;
            } else if (nextIsBreak) {
                result = previousRectangle;
            } else {
                result = previousRectangle;
                result.add(nextRectangle);
            }
        }
        // Now, the rectangle has been computed regardless of the view's
        // direction. Correct it :
        if (v.getDirection().equals(TextDirection.RIGHT_TO_LEFT)) {
            double x, y, w, h;
            x = result.getMinX();
            y = result.getMinY();
            w = result.getWidth();
            h = result.getHeight();
            x = v.getInternalWidth() - x - w;
            result = new Rectangle2D.Double(x, y, w, h);
        }
        return result;
    }

    public boolean isCached() {
        return (imageCache != null);
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
        return drawingSpecifications.isPaged();
    }

    public boolean isShadeAfter() {
        return elementDrawer.isShadeAfter();
    }

    /*
	 * Empties the cache. Should be called, in particular, when the scale is
	 * changed.
	 * 
	 * void resetCache() { setCached(isCached()); System.gc(); }
     */
    /**
     * Request or prevent image and view caching. Image caching is only possible
     * for raster output. In practice, its only use is for interactive displays
     * of hieroglyphs.
     *
     * View caching is needed for caret to be found and interactive edition in
     * general. We will probably separate the two issues.
     *
     * @param c
     */
    public void setCached(boolean c) {
        if (c) {
            imageCache = new PictureCache(1000);
        } else {
            imageCache = null;
        }
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
     * Conditional cursor drawing : if the current position corresponds to the
     * cursor, draw it. When the cursor is at the last position, the first test
     * never works. (we might want to use some sentinel to correct this). so we
     * have a special case. For empty text, we will write some specific code (at
     * upper levels ?)
     *
     * <p>
     * We should change the cursor drawing system, because it's not logical.
     * Cursor drawing should be linked with the view that contains the cursor,
     * not with views around the cursor.
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

            /*
			 * A cursor stands between two views. Should we draw the cursor
			 * after or before the view ???
             */
            boolean drawAfterView;
            boolean drawBeforeView;

            drawBeforeView = v.getModel() == cursor.getInsert().getPosition()
                    .getElementAfter();

            drawAfterView = v.getModel() == cursor.getInsert().getPosition()
                    .getElementBefore();

            // We draw cursors at regular elements, not at breaks, but between
            // two breaks, we need to do something anyway.
            // Hence, if there are two breaks, we favour the "draw before view"
            // tactic.
            // Cases "against" drawBeforeView
            if (drawBeforeView) {
                if (v.getModel() != null
                        && v.getModel().isBreak()
                        && (v.getPrevious() != null
                        && v.getPrevious().getModel() != null && !v
                        .getPrevious().getModel().isBreak())) {
                    drawBeforeView = false;
                }
            }

            // Cases "against" drawAfterView
            if (drawAfterView) {
                if (v.getModel().isBreak()) {
                    drawAfterView = false;
                }
            }

            if (drawAfterView || drawBeforeView) {
                Color currentColor = g.getColor();
                g.setColor(drawingSpecifications.getCursorColor());
                g.setStroke(drawingSpecifications.getWideStroke());

                if (drawAfterView) {

                    if (textOrientation.isHorizontal()) {
                        if (textDirection.isLeftToRight()) {
                            g
                                    .draw(new Line2D.Double(v.getWidth(), 0, v
                                            .getWidth(), drawingSpecifications
                                                    .getMaxCadratHeight()));
                        } else {
                            g.draw(new Line2D.Double(0, 0, 0, drawingSpecifications
                                    .getMaxCadratHeight()));
                        }
                    } else {
                        g.draw(new Line2D.Double(0, v.getHeight(),
                                drawingSpecifications.getMaxCadratWidth(), v
                                .getHeight()));
                    }
                } else if (drawBeforeView) {
                    if (textOrientation.isHorizontal()) {
                        if (textDirection.isLeftToRight()) {
                            g.draw(new Line2D.Double(0, 0, 0, drawingSpecifications
                                    .getMaxCadratHeight()));
                        } else {
                            g
                                    .draw(new Line2D.Double(v.getWidth(), 0, v
                                            .getWidth(), drawingSpecifications
                                                    .getMaxCadratHeight()));
                        }
                    } else { // columns
                        g.draw(new Line2D.Double(0, 0, drawingSpecifications
                                .getMaxCadratWidth(), 0));

                    }
                }
                // Only restore color. The stroke should be set by all those who
                // want to draw, anyway
                // (whereas color is contextual stuff)
                g.setColor(currentColor);
            }
        }
    }

    /**
     * Draw the cursor (NOT USED).
     *
     * @param g2d
     * @param documentMdcView
     * @param mdcCaret
     * @param drawingSpecifications2
     */
    private void drawCursor(Graphics2D g2d, MDCView documentMdcView, MDCCaret mdcCaret,
            DrawingSpecification drawingSpecifications2) {
        // First, decide which element will be used to determine the cursor position (if any)
        // At the same time, decide which position (before or after the view will be used)
        // Find the view used as basis for the position
        // compute the cursor characteristics from there (note that it's a bit tricky after new pages)
        // if there is no element at all around the cursor, draw the cursor at the beginning of the text.
        MDCPosition insert = mdcCaret.getInsertPosition();
        //ERROR
        // Try to find the element in the hash table
        // if not, perform a recursive search ???

        // To be continued...
    }

    /**
     * Simple hack used to draw cursors when there is no text. It's not nice and
     * will be changed.
     *
     * @param g
     * @param view
     */
    private void drawCursorAtFirstPosition(Graphics2D g, MDCView view) {
        TextDirection textDirection = drawingSpecifications.getTextDirection();
        TextOrientation textOrientation = drawingSpecifications
                .getTextOrientation();
        Color currentColor = g.getColor();
        g.setColor(drawingSpecifications.getCursorColor());
        g.setStroke(drawingSpecifications.getWideStroke());

        if (textOrientation.isHorizontal()) {
            g.draw(new Line2D.Double(0, 0, 0, drawingSpecifications
                    .getMaxCadratHeight()));
        } else {
            g.draw(new Line2D.Double(0, 0,
                    drawingSpecifications.getMaxCadratWidth(), 0));
        }
        // Only restore color. The stroke should be set by all those who
        // want to draw, anyway
        // (whereas color is contextual stuff)

        g.setColor(currentColor);
    }
}
