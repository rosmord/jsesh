/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

/// Enum class for demo token types.
/// DemoToken
public enum DemoToken {
    IF, // if
    ELSE, // else
    FUN, // fun
    IDENTIFIER, // [a-zA-Z_][a-zA-Z0-9_]*
    INTEGER, // [0-9]+
    REAL, // [0-9]+.[0-9]+
    STRING  // "[^"]*"
}
