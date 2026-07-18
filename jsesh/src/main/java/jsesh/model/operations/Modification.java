/*
 * Created on 20 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.model.operations;

import jsesh.model.ModelElement;

/**
 * An unspecified modification made to an element.
 * @author rosmord
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
	 * @see jsesh.model.operations.ModelOperation#visitModelOperation(jsesh.model.operations.ModelOperationVisitor)
	 */
	public void accept(ModelOperationVisitor v) {
		v.visitModification(this);		
	}

}
