package jsesh.mdc.model;

import jsesh.mdc.interfaces.LigatureInterface;
/**
 * Ligature
 * @author rosmord
 *
 * This code is published under the GNU LGPL.
 */

public class Ligature extends InnerGroup implements LigatureInterface {


	public Ligature() {
	}

	public void addHieroglyph(Hieroglyph hieroglyph) {
		addChild(hieroglyph);
	}

	public void removeHieroglyph(Hieroglyph hieroglyph) {
		removeChild(hieroglyph);
	}

	public Hieroglyph getHieroglyphAt(int i) {
		return (Hieroglyph) getChildAt(i);
	}
	
	public void accept(ModelElementVisitor v) {
		v.visitLigature(this);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(lig " + getChildrenAsString() + ")";
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
		Ligature l= new Ligature();
		copyContentTo(l);
		return l;
	}
}
