/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.export.pdfExport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;

import jsesh.document.caret.MDCCaret;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;

/// With its layout respected, a PDF takes the page size of a paginated document.
public class PDFPageSizeTest {

    @TempDir
    Path tmp;

    private PdfReader export(String mdc, JSeshStyle style, boolean respectLayout)
            throws MDCSyntaxError, IOException {
        HieroglyphicText model = new MDCParserModelGenerator().parse(mdc);
        PDFExporter exporter = new PDFExporter();
        File file = tmp.resolve("out.pdf").toFile();
        exporter.getPdfExportPreferences().setFile(file);
        exporter.getPdfExportPreferences().setRespectTextLayout(respectLayout);
        exporter.exportModel(model, MDCCaret.buildWholeTextCaret(model),
                new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts()));
        return new PdfReader(file.getPath());
    }

    private static JSeshStyle withPage(PageSpecification page) {
        return JSeshStyle.DEFAULT.copy().geometry(g -> g.page(page)).build();
    }

    @Test
    public void aPaginatedDocumentGivesItsPageSizeToThePdf() throws Exception {
        PdfReader pdf = export("A1-N5-!!-A1", withPage(new PageSpecification(300, 200, 20, 20, 20, 20)), true);
        Rectangle size = pdf.getPageSize(1);
        assertEquals(300, size.getWidth(), 0.01);
        assertEquals(200, size.getHeight(), 0.01);
        assertEquals(2, pdf.getNumberOfPages());
    }

    @Test
    public void withoutPageThePdfPreferencesGiveTheSize() throws Exception {
        PdfReader pdf = export("A1-N5", JSeshStyle.DEFAULT, true);
        Rectangle a4 = new PDFExportPreferences().getPageRectangle();
        assertEquals(a4.getWidth(), pdf.getPageSize(1).getWidth(), 0.01);
        assertEquals(a4.getHeight(), pdf.getPageSize(1).getHeight(), 0.01);
    }

    @Test
    public void respectingTheLayoutIsTheDefault() {
        assertTrue(new PDFExportPreferences().isRespectTextLayout());
    }
}
