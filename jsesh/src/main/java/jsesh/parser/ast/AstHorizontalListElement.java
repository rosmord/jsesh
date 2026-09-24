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
