package jsesh.parser.ast;

import jsesh.model.api.HorizontalListElementInterface;

/**
 * Base for the nodes that can appear inside an {@link AstHBox}: inner groups
 * (see {@link AstInnerGroup}) and complex ligatures.
 *
 * @author rosmord
 */
public sealed interface AstHorizontalListElement extends AstNode, HorizontalListElementInterface
        permits AstInnerGroup, AstComplexLigature {
}
