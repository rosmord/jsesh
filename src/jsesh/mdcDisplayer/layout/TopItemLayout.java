/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 déc. 2004
 *
 */
package jsesh.mdcDisplayer.layout;

import java.awt.geom.Rectangle2D;

import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * A topitem layout knows how to place an element in a topitem.
 * <p>
 * Generic mecanism used for laying out the text :
 * a topitemlayout will arrange text in "regions". Regions are basically
 * rectangular zones of text. In certain circumstances, regions will be <em>flushed</em>.
 * This means that the first position in the region will be set, and that the global view dimensions
 * will be modified accordingly.
 * <p> Flushing will typically occur at page breaks, or when the text organisation changes.
 * @author Serge Rosmorduc
 */
abstract public class TopItemLayout {
    //TopItemLayoutPosition state;
    
    /**
     * Sets an subview position in the global view.
     * <p> update the state. 
     * @param subView
     */
    public abstract void layoutElement(MDCView subView);
    
    /**
     * Called when all elements have been processed.
     *
     */
    public abstract void endLayout();
    
    /*
     * @return Returns the state.
     *
    public TopItemLayoutPosition getState() {
        return state;
    }*/
    
    /*
     * @param state The state to set.
     *
    public void setState(TopItemLayoutPosition state) {
        this.state = state;
    }*/

    /**
     * Initialize the state, when this layout is the main layout for the text. 
     */
    public abstract void startLayout();

    /**
     * Returns the total area for the document, after computation.
     * 
     * @return the area for the complete document.
     */
    public abstract Rectangle2D getDocumentArea();
}
