/*
 * Created on 17 oct. 2004
 *
 */
package jsesh.mdcDisplayer.layout;

import jsesh.drawingspecifications.JSeshStyle;

/**
 * Shared default values for JSesh widget preferences.
 * 
 *  When a programmer wants a quick and simple way to create JSesh objects, without proceeding to the whole
 *  configuration business, the EditorKit is the place to use.
 *  
 *  It should be the only actual singleton used by JSesh, and it should only be used by top level components,
 *  except if the application programmer wants to manage everything on his own.
 *  
 * Note that any modification made to the object returned by these methods will be global.
 * If you want to be more specific, provide custom made versions of these objects.
 * 
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
abstract public class MDCEditorKit
{

	private JSeshStyle jseshStyle;
	

	private static MDCEditorKit basicMDCEditorKit = buildMDCEditorKit();
	
	private static MDCEditorKit buildMDCEditorKit() {
		 return new MDCEditorKit(JSeshStyle.DEFAULT) {
				public Layout createLayout() {
					return new Layout();
				}
			};
	}
	
	public static MDCEditorKit getBasicMDCEditorKit() {
			return basicMDCEditorKit;
	}

	
	public MDCEditorKit(JSeshStyle jseshStyle) {
		this.jseshStyle= jseshStyle;
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
	public JSeshStyle getJseshStyle()
	{
		return jseshStyle;
	}
	
	/**
	 * @param jseshStyle
	 */
	public void setJseshStyle(JSeshStyle jseshStyle)
	{
		this.jseshStyle = jseshStyle;
	}
}
