/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.document;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsesh.document.embeddedsigns.EmbeddedSign;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.document.embeddedsigns.EmbeddedSignShapes;
import jsesh.model.constants.JSeshInfoConstants;

/// Reads and writes the embedded signs of a document as header properties.
///
/// ```
/// ++JSesh_max_embedded_sign_id 3 +s
/// ++JSesh_embedded_sign_sign1 <variantOf> <version> <description> <svg> <rendering> +s
/// ```
///
/// - `variantOf` is the Gardiner code the sign is a variant of, or `-`;
/// - `version` is the version of the storage format of the sign
///   ([#FORMAT_VERSION]); a sign in another version is skipped when
///   reading, as JSesh doesn't know how its other fields are written;
/// - `description` is the description in UTF-8, base64-encoded, or `-` if
///   it is empty;
/// - `svg` is the user's SVG file, base64-encoded (not compressed, so that
///   it stays easy to recover): the reference;
/// - `rendering` is [EmbeddedSignShapes#renderingData] (size, outline and
///   ligature zones as JSesh reads them from the SVG), base64-encoded. It is written for the JavaScript port, which can't read
///   arbitrary SVG; JSesh recomputes it at each writing and ignores it when
///   reading.
///
/// No value contains spaces, so the line can't be confused with the end of
/// the header. `JSesh_max_embedded_sign_id` is the highest number ever given
/// to a sign of the document; it is written as soon as there is one sign.
/// Older versions of JSesh ignore these properties.
public final class EmbeddedSignsHeaderCodec {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedSignsHeaderCodec.class.getName());

    /// The property holding the highest sign number used.
    public static final String MAX_ID = JSeshInfoConstants.JSESH_INFO_PREFIX + "max_embedded_sign_id";

    /// The prefix of the properties holding one sign; the sign code follows.
    public static final String SIGN_PREFIX = JSeshInfoConstants.JSESH_INFO_PREFIX + "embedded_sign_";

    /// The version of the storage format of a sign, written after
    /// `variantOf`. The fields which follow it depend on it.
    public static final String FORMAT_VERSION = "v0";

    private static final String NONE = "-";

    private EmbeddedSignsHeaderCodec() {
    }

    /// Reads the embedded signs from header properties. Other properties are
    /// ignored; malformed signs are skipped (and logged).
    public static EmbeddedSignList fromHeader(Map<String, String> header) {
        EmbeddedSignList list = new EmbeddedSignList();
        for (Map.Entry<String, String> e : header.entrySet()) {
            if (e.getKey().startsWith(SIGN_PREFIX)) {
                String code = e.getKey().substring(SIGN_PREFIX.length());
                try {
                    list.put(decodeSign(code, e.getValue()));
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, "Malformed embedded sign " + code + " in document header", ex);
                }
            }
        }
        String maxId = header.get(MAX_ID);
        if (maxId != null) {
            try {
                list.raiseMaxId(Integer.parseInt(maxId.trim()));
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Malformed " + MAX_ID + " in document header: " + maxId);
            }
        }
        return list;
    }

    /// The header properties for a list of signs; empty if the list has never
    /// held a sign.
    public static Map<String, String> toHeader(EmbeddedSignList list) {
        Map<String, String> map = new TreeMap<>();
        if (list.getMaxId() > 0) {
            map.put(MAX_ID, Integer.toString(list.getMaxId()));
        }
        for (EmbeddedSign sign : list.asList()) {
            map.put(SIGN_PREFIX + sign.code(), encodeSign(sign));
        }
        return map;
    }

    private static String encodeSign(EmbeddedSign sign) {
        String variantOf = sign.hasVariantOf() ? sign.variantOf() : NONE;
        String description = sign.description().isEmpty() ? NONE : encode(sign.description());
        byte[] svg = sign.svg();
        byte[] rendering = EmbeddedSignShapes.renderingData(sign.code(), svg).getBytes(StandardCharsets.UTF_8);
        return variantOf + " " + FORMAT_VERSION + " " + description + " " + base64(svg) + " " + base64(rendering);
    }

    private static EmbeddedSign decodeSign(String code, String value) {
        OptionalInt id = EmbeddedSignList.idOf(code);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("bad code " + code);
        }
        String[] parts = value.trim().split(" +");
        if (parts.length < 2 || !FORMAT_VERSION.equals(parts[1])) {
            throw new IllegalArgumentException("unknown format version "
                    + (parts.length < 2 ? "(none)" : parts[1]));
        }
        if (parts.length != 5) {
            throw new IllegalArgumentException("expected 5 fields, found " + parts.length);
        }
        String variantOf = NONE.equals(parts[0]) ? null : parts[0];
        if (!EmbeddedSign.isValidVariantOf(variantOf)) {
            // Older files may have other codes: keep the sign, drop the code.
            LOGGER.log(Level.WARNING, "Embedded sign {0}: \"{1}\" is not a Gardiner code, ignored",
                    new Object[] { code, variantOf });
            variantOf = null;
        }
        String description = NONE.equals(parts[2]) ? "" : decode(parts[2]);
        // parts[4], the rendering data, is recomputed from the SVG.
        return new EmbeddedSign(code, unbase64(parts[3]), variantOf, description);
    }

    private static String base64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /// @throws IllegalArgumentException if `text` is not valid base64.
    private static byte[] unbase64(String text) {
        return Base64.getDecoder().decode(text);
    }

    private static String encode(String text) {
        return base64(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        return new String(unbase64(text), StandardCharsets.UTF_8);
    }
}
