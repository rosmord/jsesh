/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.Stack;
import jsesh.parser.MDCSyntaxError;

/// `%stack` in the model: building from MdC, writing back, copying.
public class MdCModelWriterStackTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static Stack findStack(HieroglyphicText text) {
        Stack[] found = new Stack[1];
        text.accept(new ModelElementDeepAdapter() {
            @Override
            public void visitStack(Stack stack) {
                if (found[0] == null) {
                    found[0] = stack;
                }
            }
        });
        return found[0];
    }

    @Test
    public void modelIsBuiltFromMdC() throws MDCSyntaxError {
        Stack stack = findStack(parse("i-%stack(O1:n,t*w)-w"));
        assertNotNull(stack);
        assertEquals(1, stack.getFirst().getBasicItemList().getNumberOfChildren());
        assertEquals(1, stack.getSecond().getBasicItemList().getNumberOfChildren());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "%stack(O1,n)",
            "i-%stack(O1:n,t*w)-w",
            "%stack(O1-n:t,x)",
            "%stack(%stack(O1,n),t)",
            "%stack(O1,%stack(n,t))",
    })
    public void writingAndReadingBackGivesTheSameModel(String mdc) throws MDCSyntaxError {
        HieroglyphicText first = parse(mdc);
        String written = new MdCModelWriter().toMdC(first);
        assertTrue(written.contains("%stack("), written);
        HieroglyphicText second = parse(written);
        assertTrue(first.equalsIgnoreId(second), mdc + " -> " + written);
    }

    @Test
    public void deepCopyIsEqual() throws MDCSyntaxError {
        Stack stack = findStack(parse("%stack(O1:n,t)"));
        assertTrue(stack.equalsIgnoreId(stack.deepCopy()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "%stack(A1)", "%stack(A1,B1", "%stack A1,B1" })
    public void malformedStackIsRejected(String mdc) {
        assertThrows(MDCSyntaxError.class, () -> parse(mdc));
    }
}
