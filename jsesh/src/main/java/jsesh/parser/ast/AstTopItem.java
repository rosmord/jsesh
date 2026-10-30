/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/// An item which can appear in an [AstTopItemList], i.e. directly in a text
/// line or in a zone.
///
/// Top items are the basic items (see [AstBasicItem]), plus the constructs
/// which only make sense at top level: superscripts, line and page breaks,
/// tabulations, horizontal rules and zones.
///
/// Sealed, so a consumer can exhaustively `switch` over the items of a list.
///
/// @author rosmord
public sealed interface AstTopItem extends AstNode
        permits AstBasicItem, AstSuperscript, AstLineBreak, AstPageBreak, AstTabStop,
        AstHRule, AstZone {
}
