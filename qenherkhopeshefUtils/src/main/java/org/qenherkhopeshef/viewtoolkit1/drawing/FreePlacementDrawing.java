package org.qenherkhopeshef.viewtoolkit.drawing;

import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;



public interface FreePlacementDrawing extends Drawing {

	void putAt(GraphicalElement elt, double x, double y);
	
	void remove(GraphicalElement elt);
}
