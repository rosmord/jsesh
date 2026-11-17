package jsesh.mdc.model;

import jsesh.mdc.interfaces.HorizontalListElementInterface;

abstract public class HorizontalListElement extends EmbeddedModelElement implements HorizontalListElementInterface{
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		return buildCadrat();
	}
	
	/**
	 * Build a cadrat containing only this element.
	 * @return
	 */
	public Cadrat buildCadrat() {
		Cadrat c= new Cadrat();
		HBox b= new HBox();
		b.addHorizontalListElement((HorizontalListElement) this.deepCopy());
		c.addHBox(b);
		return c;		
	}
	
	/**
	 * Returns true if the element contains only one sign. 
	 * @return
	 */
	public boolean containsOnlyOneSign() {
		return false;
	}
	
	
	/**
	 * If the group contains only one sign, return it, else return null.
	 * @return
	 */
	public Hieroglyph getLoneSign() {
		return null;
	}
}
