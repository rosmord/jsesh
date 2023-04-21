package jsesh.newEdit;

import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.mdc.model.TopItemList;
import org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing;


/**
 * New variant of the Manuel de codage editor class.
 * <p> Note : we have two "viewToolKit" packages. 
 * One was made for Ramses, the other is being developped for JSesh.
 * At some point (better sooner than later) we will need to merge them.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
@SuppressWarnings("serial")
public class NewMDCEditor extends JGraphicalElementDisplayer<PlainDrawing> {

    private MDCDrawingManager drawingManager;

    public NewMDCEditor(HieroglyphicFontManager fontManager) {
        super(new PlainDrawing());        
        drawingManager = new MDCDrawingManager(new TopItemList(), getDrawing(), fontManager);
    }

    public void setMdC(String mdc) {
        drawingManager.setMdc(mdc);
    }
}
