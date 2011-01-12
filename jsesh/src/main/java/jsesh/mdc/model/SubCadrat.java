
package jsesh.mdc.model;


import jsesh.mdc.interfaces.SubCadratInterface;

/**
 * 
 * SubCadrat contain lists of BasicItems (cadrats and texts).
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */
public class SubCadrat extends InnerGroup implements SubCadratInterface {

	public SubCadrat(BasicItemList b)
	{
		addChild(b);
	}

	/**
	 * Build a subcadrat containing only one basic item.
	 * <p> this is a convenience method.
	 * @param basicItem

	 */
	public SubCadrat(BasicItem basicItem) {
		BasicItemList l= new BasicItemList();
		l.addBasicItem(basicItem);
		addChild(l);
	}

	/*
	 * @see jsesh.mdc.model.ModelElement#Accept(jsesh.mdc.model.ModelElementVisitor)
	 */
	public void accept(ModelElementVisitor v) {v.visitSubCadrat(this);}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(subc "+ getChildrenAsString() + ")";
	}


	/**
	 * @return the list.
	 */
	public BasicItemList getBasicItemList() {
		return (BasicItemList) getChildAt(0);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		BasicItemList l= (BasicItemList) getBasicItemList().deepCopy();
		SubCadrat r= new SubCadrat(l);
		return r;
	}

} // end SubCadrat



