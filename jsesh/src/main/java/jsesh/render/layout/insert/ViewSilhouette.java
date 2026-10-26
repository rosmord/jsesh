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

import jsesh.model.Hieroglyph;
import jsesh.model.Kerning;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.TextDirection;
import jsesh.platform.graphics.ShapeHelper;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.elements.SignPlacement;
import jsesh.render.view.MDCView;

/// Computes the silhouette of a laid-out view: the outlines of the signs it
/// contains, as they will be drawn.
///
/// This is a geometric query on the view tree, and needs no graphics device.
/// It follows the way `jsesh.render.draw.ViewDrawer` and `DefaultElementDrawer` place
/// views and signs (sub-view positions, view transforms, mirroring, base sign scale).
///
/// The silhouette is given in the view's own coordinate system, in the parent's scale: that
/// of its position and size. The view is supposed to be laid out.
///
/// Approximations:
///
/// - the standard body of signs is always used, even though drawing may select a small body
///   depending on the final zoom, which layout can't know;
/// - views without a known outline (text, unknown signs, shading) count as their box;
/// - spaces and kerning operators count as nothing.
public final class ViewSilhouette {

    private final HieroglyphDrawer hieroglyphDrawer;
    private final float baseSignScale;
    private final TextDirection textDirection;

    /// @param hieroglyphDrawer used to find the shape of signs.
    /// @param baseSignScale the scale from the font to the style.
    /// @param textDirection the direction of the text: signs are mirrored in right-to-left text.
    public ViewSilhouette(HieroglyphDrawer hieroglyphDrawer, float baseSignScale, TextDirection textDirection) {
        this.hieroglyphDrawer = hieroglyphDrawer;
        this.baseSignScale = baseSignScale;
        this.textDirection = textDirection;
    }

    /// The outlines of a view.
    ///
    /// @param view the view. It must be laid out.
    /// @return outlines which should be filled separately (each one has its own winding rule).
    public List<Shape> silhouette(MDCView view) {
        List<Shape> result = new ArrayList<>();
        collect(view, new AffineTransform(), result);
        return result;
    }

    private void collect(MDCView view, AffineTransform toRoot, List<Shape> result) {
        if (view.getNumberOfSubviews() == 0) {
            collectLeaf(view, toRoot, result);
            return;
        }
        // As in ViewDrawer: first the view's transform, then each sub view's origin.
        AffineTransform inner = new AffineTransform(toRoot);
        inner.concatenate(view.getAffineTransform());
        for (int i = 0; i < view.getNumberOfSubviews(); i++) {
            Rectangle2D bounds = view.getSubViewBounds(i);
            AffineTransform subTransform = new AffineTransform(inner);
            subTransform.translate(bounds.getMinX(), bounds.getMinY());
            collect(view.getSubView(i), subTransform, result);
        }
    }

    private void collectLeaf(MDCView view, AffineTransform toRoot, List<Shape> result) {
        if (view.getModel() instanceof Kerning) {
            return;
        }
        if (view.getModel() instanceof Hieroglyph h) {
            switch (h.getType()) {
                case SymbolCodes.HALFSPACE, SymbolCodes.FULLSPACE:
                    return;
                case SymbolCodes.MDCCODE:
                    if (hieroglyphDrawer.isKnown(h.getCode())) {
                        collectSign(h, view, toRoot, result);
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
        Rectangle2D box = new Rectangle2D.Double(0, 0, view.getWidth(), view.getHeight());
        result.add(toRoot.createTransformedShape(box));
    }

    private void collectSign(Hieroglyph h, MDCView view, AffineTransform toRoot, List<Shape> result) {
        // The same rule as DefaultElementDrawer.drawSign.
        boolean reversed = h.isReversed() ^ textDirection.equals(TextDirection.RIGHT_TO_LEFT);
        Shape glyph = ShapeHelper.transformShape(0, 0, 1, 1, (float) (h.getAngle() * Math.PI / 180),
                hieroglyphDrawer.getShape(h.getCode()));
        AffineTransform transform = new AffineTransform(toRoot);
        transform.concatenate(SignPlacement.glyphToView(reversed, view.getWidth(), baseSignScale, view.getXScale(),
                view.getYScale()));
        result.add(transform.createTransformedShape(glyph));
    }
}
