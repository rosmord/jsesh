package jsesh.render.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import jsesh.document.caret.MDCCaret;
import jsesh.model.Cadrat;
import jsesh.model.HieroglyphicText;
import jsesh.model.MDCPosition;
import jsesh.model.TextScope;
import jsesh.model.TopItem;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

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

    /// The colour used to highlight the selection.
    private static final Color SELECTION_COLOR = new Color(0, 0, 255, 50);

    /**
     * Should we clip the drawing to the visible area?
     */
    private boolean clip;

    /**
     * Should we draw the limits of subviews for debugging purposes?
     */
    private boolean debug = false;

    /// The caret whose selection is highlighted while drawing, if any. The
    /// cursor itself is not drawn here: see [CursorShape#paint].
    private MDCCaret selectionCaret = null;

    /**
     * The class used to draw individual elements.
     */
    private ElementDrawer elementDrawer;

    /**
     * This rectangle is in fact a local variable of drawView. We have placed it
     * here, because we want to avoid garbage collection of rectangles.
     * 
     * TODO: this is probably an outdated hack.
     */
    transient Rectangle temporaryRectangle = new Rectangle();

   
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
        this(new DefaultElementDrawer());
    }

    public ViewDrawer(ElementDrawer e) {
        setClip(false);
        elementDrawer = e;
    }

    /**
     * Draws a view and its subviews.
     *
     * @param g
     * @param view
     * @param ds
     */
    public void draw(Graphics2D g, JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext,
            MDCView view) {
        drawViewAndSelection(g, renderContext, techRenderContext, view, null, 0, view.getNumberOfSubviews());
    }

    /**
     * Draws a view and its subviews between two positions.
     * <p>
     * Note that positions fall <em>between</em> signs, so the first cadrat
     * stands between positions 0 and 1.
     *
     * @param g
     * @param view
     * @param start      First position
     * @param end        last position
     * @param jseshStyle
     */
    public void draw(Graphics2D g, JSeshRenderContext renderContext,
            JSeshTechRenderContext techRenderContext,
            MDCView view,
            MDCPosition start, MDCPosition end) {
        drawViewAndSelection(g, renderContext, techRenderContext, view, null, start.getIndex(), end.getIndex());
    }

    /// Draws a view and its subviews, and highlights the selection of a caret.
    ///
    /// This does *not* draw the cursor itself. Locate it with [TextGeometry#cursorAt(MDCCaret)] and call [CursorShape#paint] afterwards for this.
    ///
    /// @param g2d the graphics.
    /// @param renderContext the rendering context.
    /// @param techRenderContext the technical rendering context.
    /// @param view the view of the whole text.
    /// @param caret the caret whose selection is highlighted. If it is null, nothing is highlighted.
    public void drawViewAndSelection(Graphics2D g2d, JSeshRenderContext renderContext,
            JSeshTechRenderContext techRenderContext, MDCView view,
            MDCCaret caret) {
        drawViewAndSelection(g2d, renderContext, techRenderContext, view, caret, 0, view.getNumberOfSubviews());
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

    public boolean isShadeAfter() {
        return elementDrawer.isShadeAfter();
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

    /// Should kerning operators be drawn as marks (as paragraph marks in a word processor)?
    /// False by default.
    public void setShowKerningMarks(boolean showKerningMarks) {
        elementDrawer.setShowKerningMarks(showKerningMarks);
    }

    public boolean isShowKerningMarks() {
        return elementDrawer.isShowKerningMarks();
    }

    /// Should explicit line breaks be drawn as marks (as paragraph marks in a word processor)?
    /// False by default. Lines which are only wrapped to the window width have no mark.
    public void setShowLineBreakMarks(boolean showLineBreakMarks) {
        elementDrawer.setShowLineBreakMarks(showLineBreakMarks);
    }

    public boolean isShowLineBreakMarks() {
        return elementDrawer.isShowLineBreakMarks();
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
            } else if (v.getModel() instanceof HieroglyphicText) {
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
     * Highlight the view v if it belongs to the selection.
     *
     * @param g a graphics2D, whose origin should be v's top left corner.
     * @param i the position in the current HieroglyphicText
     * @param x x position of the view
     * @param y y position of the view
     * @param v the view
     */
    private void drawSelection(Graphics2D g, JSeshRenderContext renderContext, int i, MDCView v) {
        // A selection inside a cartouche... is drawn by drawNestedSelection.
        if (selectionCaret != null && selectionCaret.hasMark() && selectionCaret.getScope().isRoot()) {
            JSeshStyle jseshStyles = renderContext.jseshStyle();
            int a = selectionCaret.getMinPosition().getIndex();
            int b = selectionCaret.getMaxPosition().getIndex();
            if (a <= i && i < b) {
                float w = v.getWidth();
                float h = v.getHeight();

                // h and w are OK, but the inter-cadrat space is not filled.
                if (jseshStyles.options().textOrientation().isHorizontal()) {
                    if (v.nextIsHorizontallyAdjacent()) {
                        w += jseshStyles.geometry().smallSkip();
                    }
                } else {
                    h += jseshStyles.geometry().smallSkip();
                }
                // TODO : take into account vertical text ???
                // AND right-to-left text also. Basically, use
                // the values of dx and dy.
                // h= h+ v.getNextViewPosition().getDy();

                g.setColor(SELECTION_COLOR);
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
    private boolean drawView(Graphics2D g, JSeshRenderContext renderContext, MDCView v, int depth) {
        return drawView(g, renderContext, v, 0, v.getNumberOfSubviews(), depth);
    }

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
     * TODO : this method is too complex and messy.
     * 
     * @param g
     * @param v
     * @param startPos
     * @param endPos
     * @param depth    depth of the view, used to identify upper level groups.
     */
    private boolean drawView(Graphics2D g, JSeshRenderContext renderContext, MDCView v, int startPos, int endPos,
            int depth) {

        JSeshStyle jseshStyles = renderContext.jseshStyle();
        // Is safer...
        // Graphics2D currentG = (Graphics2D) g.create(); // so why was it commented??
        Graphics2D currentG = (Graphics2D) g;

        // Will probably be removed. Was used for caching.
        Color oldBackground = g.getBackground();

        // boolean shadedItem= false;
        // TODO : move this code up, in a drawing loop for HieroglyphicText ?
        // (this would be better anyway, for the test would be simpler,
        // without need for "depth")
        // Code for HieroglyphicText elements.
        if (depth == 1 && v.getModel() instanceof TopItem) {

            // The clipping functions allow us not to draw the text which is not
            // visible.
            if (clip) {
                temporaryRectangle = g.getClipBounds(temporaryRectangle);
                // If the view stands before the visible area, do nothing.
                // If the view stands after the visible area, do nothing.

                if (temporaryRectangle != null
                        && (v.getHeight() < temporaryRectangle.getMinY() || 0 > temporaryRectangle
                                .getMaxY())
                        || 0 > temporaryRectangle.getMaxX()
                        || v.getWidth() < temporaryRectangle.getMinX()) {
                    return false;
                }
            }

            TopItem topItem = (TopItem) v.getModel();
            elementDrawer.setDrawingState(topItem.getState());
        }

        // provide the correct
        // transform to g.
        // (BTW, the best way to do this would be to embed g in a class
        // containing the original transform).

        elementDrawer.setPageCoordinateSystem(pageCoordinateSystem);

        if (elementDrawer.getDrawingState().isRed()) {
            currentG.setColor(jseshStyles.painting().redColor());
        } else {
            currentG.setColor(jseshStyles.painting().blackColor());
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
        drawSubViews(tmpG, renderContext, v, startPos, endPos, depth);
        // restore old coordinate system.
        tmpG.dispose();

        // Part of the element drawn after the subviews ("post" mode).
        elementDrawer.drawElement(v, currentG, true);

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
     * @param g             where to draw ?
     * @param renderContext rendering context and style
     * @param v             the parent view
     * @param startPos      the first sub view to draw
     * @param endPos        the last
     * @param depth         the depth of the view.
     */
    private void drawSubViews(Graphics2D g, JSeshRenderContext renderContext, MDCView v, int startPos, int endPos,
            int depth) {
        // If this element contains sub-views
        // we draw them
        if (v.getNumberOfSubviews() != 0) {

            int end = (endPos < v.getNumberOfSubviews() ? endPos
                    : v
                            .getNumberOfSubviews());

            for (int i = startPos; i < endPos && i < end; i++) {
                MDCView subv = v.getSubView(i);

                // Graphics2D subViewG = (Graphics2D) g;
                // Find the position of the subview's top-left point.
                Rectangle2D subvBounds = v.getSubViewBounds(i);
                double subvx = subvBounds.getMinX();
                double subvy = subvBounds.getMinY();
                g.translate(subvx, subvy);
                // Draw the subview.
                boolean wasDrawn = drawView(g, renderContext, subv, depth + 1);
                // If the subview was selected, outline it.
                if (wasDrawn && v.getModel() instanceof HieroglyphicText) {
                    drawSelection(g, renderContext, i, subv);
                }
                g.translate(-subvx, -subvy);
            }
        }
    }

    /// Highlights a selection which lies inside a cartouche (or another nested
    /// scope): only the selected quadrats of the cartouche are highlighted (see
    /// [TextGeometry#selection(TextScope, int, int)]).
    ///
    /// @param g the graphics, in the state used to draw `view`.
    /// @param view the view of the whole text.
    /// @param style the style used to build the view.
    /// @param caret the caret (may be null).
    private void drawNestedSelection(Graphics2D g, MDCView view, JSeshStyle style, MDCCaret caret) {
        if (caret == null || !caret.hasSelection()) {
            return;
        }
        TextScope scope = caret.getScope();
        if (scope.isRoot()) {
            return;
        }
        new TextGeometry(view, style)
                .selection(scope, caret.getLocalMinPosition().getIndex(), caret.getLocalMaxPosition().getIndex())
                .paint(g, SELECTION_COLOR);
    }

    private void drawViewAndSelection(Graphics2D g2d, JSeshRenderContext renderContext,
            JSeshTechRenderContext techRenderContext,
            MDCView view,
            MDCCaret caret, int start, int end) {
        elementDrawer.prepareDrawing(renderContext, techRenderContext);
        this.selectionCaret = caret;
        this.pageCoordinateSystem = new PageCoordinateSystem(g2d);
        drawView(g2d, renderContext, view, start, end, 0);
        drawNestedSelection(g2d, view, renderContext.jseshStyle(), caret);
        this.selectionCaret = null; // To avoid short-term memory leak.
        this.pageCoordinateSystem = null;
        elementDrawer.cleanup();
    }
}
