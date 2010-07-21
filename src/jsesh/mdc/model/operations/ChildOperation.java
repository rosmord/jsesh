/*
 * Created on 28 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.model.operations;

import jsesh.mdc.model.ModelElement;

/**
 * This operation means that a child of the element was modified.
 *   @author rosmord
 *
 */
public class ChildOperation extends ModelOperation {

	private ModelOperation childOperation;

	/**
	 * 
	 * @param element : the current element
	 * @param childOperation : the operation made on one of this element's children.
	 */
	public ChildOperation(
		ModelElement element,
		ModelOperation childOperation) {
		super(element);
		this.childOperation= childOperation;
	}

		
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.operations.ModelOperation#accept(jsesh.mdc.model.operations.ModelOperationVisitor)
	 */
	public void accept(ModelOperationVisitor v) {
		v.visitChildOperation(this);
	}
	/**
	 * Return the operation done on the child of the element.
	 * @return the child operation.
	 */
	public ModelOperation getChildOperation() {
		return childOperation;
	}

}
