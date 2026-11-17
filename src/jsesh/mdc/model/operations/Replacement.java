/*
 * Created on 20 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.model.operations;

import jsesh.mdc.model.ModelElement;

/**
 * Insertion represents the action of inserting an element in another one. 
 *   @author rosmord
 *
 */
public class Replacement extends ModelOperation {
	
	private int index;

	/**
	 * 
	 * @param element the container element.
	 * @param index the position in the container.
	 * 
	 */
	public Replacement(ModelElement element, int index) {
			super(element);
			this.index= index;
	}

	/**
	 * @return the index.
	 */
	public int getIndex() {
		return index;
	}


	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperation#visitModelOperation(jsesh.mdc.model.operations.ModelOperationVisitor)
	 */
	public void accept(ModelOperationVisitor v) {
		v.visitReplacement(this);		
	}

}
