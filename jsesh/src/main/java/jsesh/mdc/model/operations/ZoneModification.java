/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model.operations;

import jsesh.mdc.model.ModelElement;

/**
 * A zone modification means that the sub elements of this element have all been modified.
 * None of them has been suppressed.
 * @author S. Rosmorduc
 *
 */
public class ZoneModification extends ModelOperation {
	/**
	 * @return Returns the end.
	 */
	public int getEnd() {
		return end;
	}
	/**
	 * @param end The end to set.
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	/**
	 * @return Returns the start.
	 */
	public int getStart() {
		return start;
	}
	/**
	 * @param start The start to set.
	 */
	public void setStart(int start) {
		this.start = start;
	}
	private int start;
	private int end;
	
	
	/**
	 * An object meaning that all subelements of element between indexes start and end have been modified.
	 * @param element
	 * @param start
	 * @param end
	 */
	public ZoneModification(ModelElement element, int start, int end) {
		super(element);
		this.start = start;
		this.end = end;
	}


	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperation#accept(jsesh.mdc.model.operations.ModelOperationVisitor)
	 */
	public void accept(ModelOperationVisitor v) {
		v.visitZoneModification(this);
	}
	
	
}
