/*
 * Created on 19 mai 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdc.model;

/**
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PageBreak extends TopItem {
	private static final long serialVersionUID = -2629984358117811174L;

	public PageBreak() {
	}

	public void accept(ModelElementVisitor v) {
		v.visitPageBreak(this);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return getState().compareTo(((PageBreak)e).getState());
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public PageBreak deepCopy() {
		PageBreak b= new PageBreak();
		copyStateTo(b);
		return b;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.TopItem#isBreak()
	 */
	public boolean isBreak() {
		return true;
	}
}
