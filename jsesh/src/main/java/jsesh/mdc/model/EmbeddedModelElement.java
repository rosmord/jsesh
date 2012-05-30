/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdc.model;

import jsesh.mdc.model.operations.ModelOperation;

/**
 * @author rosmord
 *
 */
public abstract class EmbeddedModelElement extends ModelElement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1774338831719968071L;

	private ModelElement parent= null;

	/**
	 * next and previous can be directly manipulated by the parent class ModelElement.
	 * Hence we don't set them private.
	 */
	ModelElement next= null, previous=null;
	
	public ModelElement getParent() {
		return parent;
	}

	public void setParent(ModelElement parent) {
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#getModelElementContainer()
	 */
	public ModelElementObserver getModelElementContainer() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#unsetContainer()
	 */
	protected void unsetContainers() {
		parent= null;
	}
	

	final protected void notifyModelElementObservers(ModelOperation op) {
		if (parent != null)
			parent.observedElementChanged(op);
	}

	/**
	 * Separate a model element from its container.
	 */

	final protected void detachFromContainer() {
		unsetContainers();
		previous = null;
		next = null;
	}

	public ModelElement getNextSlibing() {
		return next;
	}

	public ModelElement getPreviousSlibing() {
		return previous;
	}
	
	
	
}
