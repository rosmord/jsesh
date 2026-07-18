/**
 * Building views for manuel de codage texts, and drawing them.
 *
 * <p>This package is made of two parts. The first deals with building a view
 * for a manuel de codage text, and in particular, placing the different
 * subviews.
 *
 * <p>Once these subviews are built, the view can be drawn on screen (or
 * printer, or PDF file...); this is the task of the drawer classes.
 * {@code HieroglyphsDrawer} is specialized in hieroglyphs, and is also used
 * while building the view, because it knows about sign dimensions.
 * {@code ViewDrawer} makes very few decisions, and mechanically draws a view.
 * A benefit of this approach is that things like view dimensions are easier to
 * compute.
 *
 * <p>The actual drawing of the view is done, for individual elements, by an
 * {@code ElementDrawer}. A default implementation is proposed, but it can be
 * changed if one wants, for instance, to change the drawing of cartouches.
 *
 * <p>A problem we need to handle now concerns the passing of data such as
 * drawing specifications. It's quite clear that in most cases the user will
 * want all their hieroglyphic rendering to look the same, so we probably want
 * to share the drawing specifications. On the other hand, it should be
 * possible to change them on a widget-by-widget basis.
 *
 * <h2>Rendering text with multiple orientations</h2>
 *
 * <p>Basically, we build regions of text with homogeneous orientation. In the
 * same page, when a region is built, it's added to the page.
 *
 * <p>The problem is <em>where</em>? To solve it, we use the metaphor of a
 * <em>current cursor</em>. The cursor's place will depend on the
 * <em>global</em> text orientation, and on the previous text orientation.
 * Suppose a global orientation right-to-left; let's consider a right-to-left
 * sequence <em>A,B</em>, then a left-to-right sequence <em>C,D</em>, then a
 * second right-to-left sequence <em>E</em>.
 *
 * <p>The rendering will be ECDBA. Thus the cursor, after <em>B</em>, would be
 * placed at the left of <em>B</em>.
 *
 * <p>Now, if the global orientation is left-to-right, the rendering would be
 * BACDE; the cursor after <em>B</em> would be placed to the <em>right</em> of
 * B. In general, the cursor position in a homogeneous sequence is a function
 * of the sequence orientation; the cursor position when the orientation
 * changes is a function of the <em>global</em> orientation.
 */
package jsesh.render.draw;
