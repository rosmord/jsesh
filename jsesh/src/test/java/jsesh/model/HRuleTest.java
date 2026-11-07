/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/// The positions of a rule are percents of the width of the text.
public class HRuleTest {

    private static final double EPSILON = 1e-9;

    @Test
    public void positionsArePercents() {
        HRule r = new HRule('l', 10, 90);
        assertEquals(0.1, r.startFraction(), EPSILON);
        assertEquals(0.9, r.endFraction(), EPSILON);
    }

    @Test
    public void positionsAreClampedAndOrdered() {
        HRule r = new HRule('L', 500, -20);
        assertEquals(0, r.startFraction(), EPSILON);
        assertEquals(1, r.endFraction(), EPSILON);
        HRule reversed = new HRule('l', 90, 10);
        assertEquals(0.1, reversed.startFraction(), EPSILON);
        assertEquals(0.9, reversed.endFraction(), EPSILON);
    }
}
