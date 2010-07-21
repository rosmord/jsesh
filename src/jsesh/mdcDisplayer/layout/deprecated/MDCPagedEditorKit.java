/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.layout.deprecated;

import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

/**
 * @author Serge Rosmorduc
 * 
 * This file is free software under the Gnu Lesser Public License.
 * @deprecated
 */
public abstract class MDCPagedEditorKit
{

    private static MDCPagedEditorKit instance= new MDCPagedEditorKit(new DrawingSpecificationsImplementation()) {
    	public PagedLayout createPagedLayout() {
	      return new SimplePrinterLayout();
	   }
    };
    
	private DrawingSpecification drawingSpecifications;
	
	public MDCPagedEditorKit(DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications= drawingSpecifications;
	}
	
	public DrawingSpecification getDrawingSpecifications()
	{
		return drawingSpecifications;
	}
	
	abstract public PagedLayout createPagedLayout();
	
	static public MDCPagedEditorKit getDefaultMDCPagedEditorKit() {
		return instance;
	}
}
