/*
 * Created on 17 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.draw;

/**
 * A base for implementing view builders.
 * @author Serge Rosmorduc
 *
 * This file is free software under the Gnu Lesser Public License.
 */
public abstract class AbstractViewBuilder implements ViewBuilder
{	
	private DrawingSpecifications drawingSpecifications;

	protected AbstractViewBuilder(DrawingSpecifications drawingSpecifications)
	{
		setDrawingSpecifications(drawingSpecifications);
	}

	/**
	 * @return
	 */
	public DrawingSpecifications getDrawingSpecifications()
	{
		return drawingSpecifications;
	}

	/**
	 * @param specifications
	 */
	public void setDrawingSpecifications(DrawingSpecifications specifications)
	{
		drawingSpecifications = specifications;
	}

}
