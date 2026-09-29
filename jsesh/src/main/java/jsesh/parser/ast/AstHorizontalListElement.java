/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * Base for the nodes that can appear inside an {@link AstHBox}: inner groups
 * (see {@link AstInnerGroup}) and complex ligatures.
 *
 * @author rosmord
 */
public sealed interface AstHorizontalListElement extends AstNode
        permits AstInnerGroup, AstComplexLigature {
}
