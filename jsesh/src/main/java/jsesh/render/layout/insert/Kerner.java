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
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.Arrays;
import java.util.List;

import jsesh.sharedvocabulary.KerningAxis;
import jsesh.model.constants.TextDirection;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.view.MDCView;

/// Computes how much two groups can be kerned: how far the second one can be
/// moved back towards the first, along the kerning axis, so that their signs end
/// up about `skip` apart where they are closest.
///
/// The computation uses the actual outlines of the signs (see [ViewSilhouette]).
/// The cross axis is cut into [#BANDS] bands. In each band, the *profile* of the
/// first group is its furthest extent along the axis, and the profile of the second
/// group is its nearest one. Both are exact for the flattened outlines. The first
/// profile is spread over the bands closer than `skip`, so that the remaining space
/// is close to an actual distance between the shapes.
///
/// Views are given with their positions in the layout coordinates of a common
/// parent. In right-to-left text, layout coordinates are the mirror of drawing
/// coordinates; silhouettes are mirrored back accordingly.
///
/// Like [ShapeMask], the computation is deterministic and needs no graphics device.
public final class Kerner {

    /// Number of bands along the cross axis.
    static final int BANDS = 64;

    private final ViewSilhouette silhouette;
    private final boolean rightToLeft;

    /// @param hieroglyphDrawer used to find the shape of signs.
    /// @param baseSignScale the scale from the font to the style.
    /// @param textDirection the direction of the text.
    public Kerner(HieroglyphDrawer hieroglyphDrawer, float baseSignScale, TextDirection textDirection) {
        this.silhouette = new ViewSilhouette(hieroglyphDrawer, baseSignScale, textDirection);
        this.rightToLeft = TextDirection.RIGHT_TO_LEFT.equals(textDirection);
    }

    /// How far `after` can be moved back towards `before`.
    ///
    /// @param before the views of the first group, already placed.
    /// @param after the view of the second group, placed where it would be without kerning.
    /// @param axis the kerning axis.
    /// @param skip the space to leave between the signs.
    /// @return the distance to move `after` back, along the axis. It is never negative
    ///         (kerning never pushes groups apart), and never moves `after` before the
    ///         start of the first group.
    public double shift(List<MDCView> before, MDCView after, KerningAxis axis, double skip) {
        boolean horizontal = axis == KerningAxis.HORIZONTAL;
        double beforeStart = Double.POSITIVE_INFINITY;
        double crossMin = cross(after, horizontal);
        double crossMax = crossMin + crossLength(after, horizontal);
        for (MDCView v : before) {
            beforeStart = Math.min(beforeStart, main(v, horizontal));
            crossMin = Math.min(crossMin, cross(v, horizontal));
            crossMax = Math.max(crossMax, cross(v, horizontal) + crossLength(v, horizontal));
        }
        double maxShift = main(after, horizontal) - beforeStart;
        if (before.isEmpty() || maxShift <= 0 || crossMax <= crossMin) {
            return 0;
        }

        Profile first = new Profile(crossMin, crossMax, horizontal);
        for (MDCView v : before) {
            first.add(v);
        }
        Profile second = new Profile(crossMin, crossMax, horizontal);
        second.add(after);

        double[] firstMax = first.dilatedMax(skip);
        double gap = Double.POSITIVE_INFINITY;
        for (int b = 0; b < BANDS; b++) {
            if (second.min[b] != Double.POSITIVE_INFINITY && firstMax[b] != Double.NEGATIVE_INFINITY) {
                gap = Math.min(gap, second.min[b] - firstMax[b]);
            }
        }
        double shift = gap == Double.POSITIVE_INFINITY ? maxShift : gap - skip;
        return Math.max(0, Math.min(shift, maxShift));
    }

    private static double main(MDCView v, boolean horizontal) {
        return horizontal ? v.getPosition().x : v.getPosition().y;
    }

    private static double cross(MDCView v, boolean horizontal) {
        return horizontal ? v.getPosition().y : v.getPosition().x;
    }

    private static double crossLength(MDCView v, boolean horizontal) {
        return horizontal ? v.getHeight() : v.getWidth();
    }

