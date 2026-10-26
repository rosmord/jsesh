/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import java.awt.geom.Rectangle2D;

import jsesh.model.constants.InsertionDirection;
import jsesh.render.view.MDCView;

/// Places the inserted element of an insertion, given the laid-out views of its two parts.
///
/// This is the geometric heart of the layout of `@insert`, shared by
/// `jsesh.render.layout.Layout` and by [InsertionRanker].
///
/// The computation is done in *drawing* coordinates, as the signs are drawn: in
/// right-to-left text, "start" is on the right.
public final class InsertPlacer {

    /// The result of a placement.
    ///
    /// @param frame the (possibly enlarged) frame of the main element, in the main element's coordinates.
    /// @param placement how the inserted element is scaled and moved, in the same coordinates.
    public record Result(Rectangle2D frame, InsertionFitter.Placement placement) {

        /// The box of the inserted element, once placed.
        public Rectangle2D placedBox(Rectangle2D insertedBox) {
            return placement.transform().createTransformedShape(insertedBox).getBounds2D();
        }
    }

    private final ViewSilhouette silhouette;
    private final boolean rightToLeft;
    private final InsertionFitter fitter = new InsertionFitter();

    /// @param silhouette computes the outlines of the views.
    /// @param rightToLeft true for right-to-left text.
    public InsertPlacer(ViewSilhouette silhouette, boolean rightToLeft) {
        this.silhouette = silhouette;
        this.rightToLeft = rightToLeft;
    }

    /// The horizontal side of an insertion, as drawn.
    public static InsertionFitter.HorizontalSide horizontalSide(InsertionDirection direction, boolean rightToLeft) {
        if (!direction.isHorizontal()) {
            return InsertionFitter.HorizontalSide.CENTER;
        }
        // "start" is on the right in right-to-left text.
        return (direction.isStart() ^ rightToLeft) ? InsertionFitter.HorizontalSide.LEFT
                : InsertionFitter.HorizontalSide.RIGHT;
    }

    /// The vertical side of an insertion.
    public static InsertionFitter.VerticalSide verticalSide(InsertionDirection direction) {
        if (!direction.isVertical()) {
            return InsertionFitter.VerticalSide.CENTER;
        }
        return direction.isTop() ? InsertionFitter.VerticalSide.TOP : InsertionFitter.VerticalSide.BOTTOM;
    }

    /// Place an inserted view in a main view.
    ///
    /// The views are not modified.
    ///
    /// @param direction where the inserted element goes.
    /// @param dw horizontal enlargement of the main element's frame, see [jsesh.model.Insert#getDw()].
    /// @param dh vertical enlargement of the main element's frame, see [jsesh.model.Insert#getDh()].
    /// @param mainView the laid-out view of the main element.
    /// @param insertedView the laid-out view of the inserted element, at its natural size.
    public Result place(InsertionDirection direction, int dw, int dh, MDCView mainView, MDCView insertedView) {
        InsertionFitter.HorizontalSide horizontal = horizontalSide(direction, rightToLeft);
        InsertionFitter.VerticalSide vertical = verticalSide(direction);
        Rectangle2D mainBox = new Rectangle2D.Double(0, 0, mainView.getWidth(), mainView.getHeight());
        Rectangle2D insertedBox = new Rectangle2D.Double(0, 0, insertedView.getWidth(), insertedView.getHeight());
        Rectangle2D frame = InsertionFitter.enlargedFrame(mainBox, insertedBox, horizontal, vertical, dw, dh);
        InsertionFitter.Placement placement = fitter.fit(new InsertionFitter.Request(silhouette.silhouette(mainView),
                frame, silhouette.silhouette(insertedView), insertedBox, horizontal, vertical));
        return new Result(frame, placement);
    }
}
