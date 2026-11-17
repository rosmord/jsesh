package jsesh.mdc.model;

/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class TabStop extends TopItem {
	private int stopPos;
	
	public TabStop(int stopPos)
	{
		this.stopPos= stopPos;
	}
	
	
	/**
	 * Returns the stopPos.
	 * the scale is 1 unit = 1/200th of the basic sign size ("n" width).
	 * @return int
	 */
	public int getStopPos() {
		return stopPos;
	}

	/**
	 * Sets the stopPos.
	 * @param stopPos The stopPos to set
	 */
	public void setStopPos(int stopPos) {
		this.stopPos= stopPos;
	}
	
	

	/*
	 * @see jsesh.mdc.model.ModelElement#accept(jsesh.mdc.model.ModelElementVisitor)
	 */
	public void accept(ModelElementVisitor v) {
		v.visitTabStop(this);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		int result= stopPos - ((TabStop)e).stopPos;
		if (result==0)
			result= getState().compareTo(((TabStop)e).getState());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		TabStop r= new TabStop(stopPos);
		copyStateTo(r);
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(tab "+ stopPos + ")";
	}
}
