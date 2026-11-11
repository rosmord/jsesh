/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.document;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import jsesh.document.DocumentPreferences;
import jsesh.model.LayoutSetting;
import jsesh.model.TextLayoutSettings;
import jsesh.model.constants.JSeshInfoConstants;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.PageTextPosition;

/// Reads and writes [DocumentPreferences] as the properties of a document header.
/// They wil appear as comments at the start of the document (`++JSesh_name value +s`).
///
/// ## The current format (`JSesh_Info 2.0`)
///
/// - `JSesh_unit`: the unit of the document (the height of a quadrat), in
///   points;
/// - `JSesh_` followed by the name of each [LayoutSetting], with the same
///   values as in zone options: `JSesh_linegap 33.333333`,
///   `JSesh_layout columns`, `JSesh_fitsigns yes`. Lengths are percents of
///   the unit;
/// - `JSesh_smallbodylimit` (in points) and `JSesh_shading` (`lines` or
///   `gray`);
/// - the page, in points (`JSesh_page_width`...), only when there is one.
///
/// ## Older headers
///
/// A header without `JSesh_unit` is an old one, with every length in points
/// (`JSesh_line_skip`, `JSesh_max_quadrantHeight`...). It is converted
/// without loss: its maximal quadrat height becomes the unit, and the other
/// lengths are divided by it. Its line skip, which was also the space between
/// the quadrats of a column, gives both `linegap` and `columnquadratgap`.
///
/// While [#WRITE_LEGACY_KEYS] is true, the old properties are written too, so
/// that older versions of JSesh can still read the layout of a new file. A
/// new property always takes precedence over an old one.
public final class DocumentHeaderCodec {

    /// Write the properties of the old format as well, for older versions of
    /// JSesh? To be set to false (and the code removed) after a transition
    /// period.
    public static final boolean WRITE_LEGACY_KEYS = true;

    /// The version written in `JSesh_Info`.
    public static final String HEADER_VERSION = "2.0";

    private static final String PREFIX = JSeshInfoConstants.JSESH_INFO_PREFIX;
    private static final String UNIT = PREFIX + "unit";
    private static final String SMALL_BODY_LIMIT = PREFIX + "smallbodylimit";
    private static final String SHADING = PREFIX + "shading";
    /// How red signs are written: `underline` or `colour`.
    private static final String RED_SIGNS = PREFIX + "redsigns";

    /// The old property giving the value of a setting, in points for lengths.
    private static final Map<LayoutSetting, String> LEGACY_KEYS = new EnumMap<>(LayoutSetting.class);
    static {
        LEGACY_KEYS.put(LayoutSetting.LAYOUT, JSeshInfoConstants.JSESH_PAGE_ORIENTATION);
        LEGACY_KEYS.put(LayoutSetting.DIRECTION, JSeshInfoConstants.JSESH_PAGE_DIRECTION);
        LEGACY_KEYS.put(LayoutSetting.QUADRAT_HEIGHT, JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT);
        LEGACY_KEYS.put(LayoutSetting.QUADRAT_WIDTH, JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH);
        LEGACY_KEYS.put(LayoutSetting.SIGN_HEIGHT, JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT);
        LEGACY_KEYS.put(LayoutSetting.LINE_GAP, JSeshInfoConstants.JSESH_LINE_SKIP);
        LEGACY_KEYS.put(LayoutSetting.COLUMN_GAP, JSeshInfoConstants.JSESH_COLUMN_SKIP);
        LEGACY_KEYS.put(LayoutSetting.QUADRAT_GAP, JSeshInfoConstants.JSESH_SMALL_SKIP);
        LEGACY_KEYS.put(LayoutSetting.COLUMN_QUADRAT_GAP, JSeshInfoConstants.JSESH_LINE_SKIP);
        LEGACY_KEYS.put(LayoutSetting.CARTOUCHE_LINE_WIDTH, JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH);
        LEGACY_KEYS.put(LayoutSetting.TEXT_BASELINE, JSeshInfoConstants.JSESH_ALPHABETIC_TEXT_BASELINE);
        LEGACY_KEYS.put(LayoutSetting.FIT_SIGNS, JSeshInfoConstants.JSESH_FIT_LONE_SIGNS);
        LEGACY_KEYS.put(LayoutSetting.HONOUR_MAX_QUADRAT_WIDTH, JSeshInfoConstants.JSESH_LIMIT_QUADRAT_WIDTH_IN_LINES);
        LEGACY_KEYS.put(LayoutSetting.CENTRE_SMALL_SIGNS, JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED);
    }

