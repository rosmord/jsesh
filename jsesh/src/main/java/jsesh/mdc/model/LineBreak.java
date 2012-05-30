/*
 * Created on 19 mai 2004
 */
package jsesh.mdc.model;

/**
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LineBreak extends TopItem {
	
	private static final long serialVersionUID = 1228419796041957932L;
	/**
	 * post-break vertical spacing
	 * Default value ?
	 */
	private int spacing; 


	
	public LineBreak(int spacing) {
		this.spacing= spacing;
	}

	/**
	 * 
	 */
	public LineBreak() {
		this(100);
	}

	public void accept(ModelElementVisitor v) {
		v.visitLineBreak(this);
	}
	
	/**
	 * Returns the spacing value.
	 * 100 is a full line, 200 is two lines worth, and so on.
	 * @return spacing.
	 */
	public int getSpacing() {
		return spacing;
	}

	
	/**
	 * @param i
	 */
	public void setSpacing(int i) {
		spacing = i;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "lineBreak\n";
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		LineBreak b= (LineBreak) e;
		int result= spacing - b.spacing;
		if (result == 0) {
			result= getState().compareTo(b.getState());
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public LineBreak deepCopy() {
		LineBreak b= new LineBreak(spacing);
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
