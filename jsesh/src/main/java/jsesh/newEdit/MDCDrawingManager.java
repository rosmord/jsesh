package jsesh.newEdit;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.EmbeddedModelElement;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElementDeepAdapter;

import jsesh.mdc.model.TopItemList;
import org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.Alignment;
import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeAxis;
import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.RectangleElement;

/**
 * Manages the synchronization between a mdc text and the drawing which
 * represents it.
 *
 * <p>
 * The current version is mainly a test in order to create the necessary
 * primitives. Afterwards, a proper management system will be created.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 * TODO : aggregate font manager in a preference object, which will fire changed events to trigger new layouts.
 *
 */
public class MDCDrawingManager {

    private TopItemList items;
    private PlainDrawing drawing;
    private HieroglyphicFontManager fontManager;

    public MDCDrawingManager(TopItemList items, PlainDrawing drawing, HieroglyphicFontManager fontManager) {
        super();
        this.fontManager = fontManager;
        this.items = items;
        this.drawing = drawing;
        this.fontManager = fontManager;
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

    private  class DrawingElementsFactory extends ModelElementDeepAdapter {

        ArrayList<GraphicalElement> elements = new ArrayList<GraphicalElement>();

        /**
         * Returns the graphical elements created by this factory.
         *
         * @return a list of graphical elements.
         */
        private Iterable<GraphicalElement> getElements() {
            return elements;
        }

        /**
         * Process the element and create its representation.
         *
         * @param item
         */
        public void processElements(EmbeddedModelElement item) {
            item.accept(this);
        }

        @Override
        public void visitCadrat(Cadrat c) {
            ArrayList<GraphicalElement> old = elements;
            elements = new ArrayList<GraphicalElement>();
            // Elements contain views for all children of b.
            CompositeElement e = new CompositeElement();
            e.setAxis(CompositeAxis.VERTICAL);
            e.setAlignment(Alignment.FILL);
            for (int i = 0; i < c.getNumberOfChildren(); i++) {
                c.getChildAt(i).accept(this);
            }
            for (GraphicalElement sub : elements) {
                e.addElement(sub);
            }
            e.setDecorator(new RectangleElement());
            e.pack();
            old.add(e);
            elements = old;
        }

        @Override
        public void visitHBox(HBox b) {
            // DUPLICATE CODE. FACTOR IT !!!
            ArrayList<GraphicalElement> old = elements;
            elements = new ArrayList<GraphicalElement>();
            // Elements contain views for all children of b.
            CompositeElement e = new CompositeElement();
            e.setAxis(CompositeAxis.HORIZONTAL);
            e.setAlignment(Alignment.FILL);
            for (int i = 0; i < b.getNumberOfChildren(); i++) {
                b.getChildAt(i).accept(this);
            }
            for (GraphicalElement sub : elements) {
                e.addElement(sub);
            }
            e.setDecorator(new RectangleElement());

            e.pack();
            old.add(e);
            elements = old;

        }

        @Override
        public void visitHieroglyph(Hieroglyph h) {
            elements.add(new HieroglyphicElement(h.getCode(), fontManager));
        }

    }
}