    private DocumentHeaderCodec() {
    }

    /// @param setting a setting.
    /// @return the name of its property in a header.
    public static String headerKey(LayoutSetting setting) {
        return PREFIX + setting.settingName();
    }

    /// Reads preferences from header properties, of the current format or of
    /// an older one. Unknown properties and invalid values are ignored;
    /// missing ones get their default value.
    ///
    /// @param map the properties, by name (`JSesh_linegap`...).
    /// @return the preferences.
    public static DocumentPreferences fromHeader(Map<String, String> map) {
        boolean legacy = !map.containsKey(UNIT);
        double unit = legacy
                ? positive(map.get(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT), LayoutSetting.DEFAULT_UNIT)
                : positive(map.get(UNIT), LayoutSetting.DEFAULT_UNIT);
        TextLayoutSettings settings = TextLayoutSettings.INHERIT_ALL;
        for (LayoutSetting setting : LayoutSetting.values()) {
            Object value = read(setting, map.get(headerKey(setting)));
            // Settings introduced after the legacy format have no legacy key.
            if (value == null && legacy && LEGACY_KEYS.containsKey(setting)) {
                value = readLegacy(setting, map.get(LEGACY_KEYS.get(setting)), unit);
            }
            settings = settings.with(setting, value);
        }
        double smallBodyLimit = positive(map.containsKey(SMALL_BODY_LIMIT) ? map.get(SMALL_BODY_LIMIT)
                : map.get(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT),
                DocumentPreferences.DEFAULT_SMALL_BODY_SCALE_LIMIT);
        boolean lines;
        if (map.containsKey(SHADING)) {
            lines = "lines".equals(map.get(SHADING).trim());
        } else {
            lines = Boolean.TRUE.equals(flag(map.get(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING)));
        }
        boolean underlineRed = map.containsKey(RED_SIGNS) && "underline".equals(map.get(RED_SIGNS).trim());
        return new DocumentPreferences(unit, settings, smallBodyLimit, lines, underlineRed,
                number(map.get(JSeshInfoConstants.JSESH_PAGE_WIDTH), 0),
                number(map.get(JSeshInfoConstants.JSESH_PAGE_HEIGHT), 0),
                number(map.get(JSeshInfoConstants.JSESH_PAGE_MARGIN_LEFT), 0),
                number(map.get(JSeshInfoConstants.JSESH_PAGE_MARGIN_RIGHT), 0),
                number(map.get(JSeshInfoConstants.JSESH_PAGE_MARGIN_TOP), 0),
                number(map.get(JSeshInfoConstants.JSESH_PAGE_MARGIN_BOTTOM), 0),
                position(map.get(JSeshInfoConstants.JSESH_PAGE_NUMBER_POSITION)),
                position(map.get(JSeshInfoConstants.JSESH_PAGE_TITLE_POSITION)),
                Boolean.TRUE.equals(flag(map.get(JSeshInfoConstants.JSESH_PAGE_TEXTS_ALTERNATE))),
                map.getOrDefault(JSeshInfoConstants.JSESH_PAGE_TITLE, ""));
    }

