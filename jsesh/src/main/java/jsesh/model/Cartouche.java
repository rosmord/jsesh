package jsesh.model;

import jsesh.model.constants.CartouchePart;
import jsesh.model.constants.CartoucheType;


/**
 * <p>
 *
 * @author Rosmorduc
 * </p>
 */
public class Cartouche extends InnerGroup {

    /**
     *
     */
    private static final long serialVersionUID = 4695009410080992441L;

    /**
     * <p>
     * see endPart
     * </p>
     *
     */
    private CartouchePart startPart;

    /**
     * <p>
     * indicates how the last extremity of the cartouche should be drawn. See
     * {@link CartouchePart} for the meaning of each value.
     * </p>
     *
     */
    private CartouchePart endPart;

    /**
     * <p>
     * the kind of cartouche, serekh, hwt-sign, castle or circular enclosure.
     * </p>
     *
     */
    private CartoucheType type;

    /**
     * <p>
     * Creates a cartouche.
     * </p>
     * <p>
     *
     * @param type which actual kind of cartouche should be drawn
     * </p>
     * <p>
     * @param startPart describe which part of the cartouche to draw first.
     * </p>
     * <p>
     * @param endPart describe the last part of the cartouche to draw.
     * </p>
     * <p>
     * @param content the content of the cartouche
     * </p>
     */
    public Cartouche(CartoucheType type, CartouchePart startPart, CartouchePart endPart, BasicItemList content) {
        this.type = type;
        this.startPart = startPart;
        this.endPart = endPart;
        //setChildAt(0, content);
        addChildAt(0, content);
    } // end Cartouche

    /**
     * <p>
     * indicates how the first extremity of the cartouche should be drawn.
     * See {@link CartouchePart} for the meaning of each value.
     * </p>
     *
     * @return the start part code.
     *
     */
    public CartouchePart getStartPart() {
        return startPart;
    }

    public void setStartPart(CartouchePart _startPart) {
        startPart = _startPart;
    }

    /**
     * <p>
     * indicates how the last extremity of the cartouche should be drawn.
     * See {@link CartouchePart} for the meaning of each value.
     * </p>
     *
     * @return the end part code.
     *
     */
    public CartouchePart getEndPart() {
        return endPart;
    }

    public void setEndPart(CartouchePart _endPart) {
        endPart = _endPart;
    }

    /**
     * Returns the type of the cartouche.
     *
     * @return the type of the cartouche.
     */
    public CartoucheType getType() {
        return type;
    }

    public void setType(CartoucheType _type) {
        type = _type;
    }

    /*
	 * @see jsesh.model.ModelElement#Accept(jsesh.model.ModelElementVisitor)
     */
    public void accept(ModelElementVisitor v) {
        v.visitCartouche(this);
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
     */
    public String toString() {
        return "(cartouche " + getChildrenAsString() + ")";
    }

    /**
     * returns the list of items contained in this cartouche.
     *
     * @return the list of items contained in this cartouche.
     */
    public BasicItemList getBasicItemList() {
        return (BasicItemList) getChildAt(0);
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
     */
    public int compareToAux(ModelElement e) {
        int result = compareContents(e);
        if (result == 0) {
            Cartouche c = (Cartouche) e;
            ;
            result = Character.compare(type.code(), c.type.code());
            if (result == 0) {
                result = Integer.compare(startPart.digit(), c.startPart.digit());
                if (result == 0) {
                    result = Integer.compare(endPart.digit(), c.endPart.digit());
// As cartouches are no more top-level items, they don't have state anymore
//					if (result == 0) {
//						result = getState().compareTo(c.getState());
//					}
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
	 * @see jsesh.model.ModelElement#deepCopy()
     */
    public Cartouche deepCopy() {
        BasicItemList l = (BasicItemList) getBasicItemList().deepCopy();
        Cartouche c = new Cartouche(type, startPart, endPart, l);
        // As cartouches are no more top-level items, they don't have state anymore
        //copyStateTo(c);
        return c;
    }

    /**
     * A cartouche is never considered as containing only one sign, as it's a
     * sign in itself.
     *
     * @return always false.
     */
    @Override
    public boolean containsOnlyOneSign() {
        return false;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        Cartouche c = (Cartouche) other;
        return (
                this.type == c.type
                && this.startPart == c.startPart
                && this.endPart == c.endPart
                );
    }

} // end Cartouche
