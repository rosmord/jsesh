package jsesh.parser.ast;

import jsesh.model.api.CartoucheInterface;

/**
 * A cartouche, serekh, hout-sign or castle.
 *
 * @param type the kind of cartouche, as one of the codes in
 * {@link CartoucheType} ('c', 's', 'h', 'f', 'g').
 * @param startPart how the first extremity of the cartouche should be drawn.
 * @param endPart how the last extremity of the cartouche should be drawn.
 * @author rosmord
 */
public record AstCartouche(int type, int startPart, int endPart, AstBasicItemList content)
        implements AstInnerGroup, CartoucheInterface {

    /**
     * A plain, fully-drawn cartouche ({@code <...>}: both extremities drawn,
     * as opposed to a partial cartouche). For tests.
     */
    public static AstCartouche of(AstNode... content) {
        return new AstCartouche(CartoucheType.CARTOUCHE, 1, 2, AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitCartouche(this);
    }
}
