package jsesh.model;

/**
 * This file is free Software (c) Serge Rosmorduc
 * 
 * @author rosmord
 *  
 */

/// An horizontal rule, `{lA,B}` (thin) or `{LA,B}` (wide).
///
/// The rule is drawn from `A` % to `B` % of the width of the text (the width of
/// its view, computed by `jsesh.render.layout.Layout`): `{l0,100}` goes across
/// the text, `{l10,90}` leaves 10 % of the width on each side.
public class HRule extends TopItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3065155670819138356L;

	private char type;

	private int startPos, endPos;
	/// Creates an horizontal rule.
	///
	/// @param type type of line. 'l' for single, 'L' for double.
	/// @param startPos the start of the rule, in percents of the width of the text.
	/// @param endPos the end of the rule, in percents of the width of the text.
	public HRule(char type, int startPos, int endPos) {
		this.type = type;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	/// The start of the drawn line, as a fraction of the width of the text.
	///
	/// The positions are clamped between 0 and 100 %, and taken in order, so that
	/// `{l90,10}` is `{l10,90}`, and older files (whose positions were in tab
	/// units) still give a line inside the text.
	///
	/// @return a number between 0 and 1.
	public double startFraction() {
		return clampedPercent(Math.min(startPos, endPos)) / 100.0;
	}

	/// The end of the drawn line, as a fraction of the width of the text (see
	/// [#startFraction()]).
	///
	/// @return a number between 0 and 1.
	public double endFraction() {
		return clampedPercent(Math.max(startPos, endPos)) / 100.0;
	}

	private static int clampedPercent(int p) {
		return Math.max(0, Math.min(100, p));
	}

	/**
	 * Returns the endPos.
	 * 
	 * @return int
	 */
	public int getEndPos() {
		return endPos;
	}

	/**
	 * Returns the startPos.
	 * 
	 * @return int
	 */
	public int getStartPos() {
		return startPos;
	}

	/**
	 * Returns the type : 'l' for thin lines, 'L' for wide ones.
	 * 
	 * @return char
	 */
	public char getType() {
		return type;
	}

	/**
	 * Sets the endPos.
	 * 
	 * @param endPos
	 *            The endPos to set
	 */
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	/**
	 * Sets the startPos.
	 * 
	 * @param startPos
	 *            The startPos to set
	 */
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            The type to set
	 */
	public void setType(char type) {
		this.type = type;
	}

	/*
	 * @see ModelElement#accept(ModelElementVisitor) 
	 */
	public void accept(ModelElementVisitor v) {
		v.visitHRule(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		HRule r = (HRule) e;
		int result = type - r.type;
		if (result == 0) {
			result = startPos - r.startPos;
			if (result == 0) {
				result = endPos - r.endPos;
				if (result == 0) {
					result= getState().compareTo(r.getState());
				}
			}
		}
		return result;
	}
	
	
	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return null;	
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#deepCopy()
	 */
        @Override
	public HRule deepCopy() {
		HRule r= new HRule(type, startPos, endPos);
		copyStateTo(r);
		return r;
	}

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
            HRule rule= (HRule) other;
            return this.type== rule.type && this.startPos== rule.startPos
                    && this.endPos== rule.endPos;
    }
        
        
}