package org.qenherkhopeshef.viewToolKit.drawing;

import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;



public interface FreePlacementDrawing extends Drawing {

	void putAt(GraphicalElement elt, double x, double y);
	
	void remove(GraphicalElement elt);
}
