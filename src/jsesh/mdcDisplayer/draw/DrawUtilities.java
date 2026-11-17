/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 7 déc. 2004
 *
 */
package jsesh.mdcDisplayer.draw;

import java.util.List;

import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.utilities.InnerGroupLister;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.RelativePosition;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * @author Serge Rosmorduc
 */
public class DrawUtilities {

    	/**
    	 * Build an absolute group from a list of top items.
    	 * The group will try to be as close as possible to the original top items disposition.
    	 * @param topItems
    	 * @param specs
    	 * @return a new AbsoluteGroup.
    	 */
    	public static AbsoluteGroup createAbsoluteGroupFrom(List topItems, DrawingSpecifications specs) {
    	    AbsoluteGroup result= null;
    	    
    	    if (topItems.size() == 1)
    	    {
    	        TopItem item= (TopItem) topItems.get(0);
    	        InnerGroup groups[]= InnerGroupLister.getInnerGroups(item);
    	        if (groups.length == 1 && groups[0] instanceof AbsoluteGroup) {
    	            result= (AbsoluteGroup) groups[0].deepCopy();
    	        }
    	    }
    	    if (result == null) {
    	        AbsoluteGroupComposer composer= new AbsoluteGroupComposer();
    	        composer.createAbsoluteGroupFrom(topItems, specs);
    	        result= composer.result;

    	    }
    	    return result;
    	}
    	
    	private static class AbsoluteGroupComposer extends ModelElementAdapter {
    	    
    	    AbsoluteGroup result;
    	    /**
    	     * Current view origin position.
    	     */
    	    float origx, origy;
    	    float scale;
    	    float sizeofA1;
    	    
    	    public AbsoluteGroup createAbsoluteGroupFrom(List topItems, DrawingSpecifications specs) {
    	        result= new AbsoluteGroup();
    	        SimpleViewBuilder builder= new SimpleViewBuilder(specs);
    	        sizeofA1= specs.getBaseLength();
    	        origx= 0;
    	        origy= 0;
    	        for (int i= 0; i < topItems.size(); i++) {
    	            float oldRefx= origx;
    	            scale= 1;
    	            MDCView v= builder.buildView((ModelElement) topItems.get(i));
    	            scanView(v);
    	            origx= oldRefx+ v.getWidth();
    	            origy= 0;
    	        }
    	        return result;
    	    }
    	    
    	    
    	    private void scanView(MDCView v) {
    	        origx= origx + v.getDx();
    	        origy= origy+ v.getDy();
    	        scale= scale*v.getXScale();
    	        
    	        v.getModel().accept(this);
    	        
    	        // Relative position of subview in the view.
    	        float px, py;
    	        px= 0;
    	        py= 0;
    	        RelativePosition prev= v.getFirstSubViewPosition();
    	        for (int i= 0; i< v.getNumberOfSubviews(); i++) {
    	            MDCView subv= v.getSubView(i);
    	            px= prev.computeXTranslation(v,px);
        	        py= prev.computeYTranslation(v,py);
    	            px= subv.getStartPoint().computeXTranslation(v,px);
        	        py= subv.getStartPoint().computeYTranslation(v,py);
        	        // Save position before entering the subview.
        	        float oldScale= scale;
        	        float oldx= origx;
        	        float oldy= origy;
        	        origx+= px*scale;
        	        origy+= py*scale;
        	        // Process subview
        	        scanView(subv);
        	        // Restore.
        	        origx= oldx;
        	        origy= oldy;
        	        scale= oldScale;
        	        prev= subv.getNextViewPosition();
    	        }
    	    }
    	    
    	    /* (non-Javadoc)
             * @see jsesh.mdc.model.ModelElementAdapter#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
             */
            public void visitHieroglyph(Hieroglyph oldh) {
                Hieroglyph h= (Hieroglyph) oldh.deepCopy();
                int xx= (int) (origx*1000.0/sizeofA1 );
                int yy= (int) (origy*1000.0/sizeofA1 );
                int s= (int) (scale*h.getRelativeSize());
                h.setExplicitPosition(xx, yy, s);
                result.addHieroglyph(h);
            }
    	}
}
