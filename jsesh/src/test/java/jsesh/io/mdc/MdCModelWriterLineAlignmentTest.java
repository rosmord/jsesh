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
import jsesh.model.HieroglyphicText;
import jsesh.model.LineBreak;
import jsesh.model.PageBreak;
import jsesh.parser.MDCSyntaxError;
import jsesh.sharedvocabulary.LineAlignment;

/// The alignment of lines (`-![align=...]`) in the model: building from MdC,
/// writing back, copying.
public class MdCModelWriterLineAlignmentTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private String write(HieroglyphicText text) {
        return new MdCModelWriter().toMdC(text);
    }

    @Test
    public void alignmentIsReadOnLineAndPageBreaks() throws MDCSyntaxError {
        HieroglyphicText text = parse("A1-![align=center]-A2-!=150%[align=end]-A3-!![align=justify]-A4-!-A5");
        LineBreak center = assertInstanceOf(LineBreak.class, text.getTopItemAt(1));
        assertEquals(LineAlignment.CENTER, center.getAlignment());
        LineBreak end = assertInstanceOf(LineBreak.class, text.getTopItemAt(3));
        assertEquals(LineAlignment.END, end.getAlignment());
        assertEquals(150, end.getSpacing());
        PageBreak justify = assertInstanceOf(PageBreak.class, text.getTopItemAt(5));
        assertEquals(LineAlignment.JUSTIFY, justify.getAlignment());
        LineBreak plain = assertInstanceOf(LineBreak.class, text.getTopItemAt(7));
        assertEquals(LineAlignment.START, plain.getAlignment());
    }

    @Test
    public void unknownAlignmentIsStart() throws MDCSyntaxError {
        HieroglyphicText text = parse("A1-![align=bogus]-A2");
        assertEquals(LineAlignment.START, ((LineBreak) text.getTopItemAt(1)).getAlignment());
    }

    @Test
    public void defaultAlignmentIsNotWritten() throws MDCSyntaxError {
        String written = write(parse("A1-![align=start]-A2-!!-A3"));
        assertFalse(written.contains("align"), written);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "A1-![align=center]-A2",
        "A1-!=150%[align=end]-A2",
        "A1-!![align=justify]-A2",
        "%zone[width=1200](A1-![align=end]-A2)-B1",
    })
    public void alignmentIsWrittenBack(String mdc) throws MDCSyntaxError {
        HieroglyphicText text = parse(mdc);
        String written = write(text);
        assertTrue(written.contains("[align="), written);
        assertTrue(text.equalsIgnoreId(parse(written)), written);
    }

    @Test
    public void copiesKeepTheAlignment() {
        LineBreak b = new LineBreak(200, LineAlignment.CENTER);
        assertEquals(LineAlignment.CENTER, b.deepCopy().getAlignment());
        assertEquals(200, b.deepCopy().getSpacing());
        assertEquals(LineAlignment.END, b.withAlignment(LineAlignment.END).getAlignment());
        assertEquals(LineAlignment.JUSTIFY, new PageBreak(LineAlignment.JUSTIFY).deepCopy().getAlignment());
        assertFalse(b.equalsIgnoreId(new LineBreak(200)));
        assertFalse(new PageBreak().equalsIgnoreId(new PageBreak(LineAlignment.END)));
    }
}
