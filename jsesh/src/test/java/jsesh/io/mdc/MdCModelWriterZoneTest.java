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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.LayoutSetting;
import jsesh.model.LineBreak;
import jsesh.model.ModelElement;
import jsesh.model.PageBreak;
import jsesh.model.Zone;
import jsesh.model.ZoneParameters;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;

/// `%zone` in the model: building from MdC, writing back, copying.
public class MdCModelWriterZoneTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private String write(HieroglyphicText text) {
        return new MdCModelWriter().toMdC(text);
    }

    @Test
    public void zoneIsBuiltFromMdC() throws MDCSyntaxError {
        HieroglyphicText text = parse("A1-%zone[layout=columns,dir=rl,width=1200,height=400,linegap=20,quadratgap=30,frame](A2-A3!A4)-A5");
        assertEquals(3, text.getNumberOfChildren());
        Zone zone = assertInstanceOf(Zone.class, text.getTopItemAt(1));
        ZoneParameters p = zone.getParameters();
        assertEquals(TextOrientation.VERTICAL, p.orientation());
        assertEquals(TextDirection.RIGHT_TO_LEFT, p.direction());
        assertEquals(1200, p.width());
        assertEquals(400, p.height());
        assertEquals(20.0, p.settings().length(LayoutSetting.LINE_GAP));
        assertEquals(30.0, p.settings().length(LayoutSetting.QUADRAT_GAP));
        assertTrue(p.frame());
        assertEquals(ZoneParameters.UNSET, p.separator());
        assertEquals(4, zone.getContent().getNumberOfChildren());
        assertInstanceOf(LineBreak.class, zone.getContent().getItemAt(2));
    }

    @Test
    public void missingOptionsAreInherited() throws MDCSyntaxError {
        Zone zone = assertInstanceOf(Zone.class, parse("%zone(A1)").getTopItemAt(0));
        assertEquals(ZoneParameters.DEFAULT, zone.getParameters());
        assertNull(zone.getParameters().orientation());
        assertFalse(zone.getParameters().frame());
        assertNull(zone.getParameters().settings().flag(LayoutSetting.FIT_SIGNS));
        assertTrue(zone.getParameters().settings().isEmpty());
    }

    @Test
    public void fitSignsIsReadAsYesOrNo() throws MDCSyntaxError {
        Zone yes = assertInstanceOf(Zone.class, parse("%zone[fitsigns=yes](A1)").getTopItemAt(0));
        Zone no = assertInstanceOf(Zone.class, parse("%zone[fitsigns=no](A1)").getTopItemAt(0));
        assertEquals(Boolean.TRUE, yes.getParameters().settings().flag(LayoutSetting.FIT_SIGNS));
        assertEquals(Boolean.FALSE, no.getParameters().settings().flag(LayoutSetting.FIT_SIGNS));
    }

    @Test
    public void fillIsReadAsYesOrNo() throws MDCSyntaxError {
        assertTrue(zoneAt("%zone[fill=yes](A1)").getParameters().fill());
        assertTrue(zoneAt("%zone[fill](A1)").getParameters().fill());
        assertFalse(zoneAt("%zone[fill=no](A1)").getParameters().fill());
        assertFalse(zoneAt("%zone[fill=maybe](A1)").getParameters().fill());
        assertFalse(zoneAt("%zone(A1)").getParameters().fill());
    }

    @Test
    public void fillIsWrittenOnlyWhenSet() throws MDCSyntaxError {
        assertTrue(write(parse("%zone[fill=yes](A1)")).contains("fill=yes"));
        assertFalse(write(parse("%zone[fill=no](A1)")).contains("fill"));
    }

    @Test
    public void fillIsIgnoredWithAFixedSizeAlongTheLinesOfTheParent() throws MDCSyntaxError {
        ZoneParameters withWidth = zoneAt("%zone[fill=yes,width=300](A1)").getParameters();
        assertFalse(withWidth.fills(true));
        assertTrue(withWidth.fills(false));
        ZoneParameters withHeight = zoneAt("%zone[fill=yes,height=300](A1)").getParameters();
        assertTrue(withHeight.fills(true));
        assertFalse(withHeight.fills(false));
    }

    private Zone zoneAt(String mdc) throws MDCSyntaxError {
        return assertInstanceOf(Zone.class, parse(mdc).getTopItemAt(0));
    }

    @Test
    public void everySharedSettingIsRead() throws MDCSyntaxError {
        Zone zone = assertInstanceOf(Zone.class, parse(
                "%zone[columngap=40,columnquadratgap=10,signheight=80,cartouchelinewidth=8,textbaseline=30,"
                        + "limitquadratwidth=no,centresmallsigns=yes,scale=50](A1)").getTopItemAt(0));
        ZoneParameters p = zone.getParameters();
        assertEquals(40.0, p.settings().length(LayoutSetting.COLUMN_GAP));
        assertEquals(10.0, p.settings().length(LayoutSetting.COLUMN_QUADRAT_GAP));
        assertEquals(80.0, p.settings().length(LayoutSetting.SIGN_HEIGHT));
        assertEquals(8.0, p.settings().length(LayoutSetting.CARTOUCHE_LINE_WIDTH));
        assertEquals(30.0, p.settings().length(LayoutSetting.TEXT_BASELINE));
        assertEquals(Boolean.FALSE, p.settings().flag(LayoutSetting.LIMIT_QUADRAT_WIDTH));
        assertEquals(Boolean.TRUE, p.settings().flag(LayoutSetting.CENTRE_SMALL_SIGNS));
        assertEquals(50, p.scale());
    }

    @Test
    public void invalidValuesAreIgnored() throws MDCSyntaxError {
        Zone zone = assertInstanceOf(Zone.class, parse("%zone[fitsigns=maybe,layout=diagonal,scale=100](A1)").getTopItemAt(0));
        assertEquals(ZoneParameters.DEFAULT, zone.getParameters());
    }

    @Test
    public void pageBreaksAreKeptInZones() throws MDCSyntaxError {
        Zone zone = assertInstanceOf(Zone.class, parse("%zone(A1-!!-A2)").getTopItemAt(0));
        assertInstanceOf(PageBreak.class, zone.getContent().getItemAt(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "%zone()",
            "%zone(A1)",
            "A1-%zone(A2)",
            "%zone(A2)-A1",
            "%zone[layout=lines](A1)",
            "%zone[layout=columns](A1)",
            "%zone[dir=rl](A1)",
            "%zone[dir=lr](A1)",
            "%zone[width=1200](A1)",
            "%zone[height=300](A1)",
            "%zone[linegap=0](A1)",
            "%zone[quadratgap=25](A1)",
            "%zone[frame](A1)",
            "%zone[fill=yes](A1)",
            "%zone[layout=columns,fill=yes,frame](A1)-!-A1-A2",
            "%zone[quadratheight=150](A1!A2)",
            "%zone[layout=columns,quadratwidth=60](A1-A2)",
            "%zone[layout=columns,fitsigns=no](n-A1)",
            "%zone[fitsigns=yes](n)",
            "%zone[scale=50](A1-A2)",
            "%zone[layout=columns,columngap=50,columnquadratgap=10](A1!A2)",
            "%zone[signheight=70,cartouchelinewidth=10,textbaseline=20](<-A1->)",
            "%zone[limitquadratwidth=no,centresmallsigns=yes](A1)",
            "%zone[tabgap=30](A1-%tab-A2)",
            "%zone[separator=5](A1!A2)",
            "%zone[layout=columns,separator=3](A1!A2)",
            "%zone[layout=columns,dir=rl,width=1200,height=400,frame](A1-A2)",
            "%zone(A1-%zone[layout=columns](A2-%zone(A3))-A4)",
            "%zone(A1)-%zone(A2)",
            "%zone(A1)!%zone(A2)",
            "%zone(A1!A2!!A3)",
            "%zone(A1-?100-A2)",
            "%zone(+lsome text+s-A1)",
            "%zone(<-A1->)",
    })
    public void writingAndReadingBackGivesTheSameModel(String mdc) throws MDCSyntaxError {
        HieroglyphicText first = parse(mdc);
        String written = write(first);
        assertTrue(written.contains("%zone"), written);
        HieroglyphicText second = parse(written);
        assertTrue(first.equalsIgnoreId(second), mdc + " -> " + written);
        assertEquals(written, write(second));
    }

    @Test
    public void unsetOptionsAreNotWritten() throws MDCSyntaxError {
        assertEquals("%zone(A1)", write(parse("%zone(A1)")).strip());
        assertEquals("%zone(A1)", write(parse("%zone[fitsigns=maybe](A1)")).strip());
        assertEquals("%zone[dir=rl](A1)", write(parse("%zone[dir=rl,width=0](A1)")).strip());
    }

    /// Toggles are read in sequence across zone limits, and written so that
    /// each zone is self-contained.
    @ParameterizedTest
    @ValueSource(strings = {
            "$r-A1-%zone(A2)-A3",
            "A1-%zone($r-A2)-A3",
            "#b-A1-%zone(A2-#e-A3)-A4",
            "$r-#b-A1-%zone(A2-$b-A3)-A4",
    })
    public void togglesCrossingZoneLimitsAreKept(String mdc) throws MDCSyntaxError {
        HieroglyphicText first = parse(mdc);
        String written = write(first);
        HieroglyphicText second = parse(written);
        assertEquals(states(first), states(second), mdc + " -> " + written);
        assertEquals(written, write(second));
    }

    /// The red/shaded states of all top items, in text order.
    private static String states(HieroglyphicText text) {
        StringBuilder result = new StringBuilder();
        collectStates(text, result);
        return result.toString();
    }

    private static void collectStates(ModelElement e, StringBuilder result) {
        for (int i = 0; i < e.getNumberOfChildren(); i++) {
            ModelElement child = e.getChildAt(i);
            if (child instanceof Zone zone) {
                result.append('(');
                collectStates(zone.getContent(), result);
                result.append(')');
            } else if (child instanceof jsesh.model.TopItem item) {
                result.append(item.isRed() ? 'r' : '-').append(item.isShaded() ? 's' : '-').append(' ');
            }
        }
    }

    @Test
    public void deepCopyKeepsEverything() throws MDCSyntaxError {
        Zone zone = assertInstanceOf(Zone.class,
                parse("%zone[layout=columns,width=500,frame](A1-%zone(A2))").getTopItemAt(0));
        ModelElement copy = zone.deepCopy();
        assertTrue(zone.equalsIgnoreId(copy));
        assertEquals(0, zone.compareTo(copy));
        Zone other = new Zone(zone.getParameters().withWidth(600), zone.getContent().deepCopy());
        assertFalse(zone.equalsIgnoreId(other));
    }
}
