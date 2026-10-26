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

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.Test;

/// Tests for [ShapeMask].
public class ShapeMaskTest {

    private static final Rectangle2D FRAME = new Rectangle2D.Double(0, 0, 64, 64);

    @Test
    public void rectangleFillsExpectedCells() {
        ShapeMask mask = new ShapeMask(FRAME, 64);
        mask.fill(new Rectangle2D.Double(10, 20, 5, 3), null);
        assertTrue(mask.get(10, 20));
        assertTrue(mask.get(14, 22));
        assertFalse(mask.get(9, 20));
        assertFalse(mask.get(10, 19));
        assertFalse(mask.get(10, 23));
    }

    @Test
    public void transformIsApplied() {
        ShapeMask mask = new ShapeMask(FRAME, 64);
        mask.fill(new Rectangle2D.Double(0, 0, 4, 4), AffineTransform.getTranslateInstance(30, 40));
        assertTrue(mask.get(31, 41));
        assertFalse(mask.get(1, 1));
    }

    @Test
    public void holesAreRespected() {
        Area ring = new Area(new Ellipse2D.Double(0, 0, 60, 60));
        ring.subtract(new Area(new Ellipse2D.Double(15, 15, 30, 30)));
        ShapeMask mask = new ShapeMask(FRAME, 64);
        mask.fill(ring, null);
        assertFalse(mask.get(30, 30));
        assertTrue(mask.get(30, 5));
    }

    @Test
    public void shapesOutsideTheFrameAreIgnored() {
        ShapeMask mask = new ShapeMask(FRAME, 64);
        mask.fill(new Rectangle2D.Double(-100, -100, 50, 50), null);
        assertTrue(mask.isEmpty());
        mask.fill(new Rectangle2D.Double(-100, -100, 200, 200), null);
        assertTrue(mask.get(0, 0));
        assertTrue(mask.get(63, 63));
    }

    @Test
    public void thinStrokesAreNotLost() {
        ShapeMask mask = new ShapeMask(FRAME, 64);
        // 0.3 of a cell high, not aligned with the grid
        mask.fill(new Rectangle2D.Double(10, 10.4, 30, 0.3), null);
        assertFalse(mask.isEmpty());
    }

    @Test
    public void dilationGrowsByOneCellInEachDirection() {
        ShapeMask mask = new ShapeMask(FRAME, 64);
        mask.fill(new Rectangle2D.Double(10, 10, 1, 1), null);
        ShapeMask dilated = mask.dilate(1);
        assertTrue(dilated.get(9, 10));
        assertTrue(dilated.get(11, 10));
        assertTrue(dilated.get(10, 9));
        assertTrue(dilated.get(10, 11));
        assertFalse(dilated.get(12, 10));
        assertFalse(dilated.get(11, 11), "dilation is 4-connected");
    }

    @Test
    public void overlapAndIntersection() {
        ShapeMask a = new ShapeMask(FRAME, 64);
        a.fill(new Rectangle2D.Double(0, 0, 10, 10), null);
        ShapeMask b = new ShapeMask(a);
        b.fill(new Rectangle2D.Double(8, 8, 10, 10), null);
        assertTrue(a.intersects(b));
        assertEquals(4, a.overlap(b));
        b.clear();
        b.fill(new Rectangle2D.Double(20, 20, 5, 5), null);
        assertFalse(a.intersects(b));
    }
}
