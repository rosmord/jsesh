/**
 */
package jsesh.model;

import java.util.Optional;

import jsesh.model.constants.KerningAxis;


/**
 * An horizontal list of elements.
 * @author Rosmorduc
 */
public class HBox extends EmbeddedModelElement implements InnerEditable {

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

    /// If this box only contains kerning operators, the axis of the last one.
    ///
    /// Such a box is a line of a quadrat used to kern the lines around it, as in `p:%vkern:q`.
    public Optional<KerningAxis> getKerningAxis() {
        if (getNumberOfChildren() == 0) {
            return Optional.empty();
        }
        KerningAxis axis = null;
        for (int i = 0; i < getNumberOfChildren(); i++) {
            if (!(getChildAt(i) instanceof Kerning k)) {
                return Optional.empty();
            }
            axis = k.getAxis();
        }
        return Optional.of(axis);
    }

    /// Does this box contain a kerning operator?
    public boolean containsKerning() {
        for (int i = 0; i < getNumberOfChildren(); i++) {
            if (getChildAt(i) instanceof Kerning) {
                return true;
            }
        }
        return false;
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
	 * jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
     */
    public int compareToAux(ModelElement e) {
        return compareContents(e);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.ModelElement#deepCopy()
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
	 * @see jsesh.model.ModelElement#buildTopItem()
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

    @Override
    public boolean isNavigable() {
        return true;
    }

    /// The ends of a row are places where an element can be added to it.
    @Override
    public boolean edgeGapsAreInnerStops() {
        return true;
    }

    @Override
    public void insertSignAt(int gap, Hieroglyph sign) {
        addChildAt(gap, sign);
    }

    @Override
    public boolean disappearsWhenEmpty() {
        return true;
    }

    @Override
    public void simplifyElementAt(int index) {
        getHorizontalListElementAt(index).simplified().ifPresent(e -> setChildAt(index, e));
    }
}
