package jsesh.mdc.model;

import java.util.Iterator;

/**
 * This file is free Software 
 * under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * 
 * (c) Serge Rosmorduc
 * @author Serge Rosmorduc
 *
 */
public class ModelElementIterator {

	private Iterator i;

	/**
	 * Constructor ModelElementIterator.
	 */
	public ModelElementIterator() {
		i= null;
	}

	public ModelElementIterator(Iterator i) {
		this.i= i;
	}
	/*
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (i == null)
			return false;
		else
			return i.hasNext();
	}
	/**
	 * Return the next model element and advance.
	 * @return the next model element.
	 * @see java.util.Iterator#next()
	 */
	public ModelElement next() {
		return (ModelElement) i.next();
	}
	/**
	 * remove the current element.
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		i.remove();
	}

}
