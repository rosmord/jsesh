package jsesh.mdcDisplayer.viewToolkit.temp;

import jsesh.mdc.model.ModelElement;
import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

import org.qenherkhopeshef.algo.ReversibleMultiHashMap;

/**
 * Paginated view for a manuel de codage document.
 * @author rosmord
 *
 */
public class MDCDocumentView {
	/**
	 * Ensure n-n mapping between modelElements and their views.
	 * (notice that is possible that some GraphicalElement are *not* linked to a particular ModelElement)
	 * For instance, if we add page numbers and the like.
	 */
	
	private ReversibleMultiHashMap<ModelElement, GraphicalElement> elementMap= new ReversibleMultiHashMap<ModelElement, GraphicalElement>();


	private ReversibleMultiHashMap<Integer, GraphicalElement> pageMap= new ReversibleMultiHashMap<Integer, GraphicalElement>();


	public void recomputeViewFor(ModelElement element) {
		// TODO Auto-generated method stub
		// Remove the existing view 
		// create new view(s)
		// insert them in the map
		
		// layout ...
		
		// The complex factor here is how we call layout.
		
		// Layout algorithm idea : let a be a span of text to layout.
		// knowing the position of the last views, place the new view.
		
		
		// we want some kind of locality here  
		
		// the idea : find which unit of text we want to layout again.
		// layout this unit
		// then transfer the problem to the parent unit. And so on.
		
		// Currently, we WON'T deal with page change. That is, there is no page change unless
		// there is an explicit page skip.
		
		// So : at the time being, the range to "re-layout" is more or less the whole page.
		// (if there are tabulations, a tab in the bottom of the page may cause a layout of the whole page.
		// barring this kind of problems, the range is indeed from the first modified item to the end of the page.
		//
		// Better still : let the layout system recompute views from the first modified item to the end of the page.
		// then, in the light of the tabbing positions, decide whether a simple second path to align the tabbed 
		// signs is sufficient, or if we need a complete recomputation.
		
		
	}


	public void removeViewForElementRange(int start, int end) {
		// TODO Auto-generated method stub
		
	}


	public void insertViewFor(ModelElement element, int index) {
		// TODO Auto-generated method stub
		
	}


	public void replaceViews(int start, int end) {
		// TODO Auto-generated method stub
		
	}
	
}
