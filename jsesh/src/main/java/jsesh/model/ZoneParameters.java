/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.io.Serializable;
import java.util.Comparator;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// The settings of a [Zone], as written in `%zone[...]`.
///
/// They are of two sorts:
///
/// - the [TextLayoutSettings], shared with the document preferences
///   (`layout`, `dir`, `linegap`, `fitsigns`…, see [LayoutSetting]). An unset
///   one is inherited from the enclosing zone (or, for the outermost zones,
///   from the document preferences);
/// - the settings which only make sense for a zone: its scale, its size, its
///   tab gap, its separators and its frame.
///
/// Lengths are in **percent of the unit** of the document (the height of a
/// quadrat), as the `dw`/`dh` of [Insert]: `width=1200` is twelve quadrats
/// wide. The lengths of a zone with a `scale` are in its own quadrats: with
/// `scale=50`, `width=1200` is twelve half-sized quadrats.
///
/// @param settings the layout settings shared with the document.
/// @param scale the scale of the content of the zone, in percent (signs,
///        quadrats, gaps... everything is scaled), or [#UNSET] for none
///        (100 %).
/// @param width the fixed width of the zone, or [#UNSET] for its natural
///        width. For a zone in lines, it is also the width at which its lines
///        wrap.
/// @param height the fixed height of the zone, or [#UNSET] for its natural
///        height.
/// @param tabGap the minimal space between the text of a tab cell and the
///        next tab stop (see [Tab]), or [#UNSET] for the quadrat gap.
/// @param separator the thickness of the rules drawn between the lines (for a
///        zone in lines) or the columns (for a zone in columns), or [#UNSET]
///        for no rule.
/// @param frame should a frame be drawn around the zone?
public record ZoneParameters(TextLayoutSettings settings, int scale,
        int width, int height, int tabGap, int separator, boolean frame)
        implements Serializable {

    /// The value of an unset length.
    public static final int UNSET = -1;

    /// Everything inherited, natural size, no frame.
    public static final ZoneParameters DEFAULT = new ZoneParameters(TextLayoutSettings.INHERIT_ALL,
            UNSET, UNSET, UNSET, UNSET, UNSET, false);

    private static final Comparator<ZoneParameters> ORDER = Comparator
            .comparing(ZoneParameters::settings)
            .thenComparingInt(ZoneParameters::scale)
            .thenComparingInt(ZoneParameters::width)
            .thenComparingInt(ZoneParameters::height)
            .thenComparingInt(ZoneParameters::tabGap)
            .thenComparingInt(ZoneParameters::separator)
            .thenComparing(ZoneParameters::frame);

    /// Normalises the lengths: a scale, a size or a separator of 0 or less, or
    /// a negative gap, is [#UNSET]; a scale of 100 is [#UNSET] too.
    public ZoneParameters {
        settings = settings == null ? TextLayoutSettings.INHERIT_ALL : settings;
        scale = scale > 0 && scale != 100 ? scale : UNSET;
        width = width > 0 ? width : UNSET;
        height = height > 0 ? height : UNSET;
        tabGap = tabGap >= 0 ? tabGap : UNSET;
        separator = separator > 0 ? separator : UNSET;
    }

    /// @return the orientation of the zone, or null if inherited.
    public TextOrientation orientation() {
        return settings.orientation();
    }

    /// @return the direction of the zone, or null if inherited.
    public TextDirection direction() {
        return settings.direction();
    }

    /// @return the scale of the content, as a factor (1 for no scale).
    public float scaleFactor() {
        return scale == UNSET ? 1f : scale / 100f;
    }

    public ZoneParameters withSettings(TextLayoutSettings settings) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    /// @param setting a shared layout setting.
    /// @param value its value (null for inherited).
    /// @return a copy with the new value.
    public ZoneParameters with(LayoutSetting setting, Object value) {
        return withSettings(settings.with(setting, value));
    }

    public ZoneParameters withOrientation(TextOrientation orientation) {
        return withSettings(settings.withOrientation(orientation));
    }

    public ZoneParameters withDirection(TextDirection direction) {
        return withSettings(settings.withDirection(direction));
    }

    public ZoneParameters withScale(int scale) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    public ZoneParameters withWidth(int width) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    public ZoneParameters withHeight(int height) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    public ZoneParameters withTabGap(int tabGap) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    public ZoneParameters withSeparator(int separator) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    public ZoneParameters withFrame(boolean frame) {
        return new ZoneParameters(settings, scale, width, height, tabGap, separator, frame);
    }

    /// An arbitrary but consistent order, for [ModelElement#compareTo].
    public int compareTo(ZoneParameters other) {
        return ORDER.compare(this, other);
    }
}
