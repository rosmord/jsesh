/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.possibilities.PossibilitiesList;
import jsesh.glyphs.possibilities.Possibility;
import jsesh.glyphs.possibilities.PossibilityRepository;
import jsesh.signcodes.ManuelDeCodage;

/// The embedded signs of a document: list, shapes, possibilities.
public class EmbeddedSignsTest {

    /// A user SVG, 1000×1800 units (the size of the signs of the user folder).
    static final byte[] USER_SVG = ("<?xml version='1.0'?>\n"
            + "<svg xmlns='http://www.w3.org/2000/svg' width='1000' height='1800'>"
            + "<path d='M 0 0 L 1000 0 L 1000 1800 L 0 1800 Z'/></svg>").getBytes(StandardCharsets.UTF_8);

    /// Any data: the list doesn't read the SVG.
    private static final byte[] DATA = "<svg/>".getBytes(StandardCharsets.UTF_8);

    @Test
    public void codesAreNumberedAndNeverReused() {
        EmbeddedSignList list = new EmbeddedSignList();
        assertEquals("sign1", list.add(DATA, null, null).code());
        assertEquals("sign2", list.add(DATA, "A1", "two").code());
        assertTrue(list.remove("sign2"));
        assertEquals(1, list.size());
        assertEquals(2, list.getMaxId());
        assertEquals("sign3", list.add(DATA, null, null).code());
        assertFalse(list.contains("sign2"));
        assertFalse(list.remove("sign2"));
    }

    @Test
    public void recognizesEmbeddedSignCodes() {
        assertEquals(12, EmbeddedSignList.idOf("sign12").getAsInt());
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("sign"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("sign0"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("sign01"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("SIGN1"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("sign1A"));
        assertFalse(EmbeddedSignList.isEmbeddedSignCode("A1"));
    }

    @Test
    public void signsAreImmutableAndComparedByContent() {
        byte[] data = DATA.clone();
        EmbeddedSign s = new EmbeddedSign("sign1", data, null, null);
        data[0] = 'X';
        s.svg()[1] = 'X';
        assertArrayEquals(DATA, s.svg());
        assertEquals(s, new EmbeddedSign("sign1", DATA, null, ""));
        assertTrue(s.hasSameSvg(DATA));
    }

    @Test
    public void updateKeepsTheDrawing() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add(DATA, "A1", "old");
        EmbeddedSign s = list.update("sign1", " G17 ", "new");
        assertEquals("G17", s.variantOf());
        assertEquals("new", s.description());
        assertArrayEquals(DATA, list.get("sign1").orElseThrow().svg());
        assertNull(list.update("sign1", "  ", null).variantOf());
    }

    @Test
    public void restrictedToKeepsMaxId() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add(DATA, null, null);
        list.add(DATA, null, null);
        list.add(DATA, null, null);
        EmbeddedSignList part = list.restrictedTo(List.of("sign2", "A1"));
        assertEquals(List.of("sign2"), part.asList().stream().map(EmbeddedSign::code).collect(Collectors.toList()));
        assertEquals(3, part.getMaxId());
    }

    @Test
    public void theSvgIsReadAtJSeshScale() throws IOException {
        EmbeddedSignShapes.checkSvg(USER_SVG, "test.svg");
        ShapeChar shape = EmbeddedSignShapes.toShape("sign1", USER_SVG);
        assertEquals(18.0, shape.getBbox().getHeight(), 1e-6);
        assertEquals(10.0, shape.getBbox().getWidth(), 1e-6);
    }

    @Test
    public void renderingDataHasSizeOutlineAndZones() {
        String data = EmbeddedSignShapes.renderingData("sign1", USER_SVG);
        assertTrue(data.startsWith("{\"w\":10,\"h\":18,\"d\":\"M 0 0 "), data);
        // The zones JSesh computes are included.
        assertTrue(data.matches(".*\"zones\":\\[\\[.*"), data);
    }

    @Test
    public void badSvgIsRejected() {
        assertThrows(IOException.class,
                () -> EmbeddedSignShapes.checkSvg("not svg".getBytes(StandardCharsets.UTF_8), "bad.svg"));
    }

    @Test
    public void repositoryFollowsListAndBinding() {
        EmbeddedSignList list = new EmbeddedSignList();
        EmbeddedSignShapeRepository repo = new EmbeddedSignShapeRepository(list);
        AtomicInteger events = new AtomicInteger();
        repo.addListener(e -> events.incrementAndGet());
        ManuelDeCodage mdc = ManuelDeCodage.getInstance();

        assertNull(repo.get(mdc.getCanonicalCode("sign1")));
        list.add(USER_SVG, null, null);
        assertEquals(1, events.get());
        assertNotNull(repo.get(mdc.getCanonicalCode("sign1")));
        assertTrue(repo.getCodes().contains("sign1"));

        repo.bind(new EmbeddedSignList());
        assertEquals(2, events.get());
        assertNull(repo.get(mdc.getCanonicalCode("sign1")));
        list.add(USER_SVG, null, null); // the old list is no longer listened to.
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
            PossibilitiesList.Builder l = PossibilitiesList.builder(code);
            if (code.equals("mn")) {
                l.addSign("Y5");
            } else if (code.matches("[A-Z][0-9]+")) {
                l.addSign(code);
            }
            return l.build();
        };
        EmbeddedSignList list = new EmbeddedSignList();
        EmbeddedSignShapeRepository repo = new EmbeddedSignShapeRepository(list);
        EmbeddedSignPossibilityRepository possibilities = new EmbeddedSignPossibilityRepository(base, repo);

        assertEquals(List.of("Y5"), codes(possibilities.getPossibilityListFor("Y5")));
        list.add(DATA, "Y5", null);
        list.add(DATA, "A1", null);
        assertEquals(List.of("Y5", "sign1"), codes(possibilities.getPossibilityListFor("Y5")));
        assertEquals(List.of("Y5", "sign1"), codes(possibilities.getPossibilityListFor("mn")));
        assertEquals(List.of("sign2"), codes(possibilities.getPossibilityListFor("sign2")));
        assertEquals(List.of("G17"), codes(possibilities.getPossibilityListFor("G17")));
    }

