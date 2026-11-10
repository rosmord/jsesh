/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

import jsesh.glyphs.shape.LigatureZone;
import jsesh.glyphs.shape.LigatureZoneBuilder;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.signsource.SVGSignSource;

/// Reading the SVG of an [EmbeddedSign].
///
/// The SVG is the user's file, kept as is. It is read exactly as a sign of
/// the user's sign folder would be ([SVGSignSource]: a sign 1800 units high
/// is scaled down to 18, the shape starts at 0,0; ligature zones are computed
/// by JSesh when the file has none).
public final class EmbeddedSignShapes {

    /// Decimals kept in [#renderingData].
    private static final int PRECISION = 4;

    private EmbeddedSignShapes() {
    }

    /// Checks that some SVG data holds a sign drawing.
    ///
    /// @param svg        the SVG data.
    /// @param sourceName a name for error messages (usually the file name).
    /// @throws IOException if the SVG can't be read or holds no drawing.
    public static void checkSvg(byte[] svg, String sourceName) throws IOException {
        SVGSignSource source = new SVGSignSource(sourceName, new ByteArrayInputStream(svg));
        // SVGSignSource doesn't throw: on error, it renames its code "Error ...".
        String code = source.getCurrentCode();
        ShapeChar shape = source.getCurrentShape();
        if ((code != null && code.startsWith("Error ")) || shape == null || shape.getShape() == null
                || shape.getBbox().isEmpty()) {
            throw new IOException("Can't read a sign drawing in " + sourceName);
        }
    }

    /// The shape drawn by some SVG data.
    ///
    /// @param code the sign code, for error messages.
    /// @param svg  the SVG data.
    /// @return the shape; a small square if the SVG can't be read (as for the
    ///         signs of the user's folder).
    public static ShapeChar toShape(String code, byte[] svg) {
        return new SVGSignSource(code, new ByteArrayInputStream(svg)).getCurrentShape();
    }

    /// The drawing of a sign as JSesh uses it, as a small JSON object:
    /// `{"w":…,"h":…,"d":"M …","zones":[[x,y,w,h,"sc"],null,…]}`.
    ///
    /// `w` and `h` are the size of the sign, `d` its outline (starting at
    /// 0,0), and `zones` its three ligature zones, with their gravity (`s`,
    /// `c` or `e`, then `t`, `c` or `b`) — the ones JSesh computes when the
    /// file has none. This is the format of the glyph data of the JavaScript
    /// port, which can't read arbitrary SVG; it is stored in the header next
    /// to the SVG, which remains the reference.
    public static String renderingData(String code, byte[] svg) {
        ShapeChar shape = toShape(code, svg);
        if (!shape.hasZones()) {
            LigatureZoneBuilder builder = new LigatureZoneBuilder(shape);
            for (int k = 0; k < 3; k++) {
                shape.setZone(k, builder.getLigatureArea(k));
            }
        }
        StringWriter d = new StringWriter();
        try {
            shape.writeSVGPath(d, PRECISION);
        } catch (IOException e) {
            throw new RuntimeException(e); // Can't happen on a StringWriter.
        }
        Rectangle2D bbox = shape.getBbox();
        StringBuilder b = new StringBuilder("{\"w\":").append(number(bbox.getWidth()))
                .append(",\"h\":").append(number(bbox.getHeight()))
                .append(",\"d\":\"").append(d).append("\",\"zones\":[");
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                b.append(',');
            }
            LigatureZone z = shape.getZone(i);
            if (z == null) {
                b.append("null");
            } else {
                b.append('[').append(number(z.getMinX())).append(',').append(number(z.getMinY())).append(',')
                        .append(number(z.getWidth())).append(',').append(number(z.getHeight())).append(",\"")
                        .append(z.getHorizontalGravity().getCode()).append(z.getVerticalGravity().getCode())
                        .append("\"]");
            }
        }
        return b.append("]}").toString();
    }

    /// A number with at most [#PRECISION] decimals, in JSON syntax.
    private static String number(double v) {
        return BigDecimal.valueOf(v).setScale(PRECISION, RoundingMode.HALF_UP).stripTrailingZeros()
                .toPlainString();
    }
}
