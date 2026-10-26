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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.Insert;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.constants.InsertionDirection;
import jsesh.parser.MDCSyntaxError;

/// `@insert` in the model: building from MdC, writing back, copying.
public class MdCModelWriterInsertTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static Insert findInsert(HieroglyphicText text) {
        Insert[] found = new Insert[1];
        text.accept(new ModelElementDeepAdapter() {
            @Override
            public void visitInsert(Insert insert) {
                if (found[0] == null) {
                    found[0] = insert;
                }
            }
        });
        return found[0];
    }

    @Test
    public void modelIsBuiltFromMdC() throws MDCSyntaxError {
        Insert insert = findInsert(parse("i-@insert[dir=bs,dw=20,dh=30](O1,n:t)-w"));
        assertNotNull(insert);
        assertEquals(InsertionDirection.BOTTOM_START, insert.getDirection());
        assertEquals(20, insert.getDw());
        assertEquals(30, insert.getDh());
        assertEquals(1, insert.getMain().getBasicItemList().getNumberOfChildren());
        assertEquals(1, insert.getInserted().getBasicItemList().getNumberOfChildren());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "@insert[dir=s](O1,n)",
            "@insert[dir=ts,dw=20](O1,n:t)",
            "@insert[dir=be,dw=10,dh=40](O1,n-t)",
            "i-@insert[dir=c](O1,n*t)-w",
            "@insert[dir=te](@insert[dir=bs](O1,n),t)",
            "@insert[dir=t](O1-n:t,x)",
    })
    public void writingAndReadingBackGivesTheSameModel(String mdc) throws MDCSyntaxError {
        HieroglyphicText first = parse(mdc);
        String written = new MdCModelWriter().toMdC(first);
        assertTrue(written.contains("@insert[dir="), written);
        HieroglyphicText second = parse(written);
        assertTrue(first.equalsIgnoreId(second), mdc + " -> " + written);
    }

    @Test
    public void optionsDefaultToZeroAndNegativeValuesAreClamped() throws MDCSyntaxError {
        Insert insert = findInsert(parse("@insert[dir=t](O1,n)"));
        assertEquals(0, insert.getDw());
        assertEquals(0, insert.getDh());
        insert.setDw(-5);
        assertEquals(0, insert.getDw());
    }

    @Test
    public void nestedInsertionsAreBuilt() throws MDCSyntaxError {
        List<Insert> inserts = new ArrayList<>();
        parse("@insert[dir=te](@insert[dir=bs](O1,n),t)").accept(new ModelElementDeepAdapter() {
            @Override
            public void visitInsert(Insert insert) {
                inserts.add(insert);
                super.visitInsert(insert);
            }
        });
        assertEquals(2, inserts.size());
        assertEquals(InsertionDirection.TOP_END, inserts.get(0).getDirection());
        assertEquals(InsertionDirection.BOTTOM_START, inserts.get(1).getDirection());
    }

    @Test
    public void deepCopyKeepsEverything() throws MDCSyntaxError {
        Insert insert = findInsert(parse("@insert[dir=be,dw=15](O1,n:t)"));
        ModelElement copy = insert.deepCopy();
        assertTrue(insert.equalsIgnoreId(copy));
        assertEquals(0, insert.compareTo(copy));
    }
}