    /// Writes preferences as header properties, sorted by name. The page
    /// properties are only written when there is a page, and its texts when
    /// there are some.
    ///
    /// @param preferences the preferences.
    /// @return the properties, by name.
    public static Map<String, String> toHeader(DocumentPreferences preferences) {
        TreeMap<String, String> result = new TreeMap<>();
        result.put(UNIT, LayoutSetting.QUADRAT_HEIGHT.format(preferences.unit()));
        for (Map.Entry<LayoutSetting, Object> e : preferences.settings().asMap().entrySet()) {
            result.put(headerKey(e.getKey()), e.getKey().format(e.getValue()));
        }
        result.put(SMALL_BODY_LIMIT, LayoutSetting.QUADRAT_HEIGHT.format(preferences.smallBodyScaleLimit()));
        result.put(SHADING, preferences.useLinesForShading() ? "lines" : "gray");
        result.put(RED_SIGNS, preferences.useUnderlineForRed() ? "underline" : "colour");
        if (WRITE_LEGACY_KEYS) {
            putLegacyKeys(preferences, result);
        }
        if (preferences.hasPage()) {
            result.put(JSeshInfoConstants.JSESH_PAGE_WIDTH, Double.toString(preferences.pageWidth()));
            result.put(JSeshInfoConstants.JSESH_PAGE_HEIGHT, Double.toString(preferences.pageHeight()));
            result.put(JSeshInfoConstants.JSESH_PAGE_MARGIN_LEFT, Double.toString(preferences.marginLeft()));
            result.put(JSeshInfoConstants.JSESH_PAGE_MARGIN_RIGHT, Double.toString(preferences.marginRight()));
            result.put(JSeshInfoConstants.JSESH_PAGE_MARGIN_TOP, Double.toString(preferences.marginTop()));
            result.put(JSeshInfoConstants.JSESH_PAGE_MARGIN_BOTTOM, Double.toString(preferences.marginBottom()));
            if (preferences.hasPageTexts()) {
                result.put(JSeshInfoConstants.JSESH_PAGE_NUMBER_POSITION, preferences.pageNumberPosition().name());
                result.put(JSeshInfoConstants.JSESH_PAGE_TITLE_POSITION, preferences.pageTitlePosition().name());
                if (preferences.pageTextsAlternate()) {
                    result.put(JSeshInfoConstants.JSESH_PAGE_TEXTS_ALTERNATE, "true");
                }
                if (!preferences.pageTitle().isEmpty()) {
                    // A header entry is a single line.
                    result.put(JSeshInfoConstants.JSESH_PAGE_TITLE,
                            preferences.pageTitle().replaceAll("[\\r\\n]+", " "));
                }
            }
        }
        return result;
    }

    /// The properties of the old format, as older versions write them.
    private static void putLegacyKeys(DocumentPreferences p, Map<String, String> result) {
        for (LayoutSetting setting : LayoutSetting.values()) {
            String key = LEGACY_KEYS.get(setting);
            if (key == null) {
                continue; // A setting which is more recent than the old format.
            }
            if (setting == LayoutSetting.COLUMN_QUADRAT_GAP) {
                continue; // The old format has a single line skip, the line gap's.
            }
            Object value = p.settings().get(setting);
            String text = switch (setting.kind()) {
                case LENGTH -> Double.toString(decimal(p.points(setting)));
                case FLAG -> value.toString();
                case ORIENTATION -> ((TextOrientation) value).name();
                case DIRECTION -> ((TextDirection) value).name();
            };
            result.put(key, text);
        }
        result.put(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT, Double.toString(p.smallBodyScaleLimit()));
        result.put(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, Boolean.toString(p.useLinesForShading()));
    }

    /// Rounds to six decimals, so that a length converted back to points is
    /// written "6.0" and not "5.999999999".
    private static double decimal(double value) {
        return Math.round(value * 1e6) / 1e6;
    }

    private static Object read(LayoutSetting setting, String text) {
        return text == null ? null : setting.parse(text).orElse(null);
    }

    private static Object readLegacy(LayoutSetting setting, String text, double unit) {
        switch (setting.kind()) {
            case LENGTH: {
                // A missing length had its old default value, in points.
                double defaultPoints = (Double) setting.documentDefault() * LayoutSetting.DEFAULT_UNIT / 100;
                double points = number(text, defaultPoints);
                return points / unit * 100;
            }
            case FLAG:
                return text == null ? null : flag(text);
            case ORIENTATION:
                return enumValue(TextOrientation.class, text);
            case DIRECTION:
                return enumValue(TextDirection.class, text);
            default:
                throw new IllegalStateException();
        }
    }

    private static Boolean flag(String text) {
        return text == null ? null
                : (Boolean) LayoutSetting.FIT_SIGNS.parse(text).orElse(null);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String text) {
        if (text == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, text.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static PageTextPosition position(String text) {
        PageTextPosition p = enumValue(PageTextPosition.class, text);
        return p == null ? PageTextPosition.NONE : p;
    }

    private static double number(String text, double defaultValue) {
        if (text == null) {
            return defaultValue;
        }
        try {
            double d = Double.parseDouble(text.trim());
            return Double.isFinite(d) ? d : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double positive(String text, double defaultValue) {
        double d = number(text, defaultValue);
        return d > 0 ? d : defaultValue;
    }
}
