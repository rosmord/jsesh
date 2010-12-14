package jsesh.mdcDisplayer.viewToolkit.drawing;

import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

public interface FreePlacementDrawing extends Drawing {

	void putAt(GraphicalElement elt, double x, double y);
	
	void remove(GraphicalElement elt);
}
