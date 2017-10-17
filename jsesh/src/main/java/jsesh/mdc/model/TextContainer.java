/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdc.model;

/**
 * An interface implemented by all model elements which contain "regular" (i.e. alphabetic) text.
 * @author rosmord
 *
 */
public interface TextContainer {

	String getText();
	
	void setText(String text);
	
	ModelElement deepCopy();
        
        
	
}
