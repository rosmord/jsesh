package jsesh.mdc.model;

/**
 * This elements clears all current tab stops.
 * @author rosmord
 */
public class TabbingClear extends TopItem {

	private static final long serialVersionUID = -8676818809470697539L;

	@Override
	public void accept(ModelElementVisitor v) {
		v.visitTabbingClear(this);		
	}

	@Override
	protected int compareToAux(ModelElement e) {
		TabbingClear clear= (TabbingClear) e;
		return getState().compareTo(clear.getState());
	}

	@Override
	public TabbingClear deepCopy() {
		TabbingClear c= new TabbingClear();
		this.copyStateTo(c);
		return c;
	}

	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return null;	
	}
}
