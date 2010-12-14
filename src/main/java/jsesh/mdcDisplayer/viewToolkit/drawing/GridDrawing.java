package jsesh.mdcDisplayer.viewToolkit.drawing;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

import jsesh.mdcDisplayer.viewToolkit.drawing.event.DrawingEvent;
import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

/**
 * A free position drawing, in which element access is speeded by a grid-like indexation.
 * @author rosmord
 */
public class GridDrawing extends AbstractDrawing {

	private double gridWidth;
	private double gridHeight;
	
	@Override
	protected void eventOccurrentInDrawing(DrawingEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
		// TODO Auto-generated method stub
		return null;
	}

	public Rectangle2D getPreferredSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

}
