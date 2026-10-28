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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// Computes where, and how large, an element can be inserted in the free space
/// of another one, using the actual shapes of both.
///
/// This is the geometric core of the `%insert` operator. It is deliberately
/// independent from the model and from the view classes: it works on shapes and
/// boxes, in the geometric (Java2D, y axis downwards) coordinates of the layout.
/// "Left" and "right" are geometric: the caller translates "start" and "end"
/// according to the text direction.
///
/// ## Algorithm
///
/// The *frame* is the target's box, possibly enlarged on the side of the
/// insertion (see [#enlargedFrame]). The target is rasterised in the frame (see
/// [ShapeMask]) and dilated by a small margin. Then:
///
/// - scales are tried from largest to smallest: `1, 2^(-1/4), 2^(-2/4), ... 1/4`
///   (1 being the natural size of the inserted element);
/// - for each scale, candidate positions are tried, nearest to the insertion
///   corner or edge first:
///   - oblique insertion: a regular 5x5 grid in the square at the corner, whose
///     side is 1/4 of the frame's;
///   - insertion from a side: 10 points on the central line, from the edge
///     to the center of the frame;
///   - center insertion: the center of the frame;
/// - the inserted element is anchored on the candidate position by the corner (or
///   side midpoint) of its box which faces the insertion corner (or side).
/// - the first placement where the inserted element is inside the frame and does
///   not touch the target wins.
///
/// If nothing fits, the smallest scale is used at the position with the least overlap.
public final class InsertionFitter {

    /// A horizontal side of the frame.
    public enum HorizontalSide {
        LEFT, CENTER, RIGHT
    }

    /// A vertical side of the frame. As layout coordinates are y-down, `TOP` is the side with the smallest y.
    public enum VerticalSide {
        TOP, CENTER, BOTTOM
    }

    /// What to fit.
    ///
    /// The shapes are given as lists of outlines, each filled according to its own
    /// winding rule. They must **not** be merged into a single area: that would fill
    /// the holes of signs, which are precisely where an element can be inserted.
    ///
    /// @param target the outlines of the main element.
    /// @param frame the area where the inserted element must go. Usually, the target's box, see [#enlargedFrame].
    /// @param inserted the outlines of the inserted element, at its natural size.
    /// @param insertedBox the box of the inserted element, at its natural size.
    /// @param horizontal on which side the element is inserted.
    /// @param vertical on which side the element is inserted.
    public record Request(List<? extends Shape> target, Rectangle2D frame, List<? extends Shape> inserted,
            Rectangle2D insertedBox, HorizontalSide horizontal, VerticalSide vertical) {
    }

    /// How to transform the inserted element.
    ///
    /// @param scale the (uniform) scaling factor, relative to the natural size.
    /// @param translateX x translation, applied after scaling.
    /// @param translateY y translation, applied after scaling.
    /// @param fits false if nothing fitted, and the placement is a best effort.
    public record Placement(double scale, double translateX, double translateY, boolean fits) {

        /// The transformation to apply to the inserted element's coordinates.
        public AffineTransform transform() {
            AffineTransform t = new AffineTransform();
            t.translate(translateX, translateY);
            t.scale(scale, scale);
            return t;
        }
    }

    /// Number of scales tried: 1, 2^(-1/4), ..., 2^(-8/4) = 1/4.
    static final int SCALE_STEPS = 9;

    /// Number of points on the center line for insertions from a side.
    static final int SIDE_POINTS = 10;

    /// Number of grid points per side for oblique insertions.
    static final int GRID_POINTS = 5;

    /// Side of the corner square used for oblique insertions, as a fraction of the frame.
    static final double CORNER_SQUARE = 0.25;

    private static final double EPSILON = 1e-9;

    private final int resolution;
    private final int marginCells;

    /// Default fitter: 64 cells resolution, one cell of margin.
    public InsertionFitter() {
        this(ShapeMask.MAX_RESOLUTION, 1);
    }

