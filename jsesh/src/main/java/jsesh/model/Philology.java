package jsesh.model;

import jsesh.mdc.interfaces.PhilologyInterface;

/**
 * 
 * Philology
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */
public class Philology extends InnerGroup implements PhilologyInterface {

	private static final long serialVersionUID = -433250876175828144L;
	private int type;
	
	public Philology(int type, BasicItemList l)
	{
		this.type= type;
		addChild(l);
	}

 
	/**
	 * Returns the code for the type of brackets.
	 * Remember that the codes for the actual brackets symbols are resp.
	 * 2*getType() and 2*getType()+1 for the opening and closing brackets.
	 * @see jsesh.model.constants.SymbolCodes
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(int type) {
		this.type= type;
	}

	/*
	 * @see jsesh.model.ModelElement#accept(jsesh.model.ModelElementVisitor)
	 */
	public void accept(ModelElementVisitor v) {v.visitPhilology(this);}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(phil "+ getChildrenAsString() + ")";
	}

	/**
	 * @return the list.
	 */
	public BasicItemList getBasicItemList() {
		return (BasicItemList) getChildAt(0);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		int result= type - ((Philology)e).type;
		if (result==0)
			result= compareContents(e);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#deepCopy()
	 */
	public Philology deepCopy() {
		BasicItemList l = (BasicItemList) getBasicItemList().deepCopy();
		Philology result= new Philology(type,l);
		return result;
	}
	
	public boolean containsOnlyOneSign() {
		return false;
	}

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
            Philology otherPhilology= (Philology) other;
            return this.type == otherPhilology.type;
    }
        
        

} 