    /// The extent of some outlines along the main axis, band by band.
    private final class Profile {
        private final double origin;
        private final double bandSize;
        private final boolean horizontal;
        /// Smallest and largest coordinate along the main axis, for each band.
        final double[] min = new double[BANDS];
        final double[] max = new double[BANDS];

        Profile(double crossMin, double crossMax, boolean horizontal) {
            this.origin = crossMin;
            this.bandSize = (crossMax - crossMin) / BANDS;
            this.horizontal = horizontal;
            Arrays.fill(min, Double.POSITIVE_INFINITY);
            Arrays.fill(max, Double.NEGATIVE_INFINITY);
        }

        /// Add the outlines of a view, at its position.
        void add(MDCView view) {
            AffineTransform transform = AffineTransform.getTranslateInstance(view.getPosition().x,
                    view.getPosition().y);
            if (rightToLeft) {
                // Back from drawing to layout coordinates, inside the view's box.
                transform.translate(view.getWidth(), 0);
                transform.scale(-1, 1);
            }
            for (Shape s : silhouette.silhouette(view)) {
                addShape(s, transform);
            }
        }

        private void addShape(Shape shape, AffineTransform transform) {
            PathIterator it = new FlatteningPathIterator(shape.getPathIterator(transform), bandSize / 4, 8);
            double[] seg = new double[6];
            double startX = 0, startY = 0, curX = 0, curY = 0;
            boolean open = false; // is there a current sub-path?
            while (!it.isDone()) {
                switch (it.currentSegment(seg)) {
                    case PathIterator.SEG_MOVETO:
                        if (open) {
                            addEdge(curX, curY, startX, startY);
                        }
                        open = true;
                        startX = curX = seg[0];
                        startY = curY = seg[1];
                        break;
                    case PathIterator.SEG_LINETO:
                        addEdge(curX, curY, seg[0], seg[1]);
                        curX = seg[0];
                        curY = seg[1];
                        break;
                    case PathIterator.SEG_CLOSE:
                        addEdge(curX, curY, startX, startY);
                        curX = startX;
                        curY = startY;
                        break;
                    default:
                        throw new IllegalStateException("flattened path expected");
                }
                it.next();
            }
            if (open) {
                addEdge(curX, curY, startX, startY);
            }
        }

        /// Update the bands crossed by an edge. The extremes of an edge clipped to a
        /// band are at the ends of the clipped part.
        private void addEdge(double x0, double y0, double x1, double y1) {
            double u0 = horizontal ? x0 : y0, v0 = horizontal ? y0 : x0;
            double u1 = horizontal ? x1 : y1, v1 = horizontal ? y1 : x1;
            double low = Math.min(v0, v1), high = Math.max(v0, v1);
            int first = Math.max(0, (int) Math.floor((low - origin) / bandSize));
            int last = Math.min(BANDS - 1, (int) Math.floor((high - origin) / bandSize));
            for (int b = first; b <= last; b++) {
                double bandLow = Math.max(low, origin + b * bandSize);
                double bandHigh = Math.min(high, origin + (b + 1) * bandSize);
                if (bandLow > bandHigh) {
                    continue;
                }
                double a, c;
                if (v0 == v1) {
                    a = u0;
                    c = u1;
                } else {
                    a = u0 + (bandLow - v0) * (u1 - u0) / (v1 - v0);
                    c = u0 + (bandHigh - v0) * (u1 - u0) / (v1 - v0);
                }
                min[b] = Math.min(min[b], Math.min(a, c));
                max[b] = Math.max(max[b], Math.max(a, c));
            }
        }

        /// The largest extent, spread over the bands closer than `distance`.
        double[] dilatedMax(double distance) {
            int k = (int) Math.ceil(distance / bandSize - 1e-9);
            double[] result = new double[BANDS];
            for (int b = 0; b < BANDS; b++) {
                double m = Double.NEGATIVE_INFINITY;
                for (int j = Math.max(0, b - k); j <= Math.min(BANDS - 1, b + k); j++) {
                    m = Math.max(m, max[j]);
                }
                result[b] = m;
            }
            return result;
        }
    }
}
