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
/// Every setting is optional: an unset one is inherited from the enclosing
/// zone (or, for the outermost zones, from the document preferences).
///
/// Lengths are integers, in **percent of the height of a quadrat**, as the
/// `dw`/`dh` of [Insert]: `width=1200` is twelve quadrats wide.
///
/// @param orientation the layout: [TextOrientation#HORIZONTAL] for lines,
///        [TextOrientation#VERTICAL] for columns; `null` if inherited.
/// @param direction the writing direction; `null` if inherited.
/// @param width the fixed width of the zone, or [#UNSET] for its natural
///        width. For a zone in lines, it is also the width at which its lines
///        wrap.
/// @param height the fixed height of the zone, or [#UNSET] for its natural
///        height.
/// @param lineSkip the space between lines, or [#UNSET] if inherited.
/// @param columnSkip the space between columns, or [#UNSET] if inherited.
/// @param frame should a frame be drawn around the zone?
public record ZoneParameters(TextOrientation orientation, TextDirection direction,
        int width, int height, int lineSkip, int columnSkip, boolean frame) implements Serializable {

    /// The value of an unset length.
    public static final int UNSET = -1;

    /// Everything inherited, natural size, no frame.
    public static final ZoneParameters DEFAULT = new ZoneParameters(null, null, UNSET, UNSET, UNSET, UNSET, false);

    private static final Comparator<ZoneParameters> ORDER = Comparator
            .comparing(ZoneParameters::orientation, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ZoneParameters::direction, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparingInt(ZoneParameters::width)
            .thenComparingInt(ZoneParameters::height)
            .thenComparingInt(ZoneParameters::lineSkip)
            .thenComparingInt(ZoneParameters::columnSkip)
            .thenComparing(ZoneParameters::frame);

    /// Normalises the lengths: a size of 0 or less, or a negative skip, is
    /// [#UNSET].
    public ZoneParameters {
        width = width > 0 ? width : UNSET;
        height = height > 0 ? height : UNSET;
        lineSkip = lineSkip >= 0 ? lineSkip : UNSET;
        columnSkip = columnSkip >= 0 ? columnSkip : UNSET;
    }

    public ZoneParameters withOrientation(TextOrientation orientation) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    public ZoneParameters withDirection(TextDirection direction) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    public ZoneParameters withWidth(int width) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    public ZoneParameters withHeight(int height) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    public ZoneParameters withLineSkip(int lineSkip) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    public ZoneParameters withColumnSkip(int columnSkip) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    public ZoneParameters withFrame(boolean frame) {
        return new ZoneParameters(orientation, direction, width, height, lineSkip, columnSkip, frame);
    }

    /// An arbitrary but consistent order, for [ModelElement#compareTo].
    public int compareTo(ZoneParameters other) {
        return ORDER.compare(this, other);
    }
}
