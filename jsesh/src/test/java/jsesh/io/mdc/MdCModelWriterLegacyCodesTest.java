/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;

/// Legacy sign codes are read, and written back in their current form.
public class MdCModelWriterLegacyCodesTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "`    | Ff1",
            "Ff1  | Ff1",
            "A1-` | A1-Ff1",
            "J1   | Aa1",
            "Ff2  | V49A",
    })
    public void legacyCodeIsWrittenInCurrentForm(String mdc, String expected) throws MDCSyntaxError {
        assertEquals(expected, new MdCModelWriter().toMdC(new MDCParserModelGenerator().parse(mdc)));
    }
}
