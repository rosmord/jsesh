/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * Base type for every node of the literal Manuel de Codage AST.
 * <p>Sealed to the constructs the grammar actually recognizes: a consumer
 * can exhaustively {@code switch} over them (with or without pattern
 * matching) with compiler-checked coverage, in addition to (or instead of)
 * {@link AstVisitor}.
 *
 * @author rosmord
 */
public sealed interface AstNode
        permits AstDocument, AstTopItemList, AstBasicItemList, AstCadrat, AstHBox,
        AstHorizontalListElement,
        AstAlphabeticText, AstHRule, AstLineBreak, AstPageBreak, AstTabStop, AstSuperscript,
        AstToggle, AstStartHieroglyphicText, AstZoneStart, AstTabbing, AstTabbingClear,
        AstModifier, AstModifierList {

    void accept(AstVisitor visitor);
}
