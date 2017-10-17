/**
 */
package jsesh.mdc.model;

import jsesh.mdc.interfaces.HBoxInterface;

/**
 * An horizontal list of elements.
 * @author Rosmorduc
 */
public class HBox extends EmbeddedModelElement implements HBoxInterface {

    /**
     *
     */
    private static final long serialVersionUID = -6681486002034607932L;

    public HBox() {
    }

    /**
     * Create an hbox with an item in it.
     *
     * @param item
     * @throws NullPointerException if the item can
     */
    public HBox(BasicItem item) {
        HorizontalListElement horizontalListElement = item
                .buildHorizontalListElement();
        if (horizontalListElement != null) {
            addHorizontalListElement(horizontalListElement);
        } else {
            throw new java.lang.IllegalArgumentException("not possible in hbox " + item);
        }
    }

    /**
     * Create an hbox with an item in it.
     *
     * @param item
     * @throws NullPointerException if the item can
     */
    public HBox(HorizontalListElement item) {
        if (item != null) {
            addHorizontalListElement(item);
        } else {
            throw new NullPointerException();
        }
    }

    public void addHorizontalListElement(HorizontalListElement elt) {
        addChild(elt);
    }

    public void removeHorizontalListElement(HorizontalListElement elt) {
        removeChild(elt);
    }

    public HorizontalListElement getHorizontalListElementAt(int i) {
        return (HorizontalListElement) getChildAt(i);
    }

    public void accept(ModelElementVisitor v) {
        v.visitHBox(this);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
     */
    public String toString() {
        return "(hbox " + getChildrenAsString() + ")";
    }

    /**
     * A hbox is wide if it contains <em>only</em> a wide sign.
     *
     * @return true if the box is wide.
     * @see Hieroglyph#isWide()
     */
    public boolean isWide() {
        boolean result = false;
        if (getNumberOfChildren() == 1) {
            ModelElement elt = getChildAt(0);
            if (elt instanceof Hieroglyph) {
                result = ((Hieroglyph) getChildAt(0)).isWide();
            }
        }
        return result;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
     */
    public int compareToAux(ModelElement e) {
        return compareContents(e);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
     */
    public HBox deepCopy() {
        HBox result;
        result = new HBox();
        copyContentTo(result);
        return result;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
     */
    public TopItem buildTopItem() {
        Cadrat c = new Cadrat();
        c.addHBox((HBox) this.deepCopy());
        return c;
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        if (getNumberOfChildren() == 1) {
            return (HorizontalListElement) getHorizontalListElementAt(0)
                    .deepCopy();
        } else {
            BasicItemList list = new BasicItemList();
            for (int i = 0; i < getNumberOfChildren(); i++) {
                Cadrat c = new Cadrat();
                c.addChild((EmbeddedModelElement) getHorizontalListElementAt(i)
                        .deepCopy());
                list.addBasicItem(c);
            }
            return new SubCadrat(list);
        }
    }
}
