package jsesh.mdc.model;

import jsesh.mdc.interfaces.HorizontalListElementInterface;
import jsesh.mdc.model.utilities.HieroglyphExtractor;

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
	
}
