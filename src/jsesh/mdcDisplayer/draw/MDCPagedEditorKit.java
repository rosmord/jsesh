/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.draw;

import jsesh.mdcDisplayer.layout.PagedLayout;
import jsesh.mdcDisplayer.layout.SimplePrinterLayout;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
public abstract class MDCPagedEditorKit
{

    private static MDCPagedEditorKit instance= new MDCPagedEditorKit(new DrawingSpecifications()) {
		PagedLayout createPagedLayout() {
	      return new SimplePrinterLayout();
	   }
    };
    
	private DrawingSpecifications drawingSpecifications;
	
	public MDCPagedEditorKit(DrawingSpecifications drawingSpecifications) {
		this.drawingSpecifications= drawingSpecifications;
	}
	
	DrawingSpecifications getDrawingSpecifications()
	{
		return drawingSpecifications;
	}
	
	abstract PagedLayout createPagedLayout();
	
	static public MDCPagedEditorKit getDefaultMDCPagedEditorKit() {
		return instance;
	}
}
