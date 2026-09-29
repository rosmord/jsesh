/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * The kinds of cartouche/enclosure an {@link AstCartouche} or old-style
 * cartouche start can denote, keyed by its Manuel de Codage letter.
 */
public enum CartoucheType {

    CARTOUCHE('c'),
    SEREKH('s'),
    HOUT('h'),
    CASTLE('f'),
    CIRCULAR_ENCLOSURE('g');

    private final char code;

    CartoucheType(char code) {
        this.code = code;
    }

    /**
     * @return the Manuel de Codage letter for this cartouche type.
     */
    public char code() {
        return code;
    }

    /**
     * @param code a Manuel de Codage cartouche letter, in either case (the
     * "old style" MacScribe syntax uses uppercase letters for the same
     * types).
     * @return the matching cartouche type.
     */
    public static CartoucheType forCode(char code) {
        char lower = Character.toLowerCase(code);
        for (CartoucheType type : values()) {
            if (type.code == lower) {
                return type;
            }
        }
        throw new IllegalArgumentException("Not a valid cartouche type code: " + code);
    }
}
