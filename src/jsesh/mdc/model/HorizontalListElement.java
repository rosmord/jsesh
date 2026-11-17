package jsesh.mdc.model;

import jsesh.mdc.interfaces.HorizontalListElementInterface;

abstract public class HorizontalListElement extends EmbeddedModelElement implements HorizontalListElementInterface{
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		Cadrat c= new Cadrat();
		HBox b= new HBox();
		b.addHorizontalListElement((HorizontalListElement) this.deepCopy());
		c.addHBox(b);
		return c;
	}
}
