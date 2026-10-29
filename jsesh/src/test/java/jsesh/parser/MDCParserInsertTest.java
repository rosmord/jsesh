/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jsesh.parser.ast.AstBasicItem;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstInsert;
import jsesh.parser.ast.AstInsert.InsertDirection;
import jsesh.parser.ast.AstTopItem;

/// Tests for the `%insert[dir=...](main,inserted)` construct in [MDCParser].
public class MDCParserInsertTest {

    private AstDocument parse(String mdc) throws MDCSyntaxError {
        return new MDCParser().parse(mdc);
    }

    /// Parses a text made of a single cadrat holding a single insert.
    private AstInsert parseInsert(String mdc) throws MDCSyntaxError {
        AstDocument document = parse(mdc);
        assertEquals(1, document.topItems().items().size());
        AstTopItem item = document.topItems().items().get(0);
        AstCadrat cadrat = (AstCadrat) item;
        assertEquals(1, cadrat.hBoxes().size());
        assertEquals(1, cadrat.hBoxes().get(0).elements().size());
        return (AstInsert) cadrat.hBoxes().get(0).elements().get(0);
    }

    /// The code of the hieroglyph in a basic item which is a single-sign cadrat.
    private String signCode(AstBasicItem item) {
        AstCadrat cadrat = (AstCadrat) item;
        return ((AstHieroglyph) cadrat.hBoxes().get(0).elements().get(0)).code();
    }

    @Test
    public void testSimpleInsert() throws MDCSyntaxError {
        AstInsert insert = parseInsert("%insert[dir=ts](A1,B1)");
        assertEquals(InsertDirection.TOP_START, insert.direction());
        assertEquals(1, insert.firstItems().items().size());
        assertEquals("A1", signCode(insert.firstItems().items().get(0)));
        assertEquals(1, insert.secondItems().items().size());
        assertEquals("B1", signCode(insert.secondItems().items().get(0)));
    }

    @ParameterizedTest
    @CsvSource({
        "s, START", "e, END", "t, TOP", "b, BOTTOM",
        "ts, TOP_START", "bs, BOTTOM_START",
        "te, TOP_END", "be, BOTTOM_END", "c, CENTER"
    })
    public void testAllDirections(String code, InsertDirection expected) throws MDCSyntaxError {
        AstInsert insert = parseInsert("%insert[dir=" + code + "](A1,B1)");
        assertEquals(expected, insert.direction());
    }

    @Test
    public void testDirOptionIsRemovedFromOptions() throws MDCSyntaxError {
        AstInsert insert = parseInsert("%insert[dir=t](A1,B1)");
        assertFalse(insert.options().hasOption("dir"));
    }

    @Test
    public void testOtherOptionsAreKept() throws MDCSyntaxError {
        AstInsert insert = parseInsert("%insert[dir=t,fit](A1,B1)");
        assertEquals(InsertDirection.TOP, insert.direction());
        assertFalse(insert.options().hasOption("dir"));
        assertTrue(insert.options().hasOption("fit"));
    }

    @Test
    public void testInsertAmongOtherSigns() throws MDCSyntaxError {
        AstDocument document = parse("i-%insert[dir=e](A1,B1)-w");
        assertEquals(3, document.topItems().items().size());
        AstCadrat middle = (AstCadrat) document.topItems().items().get(1);
        assertTrue(middle.hBoxes().get(0).elements().get(0) instanceof AstInsert);
    }

    @Test
    public void testTargetDefaultsToFirstArgument() throws MDCSyntaxError {
        assertEquals(1, parseInsert("%insert[dir=t](A1,B1)").target());
    }

    @Test
    public void testTargetIsSecondArgumentAndArgumentsKeepTheirOrder() throws MDCSyntaxError {
        AstInsert insert = parseInsert("%insert[dir=bs,target=2](B1,A1)");
        assertEquals(InsertDirection.BOTTOM_START, insert.direction());
        assertEquals(2, insert.target());
        assertEquals("B1", signCode(insert.firstItems().items().get(0)));
        assertEquals("A1", signCode(insert.secondItems().items().get(0)));
        assertFalse(insert.options().hasOption("target"));
    }

    @ParameterizedTest
    @CsvSource({ "0", "3", "-1" })
    public void testBadTarget(int target) {
        assertThrows(MDCSyntaxError.class, () -> parse("%insert[dir=t,target=" + target + "](A1,B1)"));
    }

    @Test
    public void testMissingDir() {
        assertThrows(MDCSyntaxError.class, () -> parse("%insert[fit](A1,B1)"));
    }

    @Test
    public void testUnknownDir() {
        assertThrows(MDCSyntaxError.class, () -> parse("%insert[dir=zz](A1,B1)"));
    }

    @Test
    public void testMainElementCanBeAGroup() throws MDCSyntaxError {
        AstInsert insert = parseInsert("%insert[dir=t](A1-B1:C1,D1)");
        assertEquals(2, insert.firstItems().items().size());
        assertEquals(1, insert.secondItems().items().size());
    }

    @Test
    public void testInsertionsCanBeNested() throws MDCSyntaxError {
        AstInsert outer = parseInsert("%insert[dir=te](%insert[dir=bs](t,w),t)");
        assertEquals(InsertDirection.TOP_END, outer.direction());
        assertEquals(1, outer.firstItems().items().size());
        AstCadrat cadrat = (AstCadrat) outer.firstItems().items().get(0);
        AstInsert inner = (AstInsert) cadrat.hBoxes().get(0).elements().get(0);
        assertEquals(InsertDirection.BOTTOM_START, inner.direction());
        assertEquals("t", signCode(inner.firstItems().items().get(0)));
        assertEquals("w", signCode(inner.secondItems().items().get(0)));
        assertEquals("t", signCode(outer.secondItems().items().get(0)));
    }

    @Test
    public void testMissingOptionList() {
        assertThrows(MDCSyntaxError.class, () -> parse("%insert(A1,B1)"));
    }

    @Test
    public void testMissingComma() {
        assertThrows(MDCSyntaxError.class, () -> parse("%insert[dir=t](A1 B1)"));
    }

    @Test
    public void testMissingClosingParenthesis() {
        assertThrows(MDCSyntaxError.class, () -> parse("%insert[dir=t](A1,B1"));
    }

    @Test
    /// Test of a complex insert in a complex context
    public void testInsertInContext() throws MDCSyntaxError {
        AstDocument document = parse("i-%insert[dir=e](A1,B1-k)*t:n-w");  
        assertNotNull(document);   
    }
}
