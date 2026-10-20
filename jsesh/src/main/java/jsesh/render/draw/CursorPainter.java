package jsesh.render.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Optional;

import jsesh.document.caret.MDCCaret;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;

/// Draws the cursor of a [MDCCaret] on top of an already drawn view.
///
/// The cursor geometry is computed by [CursorLocator] from the view tree; this
/// class only strokes it. It is independent from [ViewDrawer].
public final class CursorPainter {

    private CursorPainter() {
    }

    /// Draws the cursor.
    ///
    /// @param g the graphics, in the same state as the one given to [ViewDrawer] to draw `view`.
    /// @param renderContext the rendering context (its style gives the cursor's color and stroke).
    /// @param view the view of the whole text.
    /// @param caret the caret. If it is null, nothing is drawn.
    public static void paint(Graphics2D g, JSeshRenderContext renderContext, MDCView view, MDCCaret caret) {
        if (caret == null || caret.getInsert() == null) {
            return;
        }
        JSeshStyle style = renderContext.jseshStyle();
        Optional<CursorLocator.Location> location = CursorLocator.locate(view, caret.getInsertExactPosition(), style);
        if (location.isEmpty()) {
            return;
        }
        Graphics2D tmpG = (Graphics2D) g.create();
        try {
            // The cursor lines are expressed in the system used to draw the subviews of view.
            tmpG.transform(view.getAffineTransform());
            tmpG.setColor(style.painting().cursorColor());
            tmpG.setStroke(location.get().wide() ? style.geometry().wideStroke() : style.geometry().fineStroke());
            for (Line2D line : location.get().lines()) {
                tmpG.draw(line);
            }
        } finally {
            tmpG.dispose();
        }
    }
}
