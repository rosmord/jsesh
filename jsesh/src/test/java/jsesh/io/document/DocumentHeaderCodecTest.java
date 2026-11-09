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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jsesh.document.DocumentPreferences;
import jsesh.model.LayoutSetting;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.GeometrySpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.LayoutOptions;
import jsesh.render.style.ShadingMode;

/// The document header: current format, and conversion of older headers.
public class DocumentHeaderCodecTest {

    /// A header written by an older version, with non-default values.
    private static Map<String, String> legacyHeader() {
        Map<String, String> map = new HashMap<>();
        map.put("JSesh_page_orientation", "VERTICAL");
        map.put("JSesh_page_direction", "RIGHT_TO_LEFT");
        map.put("JSesh_max_quadrantHeight", "24.0");
        map.put("JSesh_max_quadrant_width", "30.0");
        map.put("JSesh_standard_sign_height", "20.0");
        map.put("JSesh_line_skip", "7.0");
        map.put("JSesh_column_skip", "9.5");
        map.put("JSesh_small_skip", "1.5");
        map.put("JSesh_cartouche_line_width", "1.2");
        map.put("JSesh_alphabetic_text_baseline", "3.0");
        map.put("JSesh_small_body_scale_limit", "10.0");
        map.put("JSesh_small_sign_centered", "1");
        map.put("JSesh_use_lines_for_shading", "true");
        map.put("JSesh_limit_quadrat_width_in_lines", "false");
        map.put("JSesh_fit_lone_signs", "false");
        return map;
    }

    @Test
    public void anOldHeaderGivesTheSameStyle() {
        DocumentPreferences prefs = DocumentHeaderCodec.fromHeader(legacyHeader());
        assertEquals(24, prefs.unit(), 1e-9);
        assertEquals(100, prefs.percent(LayoutSetting.QUADRAT_HEIGHT), 1e-9);

        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(prefs, JSeshStyle.DEFAULT);
        GeometrySpecification g = style.geometry();
        LayoutOptions o = style.options();
        assertEquals(24.0, g.maxCadratHeight());
        assertEquals(30.0, g.maxCadratWidth());
        assertEquals(20.0, g.standardSignHeight());
        assertEquals(7.0, g.lineSkip());
        // The old line skip was also the space between the quadrats of a column.
        assertEquals(7.0, g.columnQuadratSkip());
        assertEquals(9.5, g.columnSkip());
        assertEquals(1.5, g.smallSkip());
        assertEquals(1.2, g.cartoucheLineWidth());
        assertEquals(3.0, g.alphabeticTextBaseline());
        assertEquals(10.0, g.smallBodyScaleLimit());
        assertEquals(24.0, g.unit());
        assertEquals(TextOrientation.VERTICAL, o.textOrientation());
        assertEquals(TextDirection.RIGHT_TO_LEFT, o.textDirection());
        assertTrue(o.smallSignCentered());
        assertFalse(o.limitQuadratWidthInLines());
        assertFalse(o.fitLoneSigns());
        assertEquals(ShadingMode.LINE_HATCHING, style.painting().shadingStyle());
    }

    /// `textWidth` (the length of the rules) used to be a style parameter. It is gone, and a header
    /// which still has it is read as if it didn't.
    @Test
    public void theObsoleteTextWidthIsIgnored() {
        Map<String, String> map = new HashMap<>();
        map.put("textWidth", "600");
        map.put("JSesh_text_width", "600");
        DocumentPreferences prefs = DocumentHeaderCodec.fromHeader(map);
        assertEquals(DocumentHeaderCodec.fromHeader(new HashMap<>()), prefs);
    }

    @Test
    public void anEmptyHeaderGivesTheOldDefaults() {
        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(
                DocumentHeaderCodec.fromHeader(new HashMap<>()), JSeshStyle.DEFAULT);
        GeometrySpecification g = style.geometry();
        assertEquals(18.0, g.maxCadratHeight());
        assertEquals(22.0, g.maxCadratWidth());
        assertEquals(18.0, g.standardSignHeight());
        assertEquals(6.0, g.lineSkip());
        assertEquals(6.0, g.columnQuadratSkip());
        assertEquals(8.0, g.columnSkip());
        assertEquals(2.0, g.smallSkip());
        assertEquals(1.0, g.cartoucheLineWidth());
        assertEquals(4.0, g.alphabeticTextBaseline());
        assertTrue(style.options().fitLoneSigns());
        assertTrue(style.options().limitQuadratWidthInLines());
    }

