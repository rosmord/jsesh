/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.layout;

import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

/**
 * 
 * TODO : change this class into an "abstract" factory.
 * This class is used as a repository of default options for the various objects used while building a view.
 * Default implementations can be retrieved with the static method provided.
 * Note that any modification made to the object returned by these methods will be global.
 * If you want to be more specific, provide custom made versions of these objects.
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
abstract public class MDCEditorKit
{

	private DrawingSpecification drawingSpecifications;
	

	private static MDCEditorKit basicMDCEditorKit= new MDCEditorKit(new DrawingSpecificationsImplementation()) {
		public Layout createLayout() {
			return new SimpleLayout();
		}
	};
	
	public static MDCEditorKit getBasicMDCEditorKit() {
			return basicMDCEditorKit;
	}

	
	public MDCEditorKit(DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications= drawingSpecifications;
	}

	/**
	 * Factory method for creating a layout for this kit.
	 * Layout objects should not be shared if one wants to start multiple threads on them.
	 * @return a new Layout.
	 */
	abstract public Layout createLayout();
	
	/**
	 * @return drawing specifications.
	 */
	public DrawingSpecification getDrawingSpecifications()
	{
		return drawingSpecifications;
	}
	
	/**
	 * @param specifications
	 */
	public void setDrawingSpecifications(DrawingSpecification specifications)
	{
		drawingSpecifications = specifications;
	}
}
