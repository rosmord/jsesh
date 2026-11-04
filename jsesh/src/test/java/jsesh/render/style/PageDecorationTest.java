/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import jsesh.document.DocumentPreferences;
import jsesh.document.MDCDocument;
import jsesh.io.document.MDCDocumentReader;
import jsesh.io.document.MDCDocumentWriter;
import jsesh.render.draw.PageDecorationDrawer;

/// Page numbers and titles: what is written, saved and drawn.
public class PageDecorationTest {

    private static final PageSpecification PAGE = new PageSpecification(200, 120, 20, 20, 24, 24);

    private static DocumentPreferences prefs(PageTextPosition number, PageTextPosition title, String text) {
        return new DocumentPreferences()
                .withPage(PAGE.pageWidth(), PAGE.pageHeight(), PAGE.marginLeft(), PAGE.marginRight(),
                        PAGE.marginTop(), PAGE.marginBottom())
                .withPageTexts(number, title, text);
    }

    @Test
    public void textAtGivesTheNumberTheTitleOrBoth() {
        PageDecoration d = new PageDecoration(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.TOP_LEFT, "Stela");
        assertEquals("7", d.textAt(PageTextPosition.BOTTOM_RIGHT, 7));
        assertEquals("Stela", d.textAt(PageTextPosition.TOP_LEFT, 7));
        assertNull(d.textAt(PageTextPosition.TOP_RIGHT, 7));
        PageDecoration same = new PageDecoration(PageTextPosition.TOP_CENTER, PageTextPosition.TOP_CENTER, "Stela");
        assertEquals("Stela — 3", same.textAt(PageTextPosition.TOP_CENTER, 3));
        assertFalse(PageDecoration.NONE.isDefined());
        assertTrue(new PageDecoration(PageTextPosition.NONE, PageTextPosition.TOP_LEFT, "title").isDefined());
        assertFalse(new PageDecoration(PageTextPosition.NONE, PageTextPosition.TOP_LEFT, "").isDefined());
    }

    @Test
    public void alternatingExchangesLeftAndRightOnEvenPages() {
        PageDecoration d = new PageDecoration(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.TOP_LEFT, "T", true);
        // Odd pages: as given.
        assertEquals("1", d.textAt(PageTextPosition.BOTTOM_RIGHT, 1));
        assertEquals("T", d.textAt(PageTextPosition.TOP_LEFT, 3));
        // Even pages: exchanged.
        assertEquals("2", d.textAt(PageTextPosition.BOTTOM_LEFT, 2));
        assertNull(d.textAt(PageTextPosition.BOTTOM_RIGHT, 2));
        assertEquals("T", d.textAt(PageTextPosition.TOP_RIGHT, 4));
        assertNull(d.textAt(PageTextPosition.TOP_LEFT, 4));
        // The centre doesn't move.
        PageDecoration centred = new PageDecoration(PageTextPosition.BOTTOM_CENTER, PageTextPosition.NONE, "", true);
        assertEquals("2", centred.textAt(PageTextPosition.BOTTOM_CENTER, 2));
        // Without alternation, all pages are the same.
        assertEquals("2", new PageDecoration(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.NONE, "")
                .textAt(PageTextPosition.BOTTOM_RIGHT, 2));
    }

    @Test
    public void alternatingIsSavedOnlyWhenOn() {
        var on = prefs(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.NONE, "").withPageTexts(
                PageTextPosition.BOTTOM_RIGHT, PageTextPosition.NONE, "", true);
        assertEquals("true", on.getStringRepresentation().get("JSesh_page_texts_alternate"));
        assertTrue(DocumentPreferences.fromStringMap(on.getStringRepresentation()).isPageTextsAlternate());
        var off = prefs(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.NONE, "");
        assertFalse(off.getStringRepresentation().containsKey("JSesh_page_texts_alternate"));
        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(on, JSeshStyle.DEFAULT);
        assertTrue(style.geometry().pageDecoration().alternate());
        assertTrue(DocumentPreferencesStyleConverter.toDocumentPreferences(style).isPageTextsAlternate());
    }

    @Test
    public void evenPagesAreDrawnOnTheOtherSide() {
        PageDecoration d = new PageDecoration(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.NONE, "", true);
        BufferedImage odd = draw(d, 1);
        BufferedImage even = draw(d, 2);
        assertTrue(inked(odd, 100, 96, 200, 120));
        assertFalse(inked(odd, 0, 96, 100, 120));
        assertTrue(inked(even, 0, 96, 100, 120));
        assertFalse(inked(even, 100, 96, 200, 120));
    }

