/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jsesh.document.MDCDocument;
import jsesh.document.embeddedsigns.EmbeddedSign;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.model.constants.Dialect;
import jsesh.parser.MDCSyntaxError;

/// Embedded signs in the document header.
public class EmbeddedSignsHeaderCodecTest {

    private static final String SVG = "<svg width='10' height='18'>\n<path d='M 0 0 L 10 18 Z'/>\n</svg>\n";

    private static EmbeddedSignList sampleList() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add(SVG, "A1", "Un homme assis, variante « tardive »");
        list.add(SVG, null, null);
        list.add(SVG, null, "removed");
        list.remove("SIGN3");
        return list;
    }

    @Test
    public void roundTrip() {
        Map<String, String> header = EmbeddedSignsHeaderCodec.toHeader(sampleList());
        assertEquals("3", header.get("JSesh_max_embedded_sign_id"));
        assertTrue(header.get("JSesh_embedded_sign_SIGN2").startsWith("- - "));
        assertFalse(header.containsKey("JSesh_embedded_sign_SIGN3"));
        header.values().forEach(v -> assertFalse(v.contains("\n")));

        EmbeddedSignList read = EmbeddedSignsHeaderCodec.fromHeader(header);
        assertEquals(3, read.getMaxId());
        assertEquals(2, read.size());
        EmbeddedSign s1 = read.get("SIGN1").orElseThrow();
        assertEquals(SVG, s1.svg());
        assertEquals("A1", s1.variantOf());
        assertEquals("Un homme assis, variante « tardive »", s1.description());
        EmbeddedSign s2 = read.get("SIGN2").orElseThrow();
        assertNull(s2.variantOf());
        assertEquals("", s2.description());
    }

    @Test
    public void emptyListWritesNothing() {
        assertTrue(EmbeddedSignsHeaderCodec.toHeader(new EmbeddedSignList()).isEmpty());
    }

    @Test
    public void malformedSignsAreSkipped() {
        Map<String, String> header = new HashMap<>();
        header.put("JSesh_embedded_sign_SIGN1", "A1 -");
        header.put("JSesh_embedded_sign_FOO", "- - PHN2Zy8+");
        header.put("JSesh_embedded_sign_SIGN2", "- - PHN2Zy8+");
        EmbeddedSignList read = EmbeddedSignsHeaderCodec.fromHeader(header);
        assertEquals(1, read.size());
        assertEquals("<svg/>", read.get("SIGN2").orElseThrow().svg());
    }

    @Test
    public void documentRoundTrip() throws MDCSyntaxError {
        MDCDocument doc = new MDCDocument();
        doc.setFile(new File("test.gly"));
        doc.setEmbeddedSigns(sampleList());
        doc.setHieroglyphicText(new MDCDocumentReader().readString("A1-SIGN1-SIGN2", new File("x.gly"))
                .getHieroglyphicText());
        String mdc = new MDCDocumentWriter().toMdC(doc);

        MDCDocument read = new MDCDocumentReader().readString(mdc, new File("test.gly"));
        assertEquals(Dialect.JSESH1, read.getDialect());
        assertEquals(3, read.getEmbeddedSigns().getMaxId());
        assertEquals(SVG, read.getEmbeddedSigns().get("SIGN1").orElseThrow().svg());
        assertTrue(mdc.contains("SIGN1"));
        assertEquals(mdc, new MDCDocumentWriter().toMdC(read));
    }

    @Test
    public void embeddedSignsForceTheGlyFormat() {
        MDCDocument doc = new MDCDocument();
        doc.setFile(new File("test.hie"));
        doc.setPhilologyIsSign(false);
        new MDCDocumentWriter().prepareForSaving(doc);
        assertTrue(doc.getFile().getName().endsWith(".hie"));

        doc.getEmbeddedSigns().add(SVG, null, null);
        new MDCDocumentWriter().prepareForSaving(doc);
        assertEquals(Dialect.JSESH1, doc.getDialect());
        assertTrue(doc.getFile().getName().endsWith(".gly"));
    }
}
