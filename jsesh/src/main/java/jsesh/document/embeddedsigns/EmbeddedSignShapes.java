/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jsesh.glyphs.shape.LigatureZoneBuilder;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.signsource.SVGSignSource;

/// Conversions between the SVG of an [EmbeddedSign] and drawable shapes.
public final class EmbeddedSignShapes {

    private EmbeddedSignShapes() {
    }

    /// Reads a user SVG file and returns its normalized form.
    ///
    /// The SVG is read as a sign of the user's sign folder would be (a sign
    /// 1800 units high is scaled down to 18), then written back as one path
    /// with its ligature zones. A sign without ligature zones gets the ones
    /// JSesh computes for it. The result is what [EmbeddedSign#svg()] holds.
    ///
    /// @param in         the SVG data.
    /// @param sourceName a name for error messages (usually the file name).
    /// @return the normalized SVG.
    /// @throws IOException if the SVG can't be read or holds no drawing.
    public static String normalizeSvg(InputStream in, String sourceName) throws IOException {
        SVGSignSource source = new SVGSignSource(sourceName, in);
        // SVGSignSource doesn't throw: on error, it renames its code "Error ...".
        String code = source.getCurrentCode();
        ShapeChar shape = source.getCurrentShape();
        if ((code != null && code.startsWith("Error ")) || shape == null || shape.getShape() == null
                || shape.getBbox().isEmpty()) {
            throw new IOException("Can't read a sign drawing in " + sourceName);
        }
        if (!shape.hasZones()) {
            // Store the zones JSesh would compute, so that every reader (the
            // JavaScript port in particular) uses the same ones.
            LigatureZoneBuilder builder = new LigatureZoneBuilder(shape);
            for (int k = 0; k < 3; k++) {
                shape.setZone(k, builder.getLigatureArea(k));
            }
        }
        return toSvg(shape);
    }

    /// The normalized SVG of a shape.
    public static String toSvg(ShapeChar shape) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            shape.exportToSVG(out, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e); // Can't happen on a byte array.
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    /// The shape drawn by a normalized SVG.
    ///
    /// @param code the sign code, for error messages.
    /// @param svg  the SVG text.
    /// @return the shape; a small square if the SVG can't be read (as for the
    ///         signs of the user's folder).
    public static ShapeChar toShape(String code, String svg) {
        return new SVGSignSource(code, new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)))
                .getCurrentShape();
    }
}
