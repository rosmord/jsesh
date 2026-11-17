/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.layout;

import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * @author Serge Rosmorduc
 * 
 * This file is free software under the Gnu Lesser Public License.
 * @deprecated
 */
public abstract class MDCPagedEditorKit
{

    private static MDCPagedEditorKit instance= new MDCPagedEditorKit(new DrawingSpecifications()) {
    	public PagedLayout createPagedLayout() {
	      return new SimplePrinterLayout();
	   }
    };
    
	private DrawingSpecifications drawingSpecifications;
	
	public MDCPagedEditorKit(DrawingSpecifications drawingSpecifications) {
		this.drawingSpecifications= drawingSpecifications;
	}
	
	public DrawingSpecifications getDrawingSpecifications()
	{
		return drawingSpecifications;
	}
	
	abstract public PagedLayout createPagedLayout();
	
	static public MDCPagedEditorKit getDefaultMDCPagedEditorKit() {
		return instance;
	}
}
