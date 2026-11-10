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
import jsesh.model.constants.JSeshInfoConstants;

/// Reads and writes the embedded signs of a document as header properties.
///
/// ```
/// ++JSesh_max_embedded_sign_id 3 +s
/// ++JSesh_embedded_sign_SIGN1 <variantOf> <description> <svg> +s
/// ```
///
/// - `variantOf` is the Gardiner code the sign is a variant of, or `-`;
/// - `description` is the description in UTF-8, base64-encoded, or `-` if
///   it is empty;
/// - `svg` is the normalized SVG in UTF-8, base64-encoded.
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
        return variantOf + " " + description + " " + encode(sign.svg());
    }

    private static EmbeddedSign decodeSign(String code, String value) {
        OptionalInt id = EmbeddedSignList.idOf(code);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("bad code " + code);
        }
        String[] parts = value.trim().split(" +");
        if (parts.length != 3) {
            throw new IllegalArgumentException("expected 3 fields, found " + parts.length);
        }
        String variantOf = NONE.equals(parts[0]) ? null : parts[0];
        String description = NONE.equals(parts[1]) ? "" : decode(parts[1]);
        return new EmbeddedSign(code, decode(parts[2]), variantOf, description);
    }

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String base64) {
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }
}
