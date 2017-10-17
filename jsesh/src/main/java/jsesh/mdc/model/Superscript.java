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

	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return null;
	}

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        // Ideally, we would use a delegate, which itself would use inheritance
        // or delegation or interface default method to do this for all TextContainer elements...
        // but we can't use default method on the interface, for we want hasSameValueIgnoreIdAux to be protected
        // not public.
        Superscript otherSuperscript= (Superscript) other;
        return this.text.equals(otherSuperscript.text);
    }
        
        
}



