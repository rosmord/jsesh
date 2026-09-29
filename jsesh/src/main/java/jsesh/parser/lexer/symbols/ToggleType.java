/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.lexer.symbols;

/// The kind of on/off or one-shot rendering toggle recognized by a `TOGGLE` symbol.
public enum ToggleType {
    SHADING_TOGGLE,
    SHADING_ON,
    SHADING_OFF,
    RED,
    BLACK,
    BLACK_RED,
    LACUNA,
    LINE_LACUNA,
    OMIT
}
