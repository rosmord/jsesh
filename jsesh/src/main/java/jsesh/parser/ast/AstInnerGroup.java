package jsesh.parser.ast;

import jsesh.model.api.InnerGroupInterface;

/**
 * Base for the innermost groups: hieroglyphs, ligatures, cartouches,
 * sub-cadrats, philology groups, overwrites and absolute groups.
 *
 * @author rosmord
 */
public sealed interface AstInnerGroup extends AstHorizontalListElement, InnerGroupInterface
        permits AstHieroglyph, AstCartouche, AstLigature, AstSubCadrat, AstOverwrite, AstPhilology,
        AstAbsoluteGroup {
}
