/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;


/**
 * Base for the innermost groups: hieroglyphs, ligatures, cartouches,
 * sub-cadrats, philology groups, overwrites and absolute groups.
 *
 * @author rosmord
 */
public sealed interface AstInnerGroup extends AstHorizontalListElement
        permits AstHieroglyph, AstCartouche, AstLigature, AstSubCadrat, AstOverwrite, AstPhilology,
        AstAbsoluteGroup {
}