    @Test
    public void nothingIsSavedWithoutPageOrTexts() {
        var noPage = new DocumentPreferences().withPageTexts(PageTextPosition.TOP_LEFT, PageTextPosition.NONE, "x");
        assertFalse(noPage.getStringRepresentation().keySet().stream().anyMatch(k -> k.startsWith("JSesh_page_n")
                || k.equals("JSesh_page_title") || k.equals("JSesh_page_title_position")));
        var plain = prefs(PageTextPosition.NONE, PageTextPosition.NONE, "ignored");
        assertFalse(plain.getStringRepresentation().containsKey("JSesh_page_number_position"));
        assertFalse(plain.getStringRepresentation().containsKey("JSesh_page_title"));
        var numbered = prefs(PageTextPosition.BOTTOM_LEFT, PageTextPosition.NONE, "");
        assertEquals("BOTTOM_LEFT", numbered.getStringRepresentation().get("JSesh_page_number_position"));
        assertFalse(numbered.getStringRepresentation().containsKey("JSesh_page_title"));
    }

    @Test
    public void aTitleWithSpacesAndAccentsSurvivesSaving() throws Exception {
        MDCDocument document = new MDCDocument();
        document.setDocumentPreferences(
                prefs(PageTextPosition.BOTTOM_CENTER, PageTextPosition.TOP_RIGHT, "Stèle de Séti Ier"));
        String written = new MDCDocumentWriter().toMdC(document);
        assertTrue(written.contains("++JSesh_page_title Stèle de Séti Ier +s\n"), written);

        DocumentPreferences reread = new MDCDocumentReader().readString(written, new java.io.File("x.gly")).getDocumentPreferences();
        assertEquals("Stèle de Séti Ier", reread.getPageTitle());
        assertEquals(PageTextPosition.TOP_RIGHT, reread.getPageTitlePosition());
        assertEquals(PageTextPosition.BOTTOM_CENTER, reread.getPageNumberPosition());
        assertEquals(200, reread.getPageWidth(), 1e-6);
    }

    @Test
    public void theStyleKeepsThePageTexts() {
        DocumentPreferences prefs = prefs(PageTextPosition.TOP_RIGHT, PageTextPosition.BOTTOM_LEFT, "A title");
        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(prefs, JSeshStyle.DEFAULT);
        assertEquals(new PageDecoration(PageTextPosition.TOP_RIGHT, PageTextPosition.BOTTOM_LEFT, "A title"),
                style.geometry().pageDecoration());
        DocumentPreferences back = DocumentPreferencesStyleConverter.toDocumentPreferences(style);
        assertEquals("A title", back.getPageTitle());
        assertEquals(PageTextPosition.TOP_RIGHT, back.getPageNumberPosition());
    }

    /// Does the drawing leave ink in a part of the page?
    private static boolean inked(BufferedImage image, int x0, int y0, int x1, int y1) {
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BufferedImage draw(PageDecoration decoration, int pageNumber) {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.page(PAGE).pageDecoration(decoration)).build();
        BufferedImage image = new BufferedImage(200, 120, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 120);
        g.setColor(Color.BLACK);
        PageDecorationDrawer.draw(g, style, pageNumber, 0);
        g.dispose();
        return image;
    }

    @Test
    public void textsAreDrawnInTheMarginsWhereAsked() {
        BufferedImage image = draw(
                new PageDecoration(PageTextPosition.BOTTOM_RIGHT, PageTextPosition.TOP_LEFT, "Title"), 12);
        // Title: top margin (24 high), left half. Number: bottom margin, right half.
        assertTrue(inked(image, 0, 0, 100, 24));
        assertFalse(inked(image, 100, 0, 200, 24));
        assertTrue(inked(image, 100, 96, 200, 120));
        assertFalse(inked(image, 0, 96, 100, 120));
        // Nothing in the middle of the page.
        assertFalse(inked(image, 0, 24, 200, 96));
        // Left and right margins stay clear.
        assertFalse(inked(image, 0, 0, 19, 120));
        assertFalse(inked(image, 181, 0, 200, 120));
    }

    @Test
    public void nothingIsDrawnWithoutPageHeight() {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.page(PAGE.withoutHeight())
                        .pageDecoration(new PageDecoration(PageTextPosition.TOP_LEFT, PageTextPosition.NONE, "")))
                .build();
        BufferedImage image = new BufferedImage(200, 120, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 120);
        g.setColor(Color.BLACK);
        PageDecorationDrawer.draw(g, style, 1, 0);
        g.dispose();
        assertFalse(inked(image, 0, 0, 200, 120));
    }
}
