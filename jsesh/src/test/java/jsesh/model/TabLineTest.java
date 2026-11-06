/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;

/// Stop indexes of dynamic tabs, and moves of a tab to another stop.
class TabLineTest {

    private static List<TopItem> line(String mdc) throws MDCSyntaxError {
        HieroglyphicText text = new MDCParserModelGenerator().parse(mdc);
        List<TopItem> items = new ArrayList<>();
        for (int i = 0; i < text.getNumberOfChildren(); i++) {
            items.add(text.getTopItemAt(i));
        }
        return items;
    }

    private static String mdc(List<TopItem> items) {
        HieroglyphicText text = new HieroglyphicText();
        text.insertItemsAt(0, items.stream().map(i -> (TopItem) i.deepCopy()).toList());
        return new MdCModelWriter().toMdC(text).replaceAll("\\s", "");
    }

    /// Moves the tab at `position` and writes the line back.
    private static String moved(String mdc, int position, int delta) throws MDCSyntaxError {
        Optional<List<TopItem>> result = TabLine.moveTab(line(mdc), position, delta);
        return result.map(TabLineTest::mdc).orElse("impossible");
    }

    @Test
    void stopIndexesFollowTheRules() throws MDCSyntaxError {
        // A1 %tab B1 %tab[n=4] C1 %tab D1 %tab[n=2] E1
        int[] indexes = TabLine.stopIndexes(line("A1-%tab-B1-%tab[n=4]-C1-%tab-D1-%tab[n=2]-E1"));
        assertArrayEquals(new int[] { 0, 1, 0, 4, 0, 5, 0, 6, 0 }, indexes);
    }

    @Test
    void stopIndexOfATabInAText() throws MDCSyntaxError {
        HieroglyphicText text = new MDCParserModelGenerator().parse("A1-%tab-B1-%tab[n=3]-C1-!-%tab-D1");
        assertEquals(1, TabLine.stopIndexOf((Tab) text.getTopItemAt(1)));
        assertEquals(3, TabLine.stopIndexOf((Tab) text.getTopItemAt(3)));
        // A new line starts again from 0.
        assertEquals(1, TabLine.stopIndexOf((Tab) text.getTopItemAt(6)));
    }

    @Test
    void movingForwardMakesTheIndexExplicit() throws MDCSyntaxError {
        assertEquals("A1-%tab[n=2]-B1", moved("A1-%tab-B1", 1, 1));
    }

    @Test
    void movingForwardPushesTheFollowingTabsOnlyIfNeeded() throws MDCSyntaxError {
        // The automatic tab follows; the explicit one at 2 must move to 3.
        assertEquals("A1-%tab[n=2]-B1-%tab-C1", moved("A1-%tab-B1-%tab-C1", 1, 1));
        assertEquals("A1-%tab[n=2]-B1-%tab[n=3]-C1-%tab-D1", moved("A1-%tab-B1-%tab[n=2]-C1-%tab-D1", 1, 1));
        // A tab further away doesn't move.
        assertEquals("A1-%tab[n=2]-B1-%tab[n=5]-C1", moved("A1-%tab-B1-%tab[n=5]-C1", 1, 1));
    }

    @Test
    void movingBackKeepsTheFollowingTabsInPlace() throws MDCSyntaxError {
        assertEquals("A1-%tab-B1-%tab[n=3]-C1", moved("A1-%tab[n=2]-B1-%tab-C1", 1, -1));
        assertEquals("A1-%tab-B1-%tab[n=5]-C1", moved("A1-%tab[n=2]-B1-%tab[n=5]-C1", 1, -1));
    }

    @Test
    void aTabCannotMoveBeforeTheNextStopOfThePreviousTab() throws MDCSyntaxError {
        assertEquals("impossible", moved("A1-%tab-B1", 1, -1));
        assertEquals("impossible", moved("A1-%tab-B1-%tab-C1", 3, -1));
        assertEquals("A1-%tab-B1-%tab-C1", moved("A1-%tab-B1-%tab[n=3]-C1", 3, -1));
    }

    @Test
    void unchangedItemsAreKept() throws MDCSyntaxError {
        List<TopItem> items = line("A1-%tab-B1-%tab[n=5]-C1");
        List<TopItem> result = TabLine.moveTab(items, 1, 1).orElseThrow();
        assertSame(items.get(0), result.get(0));
        assertSame(items.get(3), result.get(3));
        assertTrue(result.get(1) != items.get(1));
    }
}
