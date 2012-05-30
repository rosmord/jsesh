package jsesh.mdc.model;

/**
 * 
 * Superscript
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */

public class Superscript extends TopItem implements TextContainer {
	private static final long serialVersionUID = -7999824386923154047L;
	private String text;
	
	public Superscript(String text) {
		this.text= text;
	}
	
	
	/**
	 * Returns the text.
	 * @return StringBuffer
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 * @param text The text to set
	 */
	public void setText(String text) {
		this.text= text;
		notifyModification();
	}

	/*
	 * @see jsesh.mdc.model.ModelElement#accept(jsesh.mdc.model.ModelElementVisitor)
	 */
	public void accept(ModelElementVisitor v) {
		v.visitSuperScript(this);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		int result= text.toString().compareTo(((Superscript)e).getText().toString());
		if (result==0)
			result= getState().compareTo(((Superscript)e).getState());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public Superscript deepCopy() {
		Superscript r= new Superscript(text);
		copyStateTo(r);
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(super "+ text + ")";
	}

}



