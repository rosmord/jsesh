/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.signdata.PossibilitiesList;
import jsesh.glyphs.signdata.Possibility;
import jsesh.glyphs.signdata.PossibilityRepository;
import jsesh.signcodes.ManuelDeCodage;

/// The embedded signs of a document: list, shapes, possibilities.
public class EmbeddedSignsTest {

    /// A user SVG, 1000×1800 units (the size of the signs of the user folder).
    static final String USER_SVG = "<?xml version='1.0'?>\n"
            + "<svg xmlns='http://www.w3.org/2000/svg' width='1000' height='1800'>"
            + "<path d='M 0 0 L 1000 0 L 1000 1800 L 0 1800 Z'/></svg>";

    static String normalizedSvg() throws IOException {
        return EmbeddedSignShapes.normalizeSvg(
                new ByteArrayInputStream(USER_SVG.getBytes(StandardCharsets.UTF_8)), "test.svg");
    }

    @Test
    public void codesAreNumberedAndNeverReused() {
        EmbeddedSignList list = new EmbeddedSignList();
        assertEquals("SIGN1", list.add("<svg/>", null, null).code());
        assertEquals("SIGN2", list.add("<svg/>", "A1", "two").code());
        assertTrue(list.remove("SIGN2"));
        assertEquals(1, list.size());
        assertEquals(2, list.getMaxId());
        assertEquals("SIGN3", list.add("<svg/>", null, null).code());
        assertFalse(list.contains("SIGN2"));
        assertFalse(list.remove("SIGN2"));
    }

    @Test
    public void recognizesEmbeddedSignCodes() {
        assertEquals(12, EmbeddedSignList.idOf("SIGN12").getAsInt());
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("SIGN"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("SIGN0"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("SIGN01"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("sign1"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("SIGN1A"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("A1"));
    }

    @Test
    public void updateKeepsTheDrawing() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add("<svg/>", "A1", "old");
        EmbeddedSign s = list.update("SIGN1", " G17 ", "new");
        assertEquals("G17", s.variantOf());
        assertEquals("new", s.description());
        assertEquals("<svg/>", list.get("SIGN1").orElseThrow().svg());
        assertNull(list.update("SIGN1", "  ", null).variantOf());
    }

    @Test
    public void restrictedToKeepsMaxId() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add("<svg/>", null, null);
        list.add("<svg/>", null, null);
        list.add("<svg/>", null, null);
        EmbeddedSignList part = list.restrictedTo(List.of("SIGN2", "A1"));
        assertEquals(List.of("SIGN2"), part.asList().stream().map(EmbeddedSign::code).collect(Collectors.toList()));
        assertEquals(3, part.getMaxId());
    }

    @Test
    public void normalizedSvgIsAtJSeshScaleAndStable() throws IOException {
        String svg = normalizedSvg();
        ShapeChar shape = EmbeddedSignShapes.toShape("SIGN1", svg);
        assertEquals(18.0, shape.getBbox().getHeight(), 1e-6);
        assertEquals(10.0, shape.getBbox().getWidth(), 1e-6);
        // The zones JSesh computes are stored.
        assertTrue(svg.contains("id='zone1'"));
        assertTrue(shape.hasZones());
        // Reading and writing again changes nothing.
        assertEquals(svg, EmbeddedSignShapes.toSvg(shape));
    }

    @Test
    public void badSvgIsRejected() {
        assertThrows(IOException.class, () -> EmbeddedSignShapes.normalizeSvg(
                new ByteArrayInputStream("not svg".getBytes(StandardCharsets.UTF_8)), "bad.svg"));
    }

    @Test
    public void repositoryFollowsListAndBinding() throws IOException {
        EmbeddedSignList list = new EmbeddedSignList();
        EmbeddedSignShapeRepository repo = new EmbeddedSignShapeRepository(list);
        AtomicInteger events = new AtomicInteger();
        repo.addListener(e -> events.incrementAndGet());
        ManuelDeCodage mdc = ManuelDeCodage.getInstance();

        assertNull(repo.get(mdc.getCanonicalCode("SIGN1")));
        list.add(normalizedSvg(), null, null);
        assertEquals(1, events.get());
        assertNotNull(repo.get(mdc.getCanonicalCode("SIGN1")));
        assertTrue(repo.getCodes().contains("SIGN1"));

        repo.bind(new EmbeddedSignList());
        assertEquals(2, events.get());
        assertNull(repo.get(mdc.getCanonicalCode("SIGN1")));
        list.add(normalizedSvg(), null, null); // the old list is no longer listened to.
        assertEquals(2, events.get());
    }

    @Test
    public void findLooksInsideComposites() {
        EmbeddedSignList list = new EmbeddedSignList();
        CompositeHieroglyphShapeRepository composite = new CompositeHieroglyphShapeRepository();
        composite.addRepository(new EmbeddedSignShapeRepository(list));
        assertTrue(EmbeddedSignShapeRepository.find(composite).orElseThrow() == list);
        assertTrue(EmbeddedSignShapeRepository.find(new CompositeHieroglyphShapeRepository()).isEmpty());
    }

    @Test
    public void possibilitiesProposeVariants() {
        // A base repository: Y5 for "mn", the code itself for Gardiner codes.
        PossibilityRepository base = code -> {
            PossibilitiesList l = new PossibilitiesList(code);
            if (code.equals("mn")) {
                l.addSign("Y5");
            } else if (code.matches("[A-Z][0-9]+")) {
                l.addSign(code);
            }
            return l;
        };
        EmbeddedSignList list = new EmbeddedSignList();
        EmbeddedSignShapeRepository repo = new EmbeddedSignShapeRepository(list);
        EmbeddedSignPossibilityRepository possibilities = new EmbeddedSignPossibilityRepository(base, repo);

        assertEquals(List.of("Y5"), codes(possibilities.getPossibilityListFor("Y5")));
        list.add("<svg/>", "Y5", null);
        list.add("<svg/>", "A1", null);
        assertEquals(List.of("Y5", "SIGN1"), codes(possibilities.getPossibilityListFor("Y5")));
        assertEquals(List.of("Y5", "SIGN1"), codes(possibilities.getPossibilityListFor("mn")));
        assertEquals(List.of("SIGN2"), codes(possibilities.getPossibilityListFor("SIGN2")));
        assertEquals(List.of("G17"), codes(possibilities.getPossibilityListFor("G17")));
    }

    private static List<String> codes(PossibilitiesList l) {
        return l.asList().stream().map(Possibility::getCode).collect(Collectors.toList());
    }
}
