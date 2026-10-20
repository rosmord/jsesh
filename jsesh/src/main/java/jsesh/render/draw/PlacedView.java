/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.draw;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import jsesh.model.ModelElement;
import jsesh.render.view.MDCView;

/// A view of the tree, placed with respect to the root view.
///
/// Two coordinate systems are involved:
///
/// - the *local* system, used to draw the subviews of this view;
/// - the *root* system, used to draw the subviews of the root view (that is,
///   after applying [MDCView#getAffineTransform()] of the root view).
///
/// A placed view is reached from the root by following a path of child
/// indices, applying the same transformations as [ViewDrawer] on the way.
/// It is immutable.
final class PlacedView {

    private final MDCView view;

    /// From the local system to the root system.
    private final AffineTransform toRoot;

    private PlacedView(MDCView view, AffineTransform toRoot) {
        this.view = view;
        this.toRoot = toRoot;
    }

    /// @param rootView the view of the whole text.
    /// @return the root view, as a placed view.
    static PlacedView root(MDCView rootView) {
        return new PlacedView(rootView, new AffineTransform());
    }

    MDCView view() {
        return view;
    }

    ModelElement model() {
        return view.getModel();
    }

    int childCount() {
        return view.getNumberOfSubviews();
    }

    MDCView subView(int i) {
        return view.getSubView(i);
    }

    /// The `i`th subview, placed.
    ///
    /// @param i the index of the subview, which is also the index of its
    ///        model in the model of this view.
    /// @return the subview, or empty if the views don't match the model (for
    ///         instance if the view is not up to date).
    Optional<PlacedView> child(int i) {
        ModelElement model = view.getModel();
        if (model == null || i < 0 || i >= model.getNumberOfChildren() || i >= view.getNumberOfSubviews()) {
            return Optional.empty();
        }
        MDCView sub = view.getSubView(i);
        if (sub.getModel() != model.getChildAt(i)) {
            return Optional.empty();
        }
        // Same transformations as ViewDrawer.drawSubViews and drawView.
        AffineTransform subToRoot = new AffineTransform(toRoot);
        subToRoot.translate(originX(sub), sub.getPosition().y);
        subToRoot.concatenate(sub.getAffineTransform());
        return Optional.of(new PlacedView(sub, subToRoot));
    }

    /// Follows a path of child indices (a model path, as in
    /// [jsesh.model.MDCPosition]) down from this view.
    ///
    /// @param path the child indices.
    /// @return the view reached (this one for an empty path), or empty if the
    ///         views don't match the path.
    Optional<PlacedView> follow(int[] path) {
        Optional<PlacedView> current = Optional.of(this);
        for (int i : path) {
            current = current.flatMap(v -> v.child(i));
        }
        return current;
    }

    /// The area occupied by a subview, in the local system.
    Rectangle2D childBounds(int i) {
        MDCView sub = view.getSubView(i);
        return new Rectangle2D.Double(originX(sub), sub.getPosition().y, sub.getWidth(), sub.getHeight());
    }

    /// A line of the local system, in the root system.
    Line2D toRoot(Line2D line) {
        if (toRoot.isIdentity()) {
            return line;
        }
        Point2D p1 = toRoot.transform(line.getP1(), null);
        Point2D p2 = toRoot.transform(line.getP2(), null);
        return new Line2D.Double(p1, p2);
    }

    /// The bounds of an area of the local system, in the root system.
    Rectangle2D toRoot(Rectangle2D area) {
        return toRoot.createTransformedShape(area).getBounds2D();
    }

    /// Abscissa of the top-left corner of a subview, in the local system
    /// (right-to-left views are mirrored).
    private double originX(MDCView sub) {
        if (view.getDirection().isLeftToRight()) {
            return sub.getPosition().x;
        }
        return view.getInternalWidth() - sub.getPosition().x - sub.getWidth();
    }
}
