/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.Superscript;
import jsesh.parser.MDCSyntaxError;

/// Superscript text is escaped when written (`\` becomes `\\`, `-` becomes `\-`), and read back unchanged.
public class MdCModelWriterSuperscriptTest {

    private static String write(String superscriptText) {
        HieroglyphicText text = new HieroglyphicText();
        text.addTopItem(new Superscript(superscriptText));
        return new MdCModelWriter().toMdC(text);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12", // plain
            "a-b", // hyphen
            "a\\", // trailing backslash: must not protect the closing hyphen
            "a\\b", // backslash inside
            "a\\-b", // backslash followed by hyphen
            "\\\\", // two backslashes
    })
    public void superscriptSurvivesWriteThenRead(String superscriptText) throws MDCSyntaxError {
        HieroglyphicText reread = new MDCParserModelGenerator().parse(write(superscriptText));
        Superscript s = (Superscript) reread.getTopItemAt(0);
        assertEquals(superscriptText, s.getText());
    }

    @Test
    public void escapesBackslashAndHyphen() {
        assertEquals("|a\\\\-", write("a\\"));
        assertEquals("|a\\-b-", write("a-b"));
    }
}
