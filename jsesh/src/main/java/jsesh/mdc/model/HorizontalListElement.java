package jsesh.mdc.model;

import jsesh.mdc.interfaces.HorizontalListElementInterface;

abstract public class HorizontalListElement extends EmbeddedModelElement implements HorizontalListElementInterface{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1478503908902697915L;

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		return buildCadrat();
	}
	
	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return (HorizontalListElement) deepCopy();
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