    /// @param resolution number of cells along the longest side of the frame.
    /// @param marginCells minimum distance between the shapes, in cells.
    public InsertionFitter(int resolution, int marginCells) {
        this.resolution = resolution;
        this.marginCells = marginCells;
    }

    /// The scale tried at step `k`.
    static double scaleAt(int k) {
        return Math.pow(2.0, -k / 4.0);
    }

    /// Enlarge the target's box on the side of the insertion.
    ///
    /// The frame grows by `dw` percent of the inserted element's natural width
    /// on the horizontal side of the insertion, and by `dh` percent of its natural
    /// height on the vertical side. On the center line (no horizontal side, or no
    /// vertical side), the corresponding enlargement only applies if there is no
    /// side at all (center insertion), in which case it is split evenly between
    /// both sides. The target itself is not moved in the frame.
    ///
    /// @param targetBox the target's box.
    /// @param insertedBox the inserted element's natural box.
    /// @param horizontal horizontal side of the insertion.
    /// @param vertical vertical side of the insertion.
    /// @param dw width enlargement, in percent of the inserted element's width.
    /// @param dh height enlargement, in percent of the inserted element's height.
    public static Rectangle2D enlargedFrame(Rectangle2D targetBox, Rectangle2D insertedBox,
            HorizontalSide horizontal, VerticalSide vertical, int dw, int dh) {
        boolean center = horizontal == HorizontalSide.CENTER && vertical == VerticalSide.CENTER;
        double w = insertedBox.getWidth() * dw / 100.0;
        double h = insertedBox.getHeight() * dh / 100.0;
        double minX = targetBox.getMinX(), maxX = targetBox.getMaxX();
        double minY = targetBox.getMinY(), maxY = targetBox.getMaxY();
        if (center) {
            minX -= w / 2;
            maxX += w / 2;
            minY -= h / 2;
            maxY += h / 2;
        } else {
            if (horizontal == HorizontalSide.LEFT) {
                minX -= w;
            } else if (horizontal == HorizontalSide.RIGHT) {
                maxX += w;
            }
            if (vertical == VerticalSide.TOP) {
                minY -= h;
            } else if (vertical == VerticalSide.BOTTOM) {
                maxY += h;
            }
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /// Find the placement of the inserted element.
    public Placement fit(Request request) {
        Rectangle2D frame = request.frame();
        Rectangle2D box = request.insertedBox();
        Point2D anchor = anchorOf(box, request.horizontal(), request.vertical());
        List<Point2D> candidates = candidates(frame, request.horizontal(), request.vertical());

        if (frame.getWidth() <= 0 || frame.getHeight() <= 0 || box.getWidth() <= 0 || box.getHeight() <= 0) {
            return placement(1.0, candidates.get(0), anchor, false);
        }

        ShapeMask target = new ShapeMask(frame, resolution);
        for (Shape s : request.target()) {
            target.fill(s, null);
        }
        ShapeMask forbidden = target.dilate(marginCells);
        ShapeMask trial = new ShapeMask(forbidden);

        double bestScale = scaleAt(SCALE_STEPS - 1);
        Point2D bestPosition = candidates.get(0);
        int bestOverlap = Integer.MAX_VALUE;

        for (int k = 0; k < SCALE_STEPS; k++) {
            double scale = scaleAt(k);
            boolean lastScale = k == SCALE_STEPS - 1;
            for (Point2D position : candidates) {
                Placement p = placement(scale, position, anchor, true);
                boolean inside = isInside(frame, box, p);
                if (inside || lastScale) {
                    trial.clear();
                    AffineTransform t = p.transform();
                    for (Shape s : request.inserted()) {
                        trial.fill(s, t);
                    }
                    int overlap = forbidden.overlap(trial);
                    if (overlap == 0 && inside) {
                        return p;
                    }
                    if (lastScale && overlap < bestOverlap) {
                        bestOverlap = overlap;
                        bestScale = scale;
                        bestPosition = position;
                    }
                }
            }
        }
        return placement(bestScale, bestPosition, anchor, false);
    }

    private static Placement placement(double scale, Point2D position, Point2D anchor, boolean fits) {
        return new Placement(scale, position.getX() - scale * anchor.getX(), position.getY() - scale * anchor.getY(),
                fits);
    }

    private static boolean isInside(Rectangle2D frame, Rectangle2D box, Placement p) {
        Rectangle2D moved = p.transform().createTransformedShape(box).getBounds2D();
        double eps = EPSILON * Math.max(frame.getWidth(), frame.getHeight());
        return moved.getMinX() >= frame.getMinX() - eps && moved.getMaxX() <= frame.getMaxX() + eps
                && moved.getMinY() >= frame.getMinY() - eps && moved.getMaxY() <= frame.getMaxY() + eps;
    }

    /// The point of a box used to attach it to the candidate position.
    private static Point2D anchorOf(Rectangle2D box, HorizontalSide h, VerticalSide v) {
        double x = switch (h) {
            case LEFT -> box.getMinX();
            case RIGHT -> box.getMaxX();
            case CENTER -> box.getCenterX();
        };
        double y = switch (v) {
            case TOP -> box.getMinY();
            case BOTTOM -> box.getMaxY();
            case CENTER -> box.getCenterY();
        };
        return new Point2D.Double(x, y);
    }

    /// Candidate positions in the frame, best first.
    static List<Point2D> candidates(Rectangle2D frame, HorizontalSide h, VerticalSide v) {
        List<Point2D> result = new ArrayList<>();
        double w = frame.getWidth(), ht = frame.getHeight();
        double sx = h == HorizontalSide.LEFT ? 1 : -1;
        double sy = v == VerticalSide.TOP ? 1 : -1;
        double cornerX = h == HorizontalSide.LEFT ? frame.getMinX() : frame.getMaxX();
        double cornerY = v == VerticalSide.TOP ? frame.getMinY() : frame.getMaxY();

        if (h == HorizontalSide.CENTER && v == VerticalSide.CENTER) {
            result.add(new Point2D.Double(frame.getCenterX(), frame.getCenterY()));
        } else if (h != HorizontalSide.CENTER && v != VerticalSide.CENTER) {
            // Oblique: grid points (i,j), nearest to the corner first.
            List<int[]> grid = new ArrayList<>();
            for (int j = 0; j < GRID_POINTS; j++) {
                for (int i = 0; i < GRID_POINTS; i++) {
                    grid.add(new int[] { i, j });
                }
            }
            // Distance to the corner, then smaller horizontal offset first.
            grid.sort(Comparator.<int[]>comparingInt(p -> p[0] * p[0] + p[1] * p[1]).thenComparingInt(p -> p[0]));
            double stepX = CORNER_SQUARE * w / (GRID_POINTS - 1);
            double stepY = CORNER_SQUARE * ht / (GRID_POINTS - 1);
            for (int[] p : grid) {
                result.add(new Point2D.Double(cornerX + sx * p[0] * stepX, cornerY + sy * p[1] * stepY));
            }
        } else if (v == VerticalSide.CENTER) {
            // From the left or right edge to the center.
            for (int k = 0; k < SIDE_POINTS; k++) {
                double t = 0.5 * k / (SIDE_POINTS - 1);
                result.add(new Point2D.Double(cornerX + sx * t * w, frame.getCenterY()));
            }
        } else {
            // From the top or bottom edge to the center.
            for (int k = 0; k < SIDE_POINTS; k++) {
                double t = 0.5 * k / (SIDE_POINTS - 1);
                result.add(new Point2D.Double(frame.getCenterX(), cornerY + sy * t * ht));
            }
        }
        return result;
    }
}
