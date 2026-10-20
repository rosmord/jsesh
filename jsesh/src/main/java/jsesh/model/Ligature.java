package jsesh.model;

import java.util.Optional;

/**
 * Ligature
 * @author rosmord
 *
 * This code is published under the GNU LGPL.
 */

public class Ligature extends InnerGroup implements InnerEditable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2576146421775705888L;

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
	 * @see jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#deepCopy()
	 */
	public Ligature deepCopy() {
		Ligature l= new Ligature();
		copyContentTo(l);
		return l;
	}

	@Override
	public boolean isNavigable() {
		return true;
	}

	@Override
	public void insertSignAt(int gap, Hieroglyph sign) {
		addChildAt(gap, sign);
	}

	/// An empty ligature is not a normal state, and is not cleaned up.
	@Override
	public boolean disappearsWhenEmpty() {
		return false;
	}

	/// A ligature of one sign is just this sign.
	@Override
	public Optional<HorizontalListElement> simplified() {
		if (getNumberOfChildren() != 1) {
			return Optional.empty();
		}
		return Optional.of((Hieroglyph) getHieroglyphAt(0).deepCopy());
	}
}
