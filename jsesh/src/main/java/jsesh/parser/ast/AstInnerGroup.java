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
