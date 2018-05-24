/*
 * Created on 19 mai 2004
 *
 */
package jsesh.mdc.model;

public class PageBreak extends TopItem {

	public PageBreak() {
	}

        @Override
	public void accept(ModelElementVisitor v) {
		v.visitPageBreak(this);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
        @Override
	public int compareToAux(ModelElement e) {
		return getState().compareTo(((PageBreak)e).getState());
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
        @Override
	public PageBreak deepCopy() {
		PageBreak b= new PageBreak();
		copyStateTo(b);
		return b;
	}
	
	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return null;	
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.TopItem#isBreak()
	 */
        @Override
	public boolean isBreak() {
		return true;
	}
}
