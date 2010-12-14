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
public class Modification extends ModelOperation {
	
	/**
	 * 
	 * @param element the container element.
	 */
	public Modification(ModelElement element) {
			super(element);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperation#visitModelOperation(jsesh.mdc.model.operations.ModelOperationVisitor)
	 */
	public void accept(ModelOperationVisitor v) {
		v.visitModification(this);		
	}

}
