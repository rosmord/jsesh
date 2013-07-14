package jsesh.newEdit;

import jsesh.mdc.model.TopItemList;

import org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing;

/**
 * New variant of the Manuel de codage editor class.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
@SuppressWarnings("serial")
public class NewMDCEditor extends JGraphicalElementDisplayer<PlainDrawing> {

    private MDCDrawingManager drawingManager;

    public NewMDCEditor() {
        super(new PlainDrawing());
        drawingManager= new  MDCDrawingManager(new TopItemList(), getDrawing());
    }

    public void setMdC(String mdc) {
        drawingManager.setMdc(mdc);
    }

}
