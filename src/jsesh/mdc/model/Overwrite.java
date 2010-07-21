package jsesh.mdc.model;

import java.util.Iterator;

import jsesh.mdc.interfaces.OverwriteInterface;

/**
 * 
 * @author Rosmorduc
 *         </p>
 */

public class Overwrite extends InnerGroup implements OverwriteInterface {

	public Overwrite(Hieroglyph s1, Hieroglyph s2) {
		addChild(s1);
		addChild(s2);
	}

	public Hieroglyph getFirst() {
		return (Hieroglyph) getChildAt(0);
	}

	public void setFirst(Hieroglyph hieroglyph) {
		setChildAt(0, hieroglyph);
	}

	public Hieroglyph getSecond() {
		return (Hieroglyph) getChildAt(1);
	}

	public void setSecond(Hieroglyph hieroglyph) {
		setChildAt(1, hieroglyph);
	}

	public void accept(ModelElementVisitor v) {
		v.visitOverwrite(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		Overwrite o = new Overwrite((Hieroglyph) getFirst().deepCopy(),
				(Hieroglyph) getSecond().deepCopy());
		return o;
	}

} // end Overwrite
