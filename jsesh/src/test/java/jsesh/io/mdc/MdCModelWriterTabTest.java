/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.AbsoluteTab;
import jsesh.model.HieroglyphicText;
import jsesh.model.Tab;
import jsesh.model.Zone;
import jsesh.model.ZoneParameters;
import jsesh.parser.MDCSyntaxError;
import jsesh.sharedvocabulary.LineAlignment;

/// Dynamic tabs (`%tab[n=...,align=...]`) and the `tabgap` zone option in the
/// model: building from MdC, writing back, copying.
public class MdCModelWriterTabTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private String write(HieroglyphicText text) {
        return new MdCModelWriter().toMdC(text);
    }

    @Test
    public void bareTabHasDefaultValues() throws MDCSyntaxError {
        Tab tab = assertInstanceOf(Tab.class, parse("A1-%tab-B1").getTopItemAt(1));
        assertEquals(Tab.AUTO, tab.getIndex());
        assertFalse(tab.hasExplicitIndex());
        assertEquals(LineAlignment.START, tab.getAlignment());
    }

    @Test
    public void optionsAreRead() throws MDCSyntaxError {
        Tab tab = assertInstanceOf(Tab.class, parse("A1-%tab[n=3,align=end]-B1").getTopItemAt(1));
        assertEquals(3, tab.getIndex());
        assertEquals(LineAlignment.END, tab.getAlignment());
    }

    @Test
    public void invalidOptionsGiveDefaults() throws MDCSyntaxError {
        Tab tab = assertInstanceOf(Tab.class, parse("A1-%tab[n=0,align=bogus]-B1").getTopItemAt(1));
        assertEquals(Tab.AUTO, tab.getIndex());
        assertEquals(LineAlignment.START, tab.getAlignment());
    }

    @Test
    public void absoluteTabIsStillRead() throws MDCSyntaxError {
        AbsoluteTab tab = assertInstanceOf(AbsoluteTab.class, parse("A1-?20-B1").getTopItemAt(1));
        assertEquals(20, tab.getStopPos());
        assertEquals("A1-?20-B1", write(parse("A1-?20-B1")).trim());
    }

    @Test
    public void defaultOptionsAreNotWritten() throws MDCSyntaxError {
        assertEquals("A1-%tab-B1", write(parse("A1-%tab[align=start]-B1")).trim());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "A1-%tab-B1",
        "A1-%tab[n=3]-B1",
        "A1-%tab[align=center]-B1",
        "A1-%tab[n=2,align=justify]-B1-%tab-C1-!-D1-%tab[n=3]-E1",
        "%zone[tabgap=50](A1-%tab[align=end]-B1)",
        "%tab",
    })
    public void tabsAreWrittenBack(String mdc) throws MDCSyntaxError {
        HieroglyphicText text = parse(mdc);
        String written = write(text);
        assertTrue(text.equalsIgnoreId(parse(written)), written);
    }

    @Test
    public void tabGapIsReadAndWritten() throws MDCSyntaxError {
        Zone zone = assertInstanceOf(Zone.class, parse("%zone[tabgap=50](A1)").getTopItemAt(0));
        assertEquals(50, zone.getParameters().tabGap());
        assertTrue(write(parse("%zone[tabgap=50](A1)")).contains("tabgap=50"));
        Zone plain = assertInstanceOf(Zone.class, parse("%zone(A1)").getTopItemAt(0));
        assertEquals(ZoneParameters.UNSET, plain.getParameters().tabGap());
    }

    @Test
    public void copiesKeepTheOptions() {
        Tab tab = new Tab(2, LineAlignment.CENTER);
        assertTrue(tab.equalsIgnoreId(tab.deepCopy()));
        assertEquals(2, tab.deepCopy().getIndex());
        assertEquals(LineAlignment.CENTER, tab.deepCopy().getAlignment());
        assertEquals(LineAlignment.END, tab.withAlignment(LineAlignment.END).getAlignment());
        assertEquals(5, tab.withIndex(5).getIndex());
        assertFalse(tab.equalsIgnoreId(new Tab()));
        assertFalse(new AbsoluteTab(10).equalsIgnoreId(new AbsoluteTab(20)));
    }
}
