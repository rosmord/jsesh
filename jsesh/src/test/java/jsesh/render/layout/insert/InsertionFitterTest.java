/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.render.layout.insert.InsertionFitter.HorizontalSide;
import jsesh.render.layout.insert.InsertionFitter.Placement;
import jsesh.render.layout.insert.InsertionFitter.Request;
import jsesh.render.layout.insert.InsertionFitter.VerticalSide;

/// Tests for [InsertionFitter], on synthetic shapes in a 20x20 frame.
public class InsertionFitterTest {

    private static final Rectangle2D FRAME = new Rectangle2D.Double(0, 0, 20, 20);

    private final InsertionFitter fitter = new InsertionFitter();

    private static Rectangle2D square(double x, double y, double w, double h) {
        return new Rectangle2D.Double(x, y, w, h);
    }

    private static Request request(Shape target, Rectangle2D inserted, HorizontalSide h, VerticalSide v) {
        return new Request(List.of(target), FRAME, List.of(inserted), inserted, h, v);
    }

    @Test
    public void emptyTargetKeepsNaturalSizeAtTheEdge() {
        Request r = request(new Path2D.Double(), square(0, 0, 10, 10), HorizontalSide.LEFT, VerticalSide.CENTER);
        Placement p = fitter.fit(r);
        assertTrue(p.fits());
        assertEquals(1.0, p.scale(), 1e-9);
        // left-middle of the box (0,5) goes on the left-middle of the frame (0,10)
        assertEquals(0.0, p.translateX(), 1e-9);
        assertEquals(5.0, p.translateY(), 1e-9);
    }

    @Test
    public void tooLargeElementIsScaledToTheFrame() {
        Request r = request(new Path2D.Double(), square(0, 0, 30, 30), HorizontalSide.CENTER, VerticalSide.CENTER);
        Placement p = fitter.fit(r);
        assertTrue(p.fits());
        // 30 * 2^(-3/4) = 17.8 fits in 20, and 30 * 2^(-2/4) = 21.2 does not.
        assertEquals(Math.pow(2, -0.75), p.scale(), 1e-9);
    }

    @Test
    public void sideInsertionShrinksUntilItClearsTheTarget() {
        // The target fills the right of the frame, from x=8.
        Shape target = square(8, 0, 12, 20);
        Placement p = fitter.fit(request(target, square(0, 0, 10, 10), HorizontalSide.LEFT, VerticalSide.CENTER));
        assertTrue(p.fits());
        assertEquals(Math.pow(2, -0.5), p.scale(), 1e-9);
        assertEquals(0.0, p.translateX(), 1e-9);
    }

    @Test
    public void sideInsertionDoesNotPassThroughTheTarget() {
        // A bar across the frame, from y=5 to y=8; free space above and (larger) below.
        Shape target = square(0, 5, 20, 3);
        Placement p = fitter.fit(request(target, square(0, 0, 6, 6), HorizontalSide.CENTER, VerticalSide.TOP));
        assertTrue(p.fits());
        // Shrunk to fit above the bar, not placed at full size below it.
        double s = Math.pow(2, -0.5);
        assertEquals(s, p.scale(), 1e-9);
        assertEquals(0.0, p.translateY(), 1e-9);
    }

    @Test
    public void obliqueInsertionUsesTheCorner() {
        // Free space: an 8x8 hole in the bottom-left corner.
        Area target = new Area(square(0, 0, 20, 20));
        target.subtract(new Area(square(0, 12, 8, 8)));
        Placement p = fitter.fit(request(target, square(0, 0, 10, 10), HorizontalSide.LEFT, VerticalSide.BOTTOM));
        assertTrue(p.fits());
        double s = Math.pow(2, -0.5);
        assertEquals(s, p.scale(), 1e-9);
        // bottom-left of the box (0,10) goes on the bottom-left of the frame (0,20)
        assertEquals(0.0, p.translateX(), 1e-9);
        assertEquals(20 - 10 * s, p.translateY(), 1e-9);
    }

    @Test
    public void obliqueInsertionMovesAwayFromABlockedCorner() {
        // 10x10 hole in the bottom-left corner, with a small obstacle in the very corner.
        Area target = new Area(square(0, 0, 20, 20));
        target.subtract(new Area(square(0, 10, 10, 10)));
        target.add(new Area(square(0, 19, 1, 1)));
        Placement p = fitter.fit(request(target, square(0, 0, 6, 6), HorizontalSide.LEFT, VerticalSide.BOTTOM));
        assertTrue(p.fits());
        assertEquals(1.0, p.scale(), 1e-9);
        // The corner, and the points straight above it and straight to its right, touch the obstacle.
        // The first free grid point is (1,1): one 20/16 step to the right and up. It clears the obstacle
        // diagonally, which is allowed.
        assertEquals(1.25, p.translateX(), 1e-9);
        assertEquals(20 - 1.25 - 6, p.translateY(), 1e-9);
    }

    @Test
    public void enlargingTheFrameLetsTheElementGrow() {
        // The target takes the whole frame, so the element can't be inserted at its natural size
        // in the frame.
        Shape target = square(0, 0, 20, 20);
        Rectangle2D inserted = square(0, 0, 10, 10);
        // 120%: the element's width, plus room for the safety margin around the target.
        Rectangle2D frame = InsertionFitter.enlargedFrame(FRAME, inserted, HorizontalSide.RIGHT, VerticalSide.CENTER,
                120, 0);
        assertEquals(new Rectangle2D.Double(0, 0, 32, 20), frame);
        Placement p = fitter.fit(new Request(List.of(target), frame, List.of(inserted), inserted,
                HorizontalSide.RIGHT, VerticalSide.CENTER));
        assertTrue(p.fits());
        assertEquals(1.0, p.scale(), 1e-9);
        // right-middle (10,5) goes on (32,10)
        assertEquals(22.0, p.translateX(), 1e-9);
    }

    @Test
    public void nothingFitsIsReported() {
        Placement p = fitter.fit(request(square(0, 0, 20, 20), square(0, 0, 10, 10), HorizontalSide.LEFT,
                VerticalSide.CENTER));
        assertFalse(p.fits());
        assertEquals(0.25, p.scale(), 1e-9);
    }

    @Test
    public void enlargedFrameInObliqueAndCenterCases() {
        Rectangle2D inserted = square(0, 0, 10, 8);
        assertEquals(new Rectangle2D.Double(-5, 0, 25, 24),
                InsertionFitter.enlargedFrame(FRAME, inserted, HorizontalSide.LEFT, VerticalSide.BOTTOM, 50, 50));
        assertEquals(new Rectangle2D.Double(-2.5, -2, 25, 24),
                InsertionFitter.enlargedFrame(FRAME, inserted, HorizontalSide.CENTER, VerticalSide.CENTER, 50, 50));
        // dh is ignored when inserting from the side
        assertEquals(new Rectangle2D.Double(0, 0, 25, 20),
                InsertionFitter.enlargedFrame(FRAME, inserted, HorizontalSide.RIGHT, VerticalSide.CENTER, 50, 50));
    }
}