    @Test
    public void missingOldLengthsKeepTheirOldValueInPoints() {
        // Before, a missing column skip was 8 points, whatever the quadrat height.
        Map<String, String> map = new HashMap<>();
        map.put("JSesh_max_quadrantHeight", "36.0");
        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(
                DocumentHeaderCodec.fromHeader(map), JSeshStyle.DEFAULT);
        assertEquals(8.0, style.geometry().columnSkip());
        assertEquals(36.0, style.geometry().maxCadratHeight());
    }

    @Test
    public void theNewFormatUsesTheZoneNames() {
        DocumentPreferences prefs = new DocumentPreferences()
                .with(LayoutSetting.LAYOUT, TextOrientation.VERTICAL)
                .with(LayoutSetting.FIT_SIGNS, false)
                .with(LayoutSetting.LINE_GAP, 50.0);
        Map<String, String> header = DocumentHeaderCodec.toHeader(prefs);
        assertEquals("18", header.get("JSesh_unit"));
        assertEquals("columns", header.get("JSesh_layout"));
        assertEquals("no", header.get("JSesh_fitsigns"));
        assertEquals("50", header.get("JSesh_linegap"));
        assertEquals("33.333333", header.get("JSesh_columnquadratgap"));
        assertEquals("gray", header.get("JSesh_shading"));
        assertFalse(header.containsKey("JSesh_page_width"));
    }

    @Test
    public void theNewFormatRoundTrips() {
        DocumentPreferences prefs = new DocumentPreferences()
                .withUnit(25)
                .with(LayoutSetting.DIRECTION, TextDirection.RIGHT_TO_LEFT)
                .with(LayoutSetting.COLUMN_GAP, 60.0)
                .with(LayoutSetting.COLUMN_QUADRAT_GAP, 12.5)
                .with(LayoutSetting.CENTRE_SMALL_SIGNS, true)
                .withSmallBodyScaleLimit(9)
                .withUseLinesForShading(true)
                .withPage(300, 400, 20, 30, 10, 15);
        assertEquals(prefs, DocumentHeaderCodec.fromHeader(DocumentHeaderCodec.toHeader(prefs)));
    }

    @Test
    public void theOldKeysAreWrittenForOlderVersions() {
        DocumentPreferences prefs = new DocumentPreferences().withUnit(36);
        Map<String, String> header = DocumentHeaderCodec.toHeader(prefs);
        if (DocumentHeaderCodec.WRITE_LEGACY_KEYS) {
            assertEquals("36.0", header.get("JSesh_max_quadrantHeight"));
            assertEquals("12.0", header.get("JSesh_line_skip"));
            assertEquals("HORIZONTAL", header.get("JSesh_page_orientation"));
            assertEquals("true", header.get("JSesh_fit_lone_signs"));
        }
    }

    @Test
    public void newKeysTakePrecedenceOverOldOnes() {
        Map<String, String> map = new HashMap<>(DocumentHeaderCodec.toHeader(
                new DocumentPreferences().with(LayoutSetting.LINE_GAP, 50.0)));
        // As if an older version had changed its own key only.
        map.put("JSesh_line_skip", "2.0");
        assertEquals(50, DocumentHeaderCodec.fromHeader(map).percent(LayoutSetting.LINE_GAP), 1e-9);
    }

    @Test
    public void changingTheUnitScalesEveryLength() {
        DocumentPreferences prefs = new DocumentPreferences().withUnit(36);
        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(prefs, JSeshStyle.DEFAULT);
        assertEquals(36.0, style.geometry().maxCadratHeight());
        assertEquals(44.0, style.geometry().maxCadratWidth());
        assertEquals(12.0, style.geometry().lineSkip());
        assertEquals(4.0, style.geometry().smallSkip());
        assertEquals(36.0, style.geometry().standardSignHeight());
    }

    @Test
    public void aStyleSurvivesTheHeader() {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.lineSkip(6.5).columnQuadratSkip(3.0).maxCadratWidth(25.0))
                .build();
        DocumentPreferences prefs = DocumentPreferencesStyleConverter.toDocumentPreferences(style);
        JSeshStyle back = DocumentPreferencesStyleConverter.applyDocumentPreferences(
                DocumentHeaderCodec.fromHeader(DocumentHeaderCodec.toHeader(prefs)), JSeshStyle.DEFAULT);
        assertEquals(6.5, back.geometry().lineSkip());
        assertEquals(3.0, back.geometry().columnQuadratSkip());
        assertEquals(25.0, back.geometry().maxCadratWidth());
    }
}
