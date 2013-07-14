package jsesh.newEdit;

import java.util.logging.Level;
import java.util.logging.Logger;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.EmbeddedModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;

import jsesh.mdc.model.TopItemList;
import org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
/**
 * Manages the synchronization between a mdc text and the drawing which
 * represents it.
 *
 * <p> The current version is mainly a test in order to create the necessary
 * primitives. Afterwards, a proper management system will be created.
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
        drawing.clear();
        for (int i = 0; i < items.getNumberOfChildren(); i++) {
            EmbeddedModelElement item = items.getChildAt(i);
            DrawingElementsFactory fact = new DrawingElementsFactory();
            fact.processElements(item);
            for (GraphicalElement g : fact.getElements()) {
                drawing.addElement(g);

            }
        }
    }

    public void setMdc(String mdc) {
        try {
            MDCParserModelGenerator mDCParserModelGenerator = new MDCParserModelGenerator();
            items = mDCParserModelGenerator.parse(mdc);
            layout();
        } catch (MDCSyntaxError ex) {
            items = new TopItemList();
            Logger.getLogger(MDCDrawingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class DrawingElementsFactory extends ModelElementDeepAdapter {

        public DrawingElementsFactory() {
        }
        
        /**
         * Returns the graphical elements created by this factory.
         * @return a list of graphical elements.
         */
        private Iterable<GraphicalElement> getElements() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }


        /**
         * Process the element and create its representation.
         * @param item 
         */
        public void processElements(EmbeddedModelElement item) {
            item.accept(this);
        }
        
        
    }
}
