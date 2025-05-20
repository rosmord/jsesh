
package jsesh.mdc.model;

import jsesh.mdc.interfaces.InnerGroupInterface;

/**
 * 
 * InnerGroup is the base class for elements contained in hboxes.
 * It should be an interface. 
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */

abstract public class InnerGroup extends HorizontalListElement implements InnerGroupInterface {

	private static final long serialVersionUID = 7046828734218909370L;

	@Override
	public abstract InnerGroup deepCopy();
} // end InnerGroup



