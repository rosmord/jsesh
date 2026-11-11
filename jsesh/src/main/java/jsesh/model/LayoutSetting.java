/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Optional;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// The layout settings shared by the document and by zones.
///
/// Each setting has the same name, and the same values, in the options of a
/// zone (`%zone[linegap=50](...)`) and in the header of a document
/// (`++JSesh_linegap 50 +s`). This enum is the only place where these names are
/// written.
///
/// Lengths are **percents of the unit** of the document, the height of a
/// quadrat (see [jsesh.document.DocumentPreferences#unit()]): `linegap=50` is
/// half a quadrat. The gaps are named by role, and their meaning doesn't
/// depend on the orientation of the text:
///
/// - `linegap`: between two lines;
/// - `columngap`: between two columns;
/// - `quadratgap`: between two quadrats of a line (and between the signs of a
///   quadrat);
/// - `columnquadratgap`: between two quadrats of a column.
///
/// See [TextLayoutSettings] for a set of values.
public enum LayoutSetting {

    /// `lines` or `columns`.
    LAYOUT("layout", Kind.ORIENTATION, TextOrientation.HORIZONTAL),
    /// `lr` or `rl`.
    DIRECTION("dir", Kind.DIRECTION, TextDirection.LEFT_TO_RIGHT),
    /// The maximal height of a quadrat (the height of a line).
    QUADRAT_HEIGHT("quadratheight", Kind.LENGTH, Kind.percentOfDefaultUnit(18)),
    /// The maximal width of a quadrat (the width of a column).
    QUADRAT_WIDTH("quadratwidth", Kind.LENGTH, Kind.percentOfDefaultUnit(22)),
    /// The height of a standard sign (A1).
    SIGN_HEIGHT("signheight", Kind.LENGTH, Kind.percentOfDefaultUnit(18)),
    /// The space between two lines.
    LINE_GAP("linegap", Kind.LENGTH, Kind.percentOfDefaultUnit(6)),
    /// The space between two columns.
    COLUMN_GAP("columngap", Kind.LENGTH, Kind.percentOfDefaultUnit(8)),
    /// The space between two quadrats of a line, and between the signs of a
    /// quadrat.
    QUADRAT_GAP("quadratgap", Kind.LENGTH, Kind.percentOfDefaultUnit(2)),
    /// The space between two quadrats of a column.
    COLUMN_QUADRAT_GAP("columnquadratgap", Kind.LENGTH, Kind.percentOfDefaultUnit(6)),
    /// The thickness of the line of cartouches (and of the frame of zones).
    CARTOUCHE_LINE_WIDTH("cartouchelinewidth", Kind.LENGTH, Kind.percentOfDefaultUnit(1)),
    /// The height of the baseline of alphabetic text above the bottom of the
    /// quadrats.
    TEXT_BASELINE("textbaseline", Kind.LENGTH, Kind.percentOfDefaultUnit(4)),
    /// The space between a quadrat and its underline, when red quadrats are
    /// underlined (see `PaintingSpecifications.underlineRed()`).
    UNDERLINE_GAP("underlinegap", Kind.LENGTH, Kind.percentOfDefaultUnit(2)),
    /// Is a low broad sign alone on a line of a quadrat (as a lone `n` in a
    /// column) enlarged to the quadrat width ("scale to column width")?
    FIT_SIGNS("fitsigns", Kind.FLAG, Boolean.TRUE),
    /// In lines, are quadrats scaled down to the maximal quadrat width?
    /// (Columns always limit the width of their quadrats.)
    HONOUR_MAX_QUADRAT_WIDTH("honourmaxquadratwidth", Kind.FLAG, Boolean.TRUE),
    /// When a quadrat is scaled down, is it scaled as a whole, with a single
    /// scale (`yes`)? Otherwise (`no`), each line of the quadrat is first scaled
    /// down to the maximal quadrat width, then the quadrat is scaled down to the
    /// maximal quadrat height if needed.
    UNIFORM_QUADRAT_SCALING("uniformquadratscaling", Kind.FLAG, Boolean.FALSE),
    /// Are small signs centred in their quadrat?
    CENTRE_SMALL_SIGNS("centresmallsigns", Kind.FLAG, Boolean.FALSE),
    /// Retrograde writing: are the signs, and the content of each quadrat,
    /// mirrored? The order of the quadrats along the line is not changed (see
    /// `DIRECTION`). A sign marked with `\` is mirrored back.
    RETROGRADE("retrograde", Kind.FLAG, Boolean.FALSE);

