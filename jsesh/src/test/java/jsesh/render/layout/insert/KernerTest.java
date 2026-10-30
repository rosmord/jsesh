/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import jsesh.model.HBox;
import jsesh.sharedvocabulary.KerningAxis;
import jsesh.model.constants.TextDirection;
import jsesh.render.view.MDCView;

/// [Kerner] on boxes: views without a known outline count as their box.
public class KernerTest {

    private static final double EPSILON = 0.2;

    private static MDCView box(double x, double y, double width, double height) {
        MDCView v = new MDCView(new HBox());
        v.setWidth((float) width);
        v.setHeight((float) height);
        v.getPosition().setLocation(x, y);
        return v;
    }

    private static Kerner kerner(TextDirection direction) {
        return new Kerner(null, 1f, direction);
    }

    @ParameterizedTest
    @EnumSource(TextDirection.class)
    public void boxesAreKernedToTheSkip(TextDirection direction) {
        MDCView a = box(0, 0, 10, 10);
        MDCView b = box(20, 0, 10, 10);
        assertEquals(8, kerner(direction).shift(List.of(a), b, KerningAxis.HORIZONTAL, 2), EPSILON);
    }

    @Test
    public void groupsAlreadyCloseAreNotMovedApart() {
        MDCView a = box(0, 0, 10, 10);
        MDCView b = box(11, 0, 10, 10);
        assertEquals(0, kerner(TextDirection.LEFT_TO_RIGHT).shift(List.of(a), b, KerningAxis.HORIZONTAL, 2),
                1e-9);
    }

    @Test
    public void verticalKerning() {
        MDCView a = box(0, 0, 10, 4);
        MDCView b = box(0, 10, 10, 10);
        assertEquals(4, kerner(TextDirection.LEFT_TO_RIGHT).shift(List.of(a), b, KerningAxis.VERTICAL, 2),
                EPSILON);
    }

    @Test
    public void theFreeSpaceOfAnLShapeIsUsed() {
        // A tall bar and a low bar form an L; the second group slides over the low bar.
        MDCView tall = box(0, 0, 4, 20);
        MDCView low = box(4, 16, 16, 4);
        MDCView b = box(22, 0, 6, 10);
        // Limited by the tall bar: b may go to x = 4 + 2.
        assertEquals(16, kerner(TextDirection.LEFT_TO_RIGHT).shift(List.of(tall, low), b,
                KerningAxis.HORIZONTAL, 2), EPSILON);
    }

    @Test
    public void groupsWithoutCommonBandDoNotOvertake() {
        MDCView a = box(0, 0, 10, 5);
        MDCView b = box(20, 10, 10, 10);
        assertEquals(20, kerner(TextDirection.LEFT_TO_RIGHT).shift(List.of(a), b, KerningAxis.HORIZONTAL, 2),
                1e-9);
    }
}
