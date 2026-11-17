package jsesh.mdc.model;

/**
 * This file is free Software (c) Serge Rosmorduc
 * 
 * @author rosmord
 *  
 */
public class HRule extends TopItem {
	private char type;

	private int startPos, endPos;

	public HRule(char type, int startPos, int endPos) {
		this.type = type;
		this.startPos = startPos;
		this.endPos = endPos;
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
	 * Returns the type.
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
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
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
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		HRule r= new HRule(type, startPos, endPos);
		copyStateTo(r);
		return r;
	}
}