    private static final byte[] SVG_A = "<svg id='a'/>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SVG_B = "<svg id='b'/>".getBytes(StandardCharsets.UTF_8);

    @Test
    public void importIntoTheSameListChangesNothing() {
        EmbeddedSignList list = new EmbeddedSignList();
        list.add(SVG_A, "A1", null);
        list.add(SVG_B, null, null);
        EmbeddedSignImport plan = list.planImport(list.asList());
        assertTrue(plan.renaming().isEmpty());
        assertTrue(plan.addedSigns().isEmpty());
        plan.commit();
        assertEquals(2, list.size());
        assertEquals(2, list.getMaxId());
    }

    @Test
    public void importReusesSameSvgAndNumbersTheOthers() {
        EmbeddedSignList source = new EmbeddedSignList();
        source.add(SVG_A, "A1", "a");           // sign1
        source.add(SVG_B, null, "b");           // sign2
        source.add(SVG_B.clone(), null, "b2");  // sign3, same drawing as sign2

        EmbeddedSignList target = new EmbeddedSignList();
        target.add(SVG_B, "G17", "mine");       // sign1
        target.add(DATA, null, null);
        target.add(DATA, null, null);
        target.remove("sign2");
        target.remove("sign3");                 // maxId stays 3

        EmbeddedSignImport plan = target.planImport(source.asList());
        assertEquals(java.util.Map.of("sign1", "sign4", "sign2", "sign1", "sign3", "sign1"), plan.renaming());
        assertEquals(1, plan.addedSigns().size());
        assertFalse(target.contains("sign4")); // nothing done yet

        plan.commit();
        EmbeddedSign added = target.get("sign4").orElseThrow();
        assertArrayEquals(SVG_A, added.svg());
        assertEquals("A1", added.variantOf());
        assertEquals("a", added.description());
        assertEquals("mine", target.get("sign1").orElseThrow().description()); // the target's own data is kept
        assertEquals(4, target.getMaxId());
    }

    @Test
    public void incomingSignsWithTheSameSvgShareOneNewSign() {
        EmbeddedSignList source = new EmbeddedSignList();
        source.add(SVG_A, null, null);
        source.add(SVG_A.clone(), null, null);
        EmbeddedSignList target = new EmbeddedSignList();
        target.add(SVG_B, null, null);
        EmbeddedSignImport plan = target.planImport(source.asList());
        assertEquals(java.util.Map.of("sign1", "sign2"), plan.renaming()); // sign2 keeps its name
        assertEquals(1, plan.addedSigns().size());
    }

    @Test
    public void commitRefusesAListChangedSincePlanning() {
        EmbeddedSignList source = new EmbeddedSignList();
        source.add(SVG_A, null, null);
        EmbeddedSignList target = new EmbeddedSignList();
        EmbeddedSignImport plan = target.planImport(source.asList());
        target.add(SVG_B, null, null);
        assertThrows(IllegalStateException.class, plan::commit);
    }

    private static List<String> codes(PossibilitiesList l) {
        return l.asList().stream().map(Possibility::getCode).collect(Collectors.toList());
    }
}
