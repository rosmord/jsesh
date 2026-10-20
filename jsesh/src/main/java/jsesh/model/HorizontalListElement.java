package jsesh.model;

import java.util.Optional;


abstract public class HorizontalListElement extends EmbeddedModelElement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1478503908902697915L;

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#buildTopItem()
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

	/// A simpler element equivalent to this one: for instance, a ligature
	/// reduced to one sign is just this sign.
	///
	/// @return a new, simpler element, or empty if this element is as simple as
	///         it gets.
	public Optional<HorizontalListElement> simplified() {
		return Optional.empty();
	}
}
