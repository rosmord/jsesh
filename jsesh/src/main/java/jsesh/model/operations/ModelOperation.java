/*
 * Created on 20 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.model.operations;

import jsesh.model.ModelElement;

/**
 * This class describes possible operations on a model. 
 *   @author S. Rosmorduc.
 *
 */
public abstract class ModelOperation {

	abstract public void accept(ModelOperationVisitor v);

	protected ModelElement element;


	protected ModelOperation(ModelElement element) {
		this.element= element;
	}
	

	public ModelElement getElement() {
		return element;
	}
}
