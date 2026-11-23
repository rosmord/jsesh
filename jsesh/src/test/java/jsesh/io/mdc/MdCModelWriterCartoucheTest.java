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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Cartouche;
import jsesh.model.HieroglyphicText;
import jsesh.parser.MDCSyntaxError;

/// The `[fill]` option of cartouches (see [Cartouche#isFill()]): reading,
/// writing, copying.
public class MdCModelWriterCartoucheTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private String write(HieroglyphicText text) {
        return new MdCModelWriter().toMdC(text);
    }

    private static Cartouche cartouche(HieroglyphicText text) {
        // top item -> quadrat -> hbox -> cartouche
        return (Cartouche) text.getTopItemAt(0).getChildAt(0).getChildAt(0);
    }

    @Test
    public void aPlainCartoucheDoesNotFill() throws MDCSyntaxError {
        assertFalse(cartouche(parse("<-A1->")).isFill());
    }

    @Test
    public void fillIsRead() throws MDCSyntaxError {
        assertTrue(cartouche(parse("<-A1->[fill]")).isFill());
    }

    @Test
    public void fillIsReadForPartialCartouches() throws MDCSyntaxError {
        assertTrue(cartouche(parse("<s1-A1-s2>[fill]")).isFill());
        assertTrue(cartouche(parse("<1-A1-0>[fill]")).isFill());
    }

    @Test
    public void explicitValuesAreAccepted() throws MDCSyntaxError {
        assertTrue(cartouche(parse("<-A1->[fill=yes]")).isFill());
        assertFalse(cartouche(parse("<-A1->[fill=no]")).isFill());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<-A1->[fill]", "<-A1-B1->[fill]", "<s1-A1-s2>[fill]", "<-A1->[fill]-B1",
            "%tab-<-A1->[fill]-%tab-B1" })
    public void fillSurvivesAWriteAndReadCycle(String mdc) throws MDCSyntaxError {
        String written = write(parse(mdc));
        assertTrue(written.contains("[fill]"), written);
        assertEquals(written, write(parse(written)));
    }

    @Test
    public void nothingIsWrittenWithoutFill() throws MDCSyntaxError {
        assertFalse(write(parse("<-A1->")).contains("["));
    }

    @Test
    public void copiesKeepFill() throws MDCSyntaxError {
        Cartouche c = cartouche(parse("<-A1->[fill]"));
        assertTrue(c.deepCopy().isFill());
        assertEquals(c.deepCopy().compareTo(c), 0);
    }

    @Test
    public void fillMakesCartouchesDifferent() throws MDCSyntaxError {
        Cartouche filling = cartouche(parse("<-A1->[fill]"));
        Cartouche plain = cartouche(parse("<-A1->"));
        assertNotEquals(0, filling.compareTo(plain));
        assertFalse(filling.equalsIgnoreId(plain));
    }
}
