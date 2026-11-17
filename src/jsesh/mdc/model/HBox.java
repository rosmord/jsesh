/**
 */

package jsesh.mdc.model;


import jsesh.mdc.interfaces.HBoxInterface;

/**
 * <p>
 * 
 * @author Rosmorduc
 * </p>
 */
public class HBox extends EmbeddedModelElement implements HBoxInterface {

	public HBox() {
	}

	public HorizontalListElementIterator iterator() {
		return new HorizontalListElementIterator(getListIterator());
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


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(hbox " + getChildrenAsString() + ")";
	}

	/**
	 * A hbox is wide if it contains <em>only</em> a wide sign.
	 * @return true if the box is wide.
	 * @see Hieroglyph#isWide()
	 */
	public boolean isWide() {
		boolean result= false;
		if (getNumberOfChildren() == 1) {
			ModelElement elt= getChildAt(0);
			if (elt instanceof Hieroglyph)
				result= ((Hieroglyph)getChildAt(0)).isWide();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		HBox result;
		result= new HBox();
		copyContentTo(result);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		Cadrat c = new Cadrat();
		c.addHBox((HBox) this.deepCopy());
		return c;
	}

}
