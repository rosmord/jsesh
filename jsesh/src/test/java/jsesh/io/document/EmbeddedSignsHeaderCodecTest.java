/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.document;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    /// An SVG file as a user might give it: Latin-1, with an accented comment.
    private static final byte[] SVG = ("<?xml version='1.0' encoding='ISO-8859-1'?>\n<!-- signe dessiné -->\n"
            + "<svg xmlns='http://www.w3.org/2000/svg' width='10' height='18'>\n"
            + "<path d='M 0 0 L 10 18 L 0 18 Z'/>\n</svg>\n").getBytes(StandardCharsets.ISO_8859_1);

    private static EmbeddedSignList sampleList() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add(SVG, "A1", "Un homme assis, variante « tardive »");
        list.add(SVG, null, null);
        list.add(SVG, null, "removed");
        list.remove("sign3");
        return list;
    }

    private static String unbase64(String base64) {
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    @Test
    public void roundTripKeepsTheOriginalFile() {
        Map<String, String> header = EmbeddedSignsHeaderCodec.toHeader(sampleList());
        assertEquals("3", header.get("JSesh_max_embedded_sign_id"));
        assertTrue(header.get("JSesh_embedded_sign_sign2").startsWith("- v0 - "));
        assertFalse(header.containsKey("JSesh_embedded_sign_sign3"));
        header.values().forEach(v -> assertFalse(v.contains("\n")));

        EmbeddedSignList read = EmbeddedSignsHeaderCodec.fromHeader(header);
        assertEquals(3, read.getMaxId());
        assertEquals(2, read.size());
        EmbeddedSign s1 = read.get("sign1").orElseThrow();
        assertArrayEquals(SVG, s1.svg());
        assertEquals("A1", s1.variantOf());
        assertEquals("Un homme assis, variante « tardive »", s1.description());
        EmbeddedSign s2 = read.get("sign2").orElseThrow();
        assertNull(s2.variantOf());
        assertEquals("", s2.description());
    }

    @Test
    public void aVariantOfWhichIsNotAGardinerCodeIsDropped() {
        Map<String, String> header = EmbeddedSignsHeaderCodec.toHeader(sampleList());
        String value = header.get("JSesh_embedded_sign_sign1");
        header.put("JSesh_embedded_sign_sign1", "mn" + value.substring(value.indexOf(' ')));
        EmbeddedSign s1 = EmbeddedSignsHeaderCodec.fromHeader(header).get("sign1").orElseThrow();
        assertNull(s1.variantOf());
        assertArrayEquals(SVG, s1.svg());
    }

    @Test
    public void theFieldsAreBase64Only() {
        String value = EmbeddedSignsHeaderCodec.toHeader(sampleList()).get("JSesh_embedded_sign_sign1");
        String[] parts = value.split(" ");
        assertEquals(5, parts.length);
        assertEquals("A1", parts[0]);
        assertEquals("v0", parts[1]);
        assertArrayEquals(SVG, Base64.getDecoder().decode(parts[3]));
        assertTrue(unbase64(parts[4]).startsWith("{\"w\":10,\"h\":18,\"d\":\"M 0 0 "));
    }

    @Test
    public void emptyListWritesNothing() {
        assertTrue(EmbeddedSignsHeaderCodec.toHeader(new EmbeddedSignList()).isEmpty());
    }

    @Test
    public void malformedSignsAreSkipped() {
        String good = EmbeddedSignsHeaderCodec.toHeader(sampleList()).get("JSesh_embedded_sign_sign2");
        Map<String, String> header = new HashMap<>();
        header.put("JSesh_embedded_sign_sign1", "A1 v0 -");
        header.put("JSesh_embedded_sign_sign3", "- v0 - PHN2Zy8@ PHN2Zy8+"); // not base64
        header.put("JSesh_embedded_sign_sign4", good.replace(" v0 ", " v1 ")); // unknown version
        header.put("JSesh_embedded_sign_sign5", good.replace(" v0 ", " ")); // no version
        header.put("JSesh_embedded_sign_FOO", good);
        header.put("JSesh_embedded_sign_sign2", good);
        EmbeddedSignList read = EmbeddedSignsHeaderCodec.fromHeader(header);
        assertEquals(1, read.size());
        assertArrayEquals(SVG, read.get("sign2").orElseThrow().svg());
    }

    @Test
    public void documentRoundTrip() throws MDCSyntaxError {
        MDCDocument doc = new MDCDocument();
        doc.setFile(new File("test.gly"));
        doc.setEmbeddedSigns(sampleList());
        doc.setHieroglyphicText(new MDCDocumentReader().readString("A1-sign1-sign2", new File("x.gly"))
                .getHieroglyphicText());
        String mdc = new MDCDocumentWriter().toMdC(doc);

        MDCDocument read = new MDCDocumentReader().readString(mdc, new File("test.gly"));
        assertEquals(Dialect.JSESH1, read.getDialect());
        assertEquals(3, read.getEmbeddedSigns().getMaxId());
        assertArrayEquals(SVG, read.getEmbeddedSigns().get("sign1").orElseThrow().svg());
        assertTrue(mdc.contains("sign1"));
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
