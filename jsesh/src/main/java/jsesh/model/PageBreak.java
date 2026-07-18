/*
 * Created on 19 mai 2004
 *
 */
package jsesh.model;

public class PageBreak extends TopItem {

	public PageBreak() {
	}

        @Override
	public void accept(ModelElementVisitor v) {
		v.visitPageBreak(this);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
	 */
        @Override
	public int compareToAux(ModelElement e) {
		return getState().compareTo(((PageBreak)e).getState());
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#deepCopy()
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
	 * @see jsesh.model.TopItem#isBreak()
	 */
        @Override
	public boolean isBreak() {
		return true;
	}
}
