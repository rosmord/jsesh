package org.qenherkhopeshef.viewtoolkit1.drawing;

import org.qenherkhopeshef.viewtoolkit1.drawing.element.GraphicalElement;



public interface FreePlacementDrawing extends Drawing {

	void putAt(GraphicalElement elt, double x, double y);
	
	void remove(GraphicalElement elt);
}
