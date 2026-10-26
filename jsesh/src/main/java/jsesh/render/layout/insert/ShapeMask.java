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
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/// A coarse black-and-white raster of a shape, over a fixed rectangular frame.
///
/// Masks are used by [InsertionFitter] to test whether two shapes collide. They
/// are built by an own scanline filler rather than by Java2D, so that the result
/// is deterministic, needs no graphics device, and can be reproduced exactly by
/// the JavaScript port.
///
/// The grid has at most [#MAX_RESOLUTION] columns, so each row fits in a single
/// `long` (bit `c` is column `c`) and overlap tests are bitwise. Cells are square.
///
/// Filling is slightly **conservative**: each row is sampled on four horizontal
/// lines, and any cell touched horizontally by a span found on one of them is set.
/// A shape covering more than a quarter of a row's height is therefore never lost.
final class ShapeMask {

    /// Maximum number of columns (and rows) along the longest side of the frame.
    static final int MAX_RESOLUTION = 64;

    /// Number of sample lines per row.
    private static final int SAMPLES_PER_ROW = 4;

    private final double originX;
    private final double originY;
    private final double cellSize;
    private final int columns;
    private final int rowCount;
    private final long[] rows;

    /// Create an empty mask.
    ///
    /// @param frame the part of the plane covered by the mask. Must not be empty.
    /// @param resolution number of cells along the longest side of the frame (at most [#MAX_RESOLUTION]).
    ShapeMask(Rectangle2D frame, int resolution) {
        if (resolution < 1 || resolution > MAX_RESOLUTION) {
            throw new IllegalArgumentException("resolution must be in 1.." + MAX_RESOLUTION);
        }
        if (frame.getWidth() <= 0 || frame.getHeight() <= 0) {
            throw new IllegalArgumentException("empty frame");
        }
        this.originX = frame.getMinX();
        this.originY = frame.getMinY();
        this.cellSize = Math.max(frame.getWidth(), frame.getHeight()) / resolution;
        this.columns = Math.max(1, Math.min(resolution, (int) Math.ceil(frame.getWidth() / cellSize - 1e-9)));
        this.rowCount = Math.max(1, Math.min(resolution, (int) Math.ceil(frame.getHeight() / cellSize - 1e-9)));
        this.rows = new long[rowCount];
    }

    /// An empty mask over the same grid as another one.
    ShapeMask(ShapeMask model) {
        this.originX = model.originX;
        this.originY = model.originY;
        this.cellSize = model.cellSize;
        this.columns = model.columns;
        this.rowCount = model.rowCount;
        this.rows = new long[rowCount];
    }

    double cellSize() {
        return cellSize;
    }

    /// Remove every cell of the mask.
    void clear() {
        Arrays.fill(rows, 0L);
    }

    boolean isEmpty() {
        for (long row : rows) {
            if (row != 0) {
                return false;
            }
        }
        return true;
    }

    /// Set the cells touched by a shape (cells outside the grid are ignored).
    ///
    /// @param shape the shape to add.
    /// @param transform applied to the shape before filling. May be `null`.
    void fill(Shape shape, AffineTransform transform) {
        PathIterator it = new FlatteningPathIterator(shape.getPathIterator(transform), cellSize / 4, 8);
        boolean evenOdd = it.getWindingRule() == PathIterator.WIND_EVEN_ODD;

        // Edges of the flattened outline, as x0, y0, x1, y1 quadruples.
        double[] edges = new double[64];
        int edgeCount = 0;
        double[] seg = new double[6];
        double startX = 0, startY = 0, curX = 0, curY = 0;
        while (!it.isDone()) {
            int type = it.currentSegment(seg);
            double nextX = curX, nextY = curY;
            boolean edge = false;
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    // close previous subpath implicitly
                    if (curX != startX || curY != startY) {
                        edges = ensure(edges, edgeCount);
                        put(edges, edgeCount++, curX, curY, startX, startY);
                    }
                    startX = nextX = seg[0];
                    startY = nextY = seg[1];
                    curX = startX;
                    curY = startY;
                    break;
                case PathIterator.SEG_LINETO:
                    nextX = seg[0];
                    nextY = seg[1];
                    edge = true;
                    break;
                case PathIterator.SEG_CLOSE:
                    nextX = startX;
                    nextY = startY;
                    edge = true;
                    break;
                default:
                    throw new IllegalStateException("flattened path expected");
            }
            if (edge) {
                edges = ensure(edges, edgeCount);
                put(edges, edgeCount++, curX, curY, nextX, nextY);
                curX = nextX;
                curY = nextY;
            }
            it.next();
        }
        if (curX != startX || curY != startY) {
            edges = ensure(edges, edgeCount);
            put(edges, edgeCount++, curX, curY, startX, startY);
        }
        if (edgeCount == 0) {
            return;
        }

