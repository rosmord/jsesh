
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

abstract public class InnerGroup extends ModelElement implements InnerGroupInterface {

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		Cadrat c= new Cadrat();
		HBox b= new HBox();
		b.addInnerGroup((InnerGroup) this.deepCopy());
		c.addHBox(b);
		return c;
	}
} // end InnerGroup



