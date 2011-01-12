package jsesh.mdc.model;

/**
 * This elements clears all current tab stops.
 * @author rosmord
 */
public class TabbingClear extends TopItem {

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
	public ModelElement deepCopy() {
		TabbingClear c= new TabbingClear();
		this.copyStateTo(c);
		return c;
	}

}
