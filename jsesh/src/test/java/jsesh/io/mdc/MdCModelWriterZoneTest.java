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
        HieroglyphicText text = parse("A1-%zone[layout=columns,dir=rl,width=1200,height=400,lineskip=20,colskip=30,frame](A2-A3!A4)-A5");
        assertEquals(3, text.getNumberOfChildren());
        Zone zone = assertInstanceOf(Zone.class, text.getTopItemAt(1));
        ZoneParameters p = zone.getParameters();
        assertEquals(TextOrientation.VERTICAL, p.orientation());
        assertEquals(TextDirection.RIGHT_TO_LEFT, p.direction());
        assertEquals(1200, p.width());
        assertEquals(400, p.height());
        assertEquals(20, p.lineSkip());
        assertEquals(30, p.columnSkip());
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
            "%zone[lineskip=0](A1)",
            "%zone[colskip=25](A1)",
            "%zone[frame](A1)",
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
