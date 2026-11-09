/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import jsesh.document.DocumentPreferences;
import jsesh.io.document.DocumentHeaderCodec;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// A page (width and margins) makes the lines wrap at its usable width.
public class PageLayoutTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final String LONG = "A1-".repeat(59) + "A1";

    private MDCView layout(String mdc, JSeshStyle style) throws MDCSyntaxError {
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    private static JSeshStyle withPage(PageSpecification page) {
        return DocumentPreferencesStyleConverter.applyDocumentPreferences(
                new DocumentPreferences().withPage(page.pageWidth(), page.pageHeight(),
                        page.marginLeft(), page.marginRight(), page.marginTop(), page.marginBottom()),
                JSeshStyle.DEFAULT);
    }

    private static List<Double> rows(MDCView view) {
        return IntStream.range(0, view.getNumberOfSubviews())
                .mapToObj(i -> view.getSubView(i).getPosition().y)
                .distinct().sorted().toList();
    }

    private static double rightMost(MDCView view) {
        return IntStream.range(0, view.getNumberOfSubviews())
                .mapToDouble(i -> view.getSubView(i).getPosition().x + view.getSubView(i).getWidth())
                .max().orElse(0);
    }

    @Test
    public void withoutPageTheTextIsOneLine() throws MDCSyntaxError {
        assertEquals(1, rows(layout(LONG, JSeshStyle.DEFAULT)).size());
    }

    @Test
    public void withAPageLinesWrapWithinTheUsableWidth() throws MDCSyntaxError {
        PageSpecification page = new PageSpecification(300, 0, 20, 40, 10, 10);
        MDCView view = layout(LONG, withPage(page));
        assertTrue(rows(view).size() >= 3, "rows: " + rows(view));
        // The right margin is the page's one, not the left margin mirrored.
        assertTrue(rightMost(view) <= page.marginLeft() + page.usableWidth() + 1e-3,
                "right: " + rightMost(view));
    }

    @Test
    public void marginsAreIndependent() {
        PageSpecification page = new PageSpecification(300, 400, 20, 40, 10, 30);
        assertEquals(240, page.usableWidth(), 1e-6);
        assertEquals(360, page.usableHeight(), 1e-6);
        assertFalse(PageSpecification.NONE.isDefined());
        assertFalse(new PageSpecification(30, 0, 20, 20, 0, 0).isDefined());
    }

    @Test
    public void pagePreferencesRoundTrip() {
        JSeshStyle style = withPage(new PageSpecification(300, 400, 20, 40, 10, 30));
        DocumentPreferences prefs = DocumentPreferencesStyleConverter.toDocumentPreferences(style);
        DocumentPreferences reread = DocumentHeaderCodec.fromHeader(DocumentHeaderCodec.toHeader(prefs));
        JSeshStyle back = DocumentPreferencesStyleConverter.applyDocumentPreferences(reread, JSeshStyle.DEFAULT);
        assertEquals(style.geometry().page(), back.geometry().page());
        assertEquals(20.0, back.geometry().leftMargin(), 1e-6);
        assertFalse(DocumentHeaderCodec.fromHeader(new java.util.HashMap<>()).hasPage());
    }

    private static final PageSpecification SMALL_PAGE = new PageSpecification(200, 120, 10, 30, 8, 8);

    /// The page of a view's item, when pages are stacked.
    private static int pageOf(double y) {
        return (int) Math.floor(y / SMALL_PAGE.pageStride());
    }

    @Test
    public void linesNeverCrossThePageBottom() throws MDCSyntaxError {
        MDCView view = layout("A1-!\n".repeat(12) + "A1", withPage(SMALL_PAGE));
        int maxPage = 0;
        for (int i = 0; i < view.getNumberOfSubviews(); i++) {
            MDCView sub = view.getSubView(i);
            double top = sub.getPosition().y;
            int page = pageOf(top);
            maxPage = Math.max(maxPage, page);
            double pageTop = page * (double) SMALL_PAGE.pageStride();
            assertTrue(top >= pageTop + SMALL_PAGE.marginTop() - 1e-3, "top " + top);
            assertTrue(top + sub.getHeight() <= pageTop + SMALL_PAGE.pageHeight() - SMALL_PAGE.marginBottom() + 1e-3,
                    "bottom of item at " + top);
        }
        assertTrue(maxPage >= 2, "pages used: " + (maxPage + 1));
        // The view covers all pages.
        assertEquals(SMALL_PAGE.pageCount(view.getHeight()), maxPage + 1);
    }

    @Test
    public void aPageBreakStartsANewPage() throws MDCSyntaxError {
        MDCView view = layout("A1-!\n!!\nA1", withPage(SMALL_PAGE));
        double lastTop = 0;
        for (int i = 0; i < view.getNumberOfSubviews(); i++) {
            lastTop = Math.max(lastTop, view.getSubView(i).getPosition().y);
        }
        assertEquals(SMALL_PAGE.pageStride() + SMALL_PAGE.marginTop(), lastTop, 1e-3);
    }

    @Test
    public void aPageWithoutHeightIsNotPaginated() throws MDCSyntaxError {
        MDCView view = layout("A1-!\n".repeat(30) + "A1", withPage(new PageSpecification(200, 0, 10, 30, 8, 8)));
        List<Double> rows = rows(view);
        double step = rows.get(1) - rows.get(0);
        for (int i = 1; i < rows.size(); i++) {
            assertEquals(step, rows.get(i) - rows.get(i - 1), 1e-3, "no jump between lines");
        }
    }

    /// A picture of a part of the text must not be as large as a page.
    @Test
    public void aSelectionPictureIsNotAsTallAsAPage() throws MDCSyntaxError {
        JSeshStyle style = withPage(SMALL_PAGE);
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts())
                .marginLessContext();
        MDCView view = new ViewBuilder().buildView(new MDCParserModelGenerator().parse("A1"), context,
                new JSeshTechRenderContext(FRC, 1.0));
        assertTrue(view.getHeight() < SMALL_PAGE.pageHeight() / 2, "height: " + view.getHeight());
        assertTrue(view.getWidth() < SMALL_PAGE.pageWidth() / 2, "width: " + view.getWidth());
    }
}