        double[] xs = new double[edgeCount];
        int[] dirs = new int[edgeCount];
        for (int row = 0; row < rowCount; row++) {
            for (int sample = 0; sample < SAMPLES_PER_ROW; sample++) {
                double y = originY + (row + (sample + 0.5) / SAMPLES_PER_ROW) * cellSize;
                int n = crossings(edges, edgeCount, y, xs, dirs);
                fillSpans(row, xs, dirs, n, evenOdd);
            }
        }
    }

    /// Grow the mask by a number of cells in the four directions.
    ShapeMask dilate(int cells) {
        ShapeMask result = new ShapeMask(this);
        long all = columns == 64 ? -1L : (1L << columns) - 1;
        System.arraycopy(rows, 0, result.rows, 0, rowCount);
        for (int k = 0; k < cells; k++) {
            long[] src = result.rows.clone();
            for (int r = 0; r < rowCount; r++) {
                long v = src[r] | (src[r] << 1) | (src[r] >>> 1);
                if (r > 0) {
                    v |= src[r - 1];
                }
                if (r < rowCount - 1) {
                    v |= src[r + 1];
                }
                result.rows[r] = v & all;
            }
        }
        return result;
    }

    /// Do the two masks have a common cell? They must share the same grid.
    boolean intersects(ShapeMask other) {
        for (int r = 0; r < rowCount; r++) {
            if ((rows[r] & other.rows[r]) != 0) {
                return true;
            }
        }
        return false;
    }

    /// The number of cells set in both masks.
    int overlap(ShapeMask other) {
        int count = 0;
        for (int r = 0; r < rowCount; r++) {
            count += Long.bitCount(rows[r] & other.rows[r]);
        }
        return count;
    }

    /// Is the cell set? (for tests)
    boolean get(int column, int row) {
        return (rows[row] & (1L << column)) != 0;
    }

    int columns() {
        return columns;
    }

    int rowCount() {
        return rowCount;
    }

    private static double[] ensure(double[] edges, int edgeCount) {
        return (edgeCount + 1) * 4 > edges.length ? Arrays.copyOf(edges, edges.length * 2) : edges;
    }

    private static void put(double[] edges, int index, double x0, double y0, double x1, double y1) {
        int i = index * 4;
        edges[i] = x0;
        edges[i + 1] = y0;
        edges[i + 2] = x1;
        edges[i + 3] = y1;
    }

    /// Compute the sorted crossings of the outline with the horizontal line at `y`.
    /// Edges are half-open on their upper end, so that a vertex is counted once.
    private static int crossings(double[] edges, int edgeCount, double y, double[] xs, int[] dirs) {
        int n = 0;
        for (int e = 0; e < edgeCount; e++) {
            double x0 = edges[4 * e], y0 = edges[4 * e + 1], x1 = edges[4 * e + 2], y1 = edges[4 * e + 3];
            if (y0 == y1) {
                continue;
            }
            int dir = y1 > y0 ? 1 : -1;
            double low = Math.min(y0, y1), high = Math.max(y0, y1);
            if (y < low || y >= high) {
                continue;
            }
            double x = x0 + (y - y0) * (x1 - x0) / (y1 - y0);
            // insertion sort by x
            int i = n++;
            while (i > 0 && xs[i - 1] > x) {
                xs[i] = xs[i - 1];
                dirs[i] = dirs[i - 1];
                i--;
            }
            xs[i] = x;
            dirs[i] = dir;
        }
        return n;
    }

    private void fillSpans(int row, double[] xs, int[] dirs, int n, boolean evenOdd) {
        int winding = 0;
        for (int i = 0; i < n - 1; i++) {
            winding = evenOdd ? (winding == 0 ? 1 : 0) : winding + dirs[i];
            if (winding != 0) {
                setSpan(row, xs[i], xs[i + 1]);
            }
        }
    }

    private void setSpan(int row, double x0, double x1) {
        double u0 = (x0 - originX) / cellSize;
        double u1 = (x1 - originX) / cellSize;
        int c0 = (int) Math.floor(u0);
        // A span ending exactly on a cell boundary doesn't touch the next cell.
        int c1 = Math.max(c0, (int) Math.ceil(u1) - 1);
        if (c1 < 0 || c0 >= columns) {
            return;
        }
        c0 = Math.max(c0, 0);
        c1 = Math.min(c1, columns - 1);
        long high = c1 == 63 ? -1L : (1L << (c1 + 1)) - 1;
        long low = (1L << c0) - 1;
        rows[row] |= high & ~low;
    }
}
