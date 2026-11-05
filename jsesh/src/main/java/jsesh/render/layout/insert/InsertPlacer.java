/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jsesh.sharedvocabulary.InsertionDirection;
import jsesh.render.view.MDCView;

/// Places the inserted element of an insertion, given the laid-out views of its two parts.
///
/// This is the geometric heart of the layout of `%insert`, shared by
/// `jsesh.render.layout.Layout` and by [InsertionRanker].
///
/// The result is given in *drawing* coordinates, as the signs are drawn: in
/// right-to-left text, "start" is on the right.
///
/// Right-to-left text must be drawn as the exact mirror image of left-to-right
/// text. The fit itself, which rasterises the shapes on a grid anchored on the
/// left of the frame, is not symmetric: fitting the mirrored signs may give a
/// slightly different position or scale. So the fit is always computed in the
/// left-to-right orientation: in right-to-left text, the silhouettes are mirrored
/// back before the fit, and the result is mirrored afterwards.
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
        // The fit is computed in the left-to-right orientation (see the class comment).
        InsertionFitter.HorizontalSide horizontal = horizontalSide(direction, false);
        InsertionFitter.VerticalSide vertical = verticalSide(direction);
        double mainWidth = mainView.getWidth();
        double insertedWidth = insertedView.getWidth();
        Rectangle2D mainBox = new Rectangle2D.Double(0, 0, mainWidth, mainView.getHeight());
        Rectangle2D insertedBox = new Rectangle2D.Double(0, 0, insertedWidth, insertedView.getHeight());
        List<Shape> mainShapes = silhouette.silhouette(mainView);
        List<Shape> insertedShapes = silhouette.silhouette(insertedView);
        if (rightToLeft) {
            mainShapes = mirror(mainShapes, mainWidth);
            insertedShapes = mirror(insertedShapes, insertedWidth);
        }
        Rectangle2D frame = InsertionFitter.enlargedFrame(mainBox, insertedBox, horizontal, vertical, dw, dh);
        InsertionFitter.Placement placement = fitter.fit(new InsertionFitter.Request(mainShapes, frame,
                insertedShapes, insertedBox, horizontal, vertical));
        if (rightToLeft) {
            // Back to drawing coordinates: the frame is mirrored across the main element's
            // box. A point x of the (drawn) inserted element is the point insertedWidth - x
            // of the fitted one, which went to tx + s (insertedWidth - x), mirrored into
            // mainWidth - tx - s insertedWidth + s x.
            frame = new Rectangle2D.Double(mainWidth - frame.getMaxX(), frame.getY(), frame.getWidth(),
                    frame.getHeight());
            placement = new InsertionFitter.Placement(placement.scale(),
                    mainWidth - placement.translateX() - placement.scale() * insertedWidth,
                    placement.translateY(), placement.fits());
        }
        return new Result(frame, placement);
    }

    /// Mirror outlines across the vertical axis of a box of a given width.
    private static List<Shape> mirror(List<Shape> shapes, double width) {
        AffineTransform t = new AffineTransform(-1, 0, 0, 1, width, 0);
        List<Shape> result = new ArrayList<>(shapes.size());
        for (Shape s : shapes) {
            result.add(t.createTransformedShape(s));
        }
        return result;
    }
}
