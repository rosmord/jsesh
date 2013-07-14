package jsesh.newEdit;

import org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing;

import jsesh.mdc.model.TopItemList;

/**
 * Manages the synchronization between a mdc text and the drawing which
 * represents it.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
public class MDCDrawingManager {
    private TopItemList items;
    private PlainDrawing drawing;

    public MDCDrawingManager(TopItemList items, PlainDrawing drawing) {
        super();
        this.items = items;
        this.drawing = drawing;
    }

    public void layout() {

    }

    public void setMdc(String mdc) {
        
    }

}
