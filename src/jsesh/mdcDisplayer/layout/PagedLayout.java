/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.layout;

import java.awt.print.PageFormat;


/**
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
public interface PagedLayout extends Layout
{
	/**
	 * @param format
	 */
	public abstract void setPageFormat(PageFormat format);
}