    /// The unit of the default lengths, in points.
    public static final double DEFAULT_UNIT = 18;

    private final String settingName;
    private final Kind kind;
    private final Object documentDefault;

    LayoutSetting(String settingName, Kind kind, Object documentDefault) {
        this.settingName = settingName;
        this.kind = kind;
        this.documentDefault = documentDefault;
    }

    /// @return the name of the setting, in zone options and (after `JSesh_`)
    /// in document headers.
    public String settingName() {
        return settingName;
    }

    /// @return the kind of values of the setting.
    public Kind kind() {
        return kind;
    }

    /// @return the value of the setting in a document which doesn't set it.
    public Object documentDefault() {
        return documentDefault;
    }

    /// Reads a value of this setting.
    ///
    /// @param text the value, as written in zone options or in a header.
    /// @return the value, or empty if the text is not a valid value.
    public Optional<Object> parse(String text) {
        return Optional.ofNullable(kind.parse(text));
    }

    /// Writes a value of this setting, as [#parse(String)] reads it.
    ///
    /// @param value a value of the right kind.
    /// @return the text of the value.
    public String format(Object value) {
        return kind.format(value);
    }

    /// Reads a yes/no value, as the values of the [Kind#FLAG] settings.
    ///
    /// @param text the value (`yes`, `no`, `true`, `false`, `1`, `0`).
    /// @return the value, or empty if the text is not a valid flag.
    public static Optional<Boolean> parseFlag(String text) {
        return Optional.ofNullable((Boolean) Kind.FLAG.parse(text));
    }

    /// The setting with a given name.
    ///
    /// @param name the name, as in zone options.
    /// @return the setting, or empty if there is none with this name.
    public static Optional<LayoutSetting> byName(String name) {
        for (LayoutSetting setting : values()) {
            if (setting.settingName.equals(name)) {
                return Optional.of(setting);
            }
        }
        return Optional.empty();
    }

    /// The kinds of values of the settings.
    public enum Kind {
        /// A [Double], in percents of the unit. Written as a decimal number.
        LENGTH(Double.class),
        /// A [Boolean]. Written `yes` or `no` (`true`/`false` and `1`/`0` are
        /// also read).
        FLAG(Boolean.class),
        /// A [TextOrientation]. Written `lines` or `columns`.
        ORIENTATION(TextOrientation.class),
        /// A [TextDirection]. Written `lr` or `rl`.
        DIRECTION(TextDirection.class);

        private final Class<?> valueClass;

        Kind(Class<?> valueClass) {
            this.valueClass = valueClass;
        }

        /// @return the class of the values of this kind.
        public Class<?> valueClass() {
            return valueClass;
        }

        private static double percentOfDefaultUnit(double points) {
            return points / 18 * 100;
        }

        private Object parse(String text) {
            String t = text.trim().toLowerCase(Locale.ROOT);
            switch (this) {
                case LENGTH:
                    try {
                        double d = Double.parseDouble(t);
                        return Double.isFinite(d) && d >= 0 ? d : null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                case FLAG:
                    return switch (t) {
                        case "yes", "true", "1" -> Boolean.TRUE;
                        case "no", "false", "0" -> Boolean.FALSE;
                        default -> null;
                    };
                case ORIENTATION:
                    return switch (t) {
                        case "lines" -> TextOrientation.HORIZONTAL;
                        case "columns" -> TextOrientation.VERTICAL;
                        default -> null;
                    };
                case DIRECTION:
                    return switch (t) {
                        case "lr" -> TextDirection.LEFT_TO_RIGHT;
                        case "rl" -> TextDirection.RIGHT_TO_LEFT;
                        default -> null;
                    };
                default:
                    throw new IllegalStateException();
            }
        }

        private String format(Object value) {
            return switch (this) {
                case LENGTH -> formatLength((Double) value);
                case FLAG -> ((Boolean) value) ? "yes" : "no";
                case ORIENTATION -> ((TextOrientation) value).isHorizontal() ? "lines" : "columns";
                case DIRECTION -> ((TextDirection) value).isLeftToRight() ? "lr" : "rl";
            };
        }

        /// Six decimals: enough for the lengths of old documents, converted
        /// from points, to come back to the same points.
        private static String formatLength(double value) {
            BigDecimal d = BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
            return d.scale() < 0 ? d.setScale(0).toPlainString() : d.toPlainString();
        }
    }
}
