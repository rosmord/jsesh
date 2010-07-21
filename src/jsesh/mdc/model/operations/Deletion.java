/*
 * Created on 20 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.model.operations;

import jsesh.mdc.model.ModelElement;

/**
 * Deletion represents the action of deleting an element.
 * the index passed corresponds to the position before the element to be deleted. 
 * @author rosmord
 */
public class Deletion extends ModelOperation {
	
	private int start;
	private int end;

	/**
	 * @param element the container element.
	 * @param start the position before the element to be deleted.
	 * @param end
	 */
	public Deletion(ModelElement element, int start, int end) {
			super(element);
			this.start= start;
			this.end= end;
	}

	/**
	 * @return Returns the end.
	 */
	public int getEnd() {
		return end;
	}
	
	/**
	 * @return Returns the start.
	 */
	public int getStart() {
		return start;
	}
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperation#visitModelOperation(jsesh.mdc.model.operations.ModelOperationVisitor)
	 */
	public void accept(ModelOperationVisitor v) {
		v.visitDeletion(this);		
	}

}
