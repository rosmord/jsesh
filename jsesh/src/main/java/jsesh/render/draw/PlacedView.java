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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Optional;

import jsesh.model.ModelElement;
import jsesh.render.view.MDCView;

/// A view of the tree, placed with respect to the root view.
///
/// Two coordinate systems are involved:
///
/// - the *local* system, used to draw the subviews of this view;
/// - the *root* system, the one of the `Graphics2D` given to [ViewDrawer] to
///   draw the root view (that is, *before* applying
///   [MDCView#getAffineTransform()] of the root view).
///
/// A placed view is reached from the root by following a path of child
/// indices, applying the same transformations as [ViewDrawer] on the way.
/// It is immutable.
final class PlacedView {

    private final MDCView view;

    /// From the local system to the root system.
    private final AffineTransform toRoot;

    /// The child indices leading from the root view to this one.
    private final int[] path;

    private PlacedView(MDCView view, AffineTransform toRoot, int[] path) {
        this.view = view;
        this.toRoot = toRoot;
        this.path = path;
    }

    /// @param rootView the view of the whole text.
    /// @return the root view, as a placed view.
    static PlacedView root(MDCView rootView) {
        // Same transformation as ViewDrawer.drawView for the root.
        return new PlacedView(rootView, new AffineTransform(rootView.getAffineTransform()), new int[0]);
    }

    MDCView view() {
        return view;
    }

    ModelElement model() {
        return view.getModel();
    }

    /// @return a copy of the path of child indices from the root view to this
    ///         view (which is also the path of its model in the text).
    int[] path() {
        return path.clone();
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
        Rectangle2D bounds = view.getSubViewBounds(i);
        AffineTransform subToRoot = new AffineTransform(toRoot);
        subToRoot.translate(bounds.getMinX(), bounds.getMinY());
        subToRoot.concatenate(sub.getAffineTransform());
        int[] subPath = Arrays.copyOf(path, path.length + 1);
        subPath[path.length] = i;
        return Optional.of(new PlacedView(sub, subToRoot, subPath));
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

    /// The area occupied by a subview, in the local system
    /// (see [MDCView#getSubViewBounds(int)]).
    Rectangle2D childBounds(int i) {
        return view.getSubViewBounds(i);
    }

    /// A point of the root system, in the local system.
    ///
    /// @param rootPoint a point, in the root system.
    /// @return the point in the local system, or empty if the transformation
    ///         can't be inverted.
    Optional<Point2D> toLocal(Point2D rootPoint) {
        try {
            return Optional.of(toRoot.inverseTransform(rootPoint, null));
        } catch (NoninvertibleTransformException e) {
            return Optional.empty();
        }
    }

    /// @param local a point of the local system.
    /// @return true if the point falls inside this view.
    boolean containsLocal(Point2D local) {
        return local.getX() >= 0 && local.getY() >= 0
                && local.getX() < view.getInternalWidth()
                && local.getY() < view.getInternalHeight();
    }

    /// A point of the local system, in the root system.
    Point2D toRoot(Point2D point) {
        return toRoot.transform(point, null);
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
}
