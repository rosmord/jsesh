/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.style;

import java.util.Map;

import jsesh.model.LayoutSetting;
import jsesh.model.TextLayoutSettings;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// The correspondence between the [LayoutSetting]s and the fields of a
/// [JSeshStyle], for the document preferences and for zones.
///
/// It is the only place where it is written, so that a setting means the same
/// thing in a document and in a zone.
public final class LayoutSettingsApplier {

    private LayoutSettingsApplier() {
    }

    /// Converts a length, in percents of the unit, to points.
    ///
    /// The result is rounded to 1e-4 point, so that a length typed as a round
    /// percent of the unit gives a round number of points (1.2, and not
    /// 1.2000000000000002).
    ///
    /// @param unit the unit, in points.
    /// @param percent the length, in percents of the unit.
    /// @return the length, in points.
    public static double points(double unit, double percent) {
        return (Math.round(unit * percent * 100) / 1e4);
    }

    /// Applies the settings which are set to a style.
    ///
    /// @param settings the settings (the unset ones don't change the style).
    /// @param unit the unit of the lengths, in points.
    /// @param style the style to modify.
    /// @return the style, for chaining.
    public static JSeshStyle.Builder apply(TextLayoutSettings settings, double unit, JSeshStyle.Builder style) {
        for (Map.Entry<LayoutSetting, Object> e : settings.asMap().entrySet()) {
            Object value = e.getValue();
            switch (e.getKey()) {
                case LAYOUT -> style.options(o -> o.textOrientation((TextOrientation) value));
                case DIRECTION -> style.options(o -> o.textDirection((TextDirection) value));
                case FIT_SIGNS -> style.options(o -> o.fitLoneSigns((Boolean) value));
                case HONOUR_MAX_QUADRAT_WIDTH -> style.options(o -> o.honourMaxQuadratWidthInLines((Boolean) value));
                case UNIFORM_QUADRAT_SCALING -> style.options(o -> o.uniformQuadratScaling((Boolean) value));
                case CENTRE_SMALL_SIGNS -> style.options(o -> o.smallSignCentered((Boolean) value));
                case RETROGRADE -> style.options(o -> o.retrograde((Boolean) value));
                default -> {
                    double p = points(unit, (Double) value);
                    style.geometry(g -> switch (e.getKey()) {
                        case QUADRAT_HEIGHT -> g.maxCadratHeight(p);
                        case QUADRAT_WIDTH -> g.maxCadratWidth(p);
                        case SIGN_HEIGHT -> g.standardSignHeight(p);
                        case LINE_GAP -> g.lineSkip(p);
                        case COLUMN_GAP -> g.columnSkip(p);
                        case QUADRAT_GAP -> g.smallSkip(p);
                        case COLUMN_QUADRAT_GAP -> g.columnQuadratSkip(p);
                        case CARTOUCHE_LINE_WIDTH -> g.cartoucheLineWidth(p);
                        case TEXT_BASELINE -> g.alphabeticTextBaseline(p);
                        case UNDERLINE_GAP -> g.underlineGap(p);
                        default -> throw new IllegalStateException("Not a length: " + e.getKey());
                    });
                }
            }
        }
        return style;
    }

    /// The settings which describe a style: the converse of
    /// [#apply(TextLayoutSettings, double, JSeshStyle.Builder)].
    ///
    /// @param style the style.
    /// @param unit the unit in which the lengths are expressed, in points.
    /// @return all the settings.
    public static TextLayoutSettings settingsOf(JSeshStyle style, double unit) {
        GeometrySpecification g = style.geometry();
        LayoutOptions o = style.options();
        TextLayoutSettings result = TextLayoutSettings.INHERIT_ALL;
        for (LayoutSetting setting : LayoutSetting.values()) {
            Object value = switch (setting) {
                case LAYOUT -> o.textOrientation();
                case DIRECTION -> o.textDirection();
                case FIT_SIGNS -> o.fitLoneSigns();
                case HONOUR_MAX_QUADRAT_WIDTH -> o.honourMaxQuadratWidthInLines();
                case UNIFORM_QUADRAT_SCALING -> o.uniformQuadratScaling();
                case CENTRE_SMALL_SIGNS -> o.smallSignCentered();
                case RETROGRADE -> o.retrograde();
                case QUADRAT_HEIGHT -> percent(g.maxCadratHeight(), unit);
                case QUADRAT_WIDTH -> percent(g.maxCadratWidth(), unit);
                case SIGN_HEIGHT -> percent(g.standardSignHeight(), unit);
                case LINE_GAP -> percent(g.lineSkip(), unit);
                case COLUMN_GAP -> percent(g.columnSkip(), unit);
                case QUADRAT_GAP -> percent(g.smallSkip(), unit);
                case COLUMN_QUADRAT_GAP -> percent(g.columnQuadratSkip(), unit);
                case CARTOUCHE_LINE_WIDTH -> percent(g.cartoucheLineWidth(), unit);
                case TEXT_BASELINE -> percent(g.alphabeticTextBaseline(), unit);
                case UNDERLINE_GAP -> percent(g.underlineGap(), unit);
            };
            result = result.with(setting, value);
        }
        return result;
    }

    /// The percent of the unit which corresponds to a length in points.
    private static double percent(double points, double unit) {
        return points / unit * 100;
    }
}
