package jsesh.newEdit;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.ShapeChar;

import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.utils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.ObjectProperty;

public class HieroglyphicElement extends GraphicalElement {

    ObjectProperty<String> mdcProperty;

    public HieroglyphicElement(String mdc) {
        super();
        this.mdcProperty = new ObjectProperty<String>("mdc", this, mdc, true);
    }

    @Override
    public void drawElement(Graphics2D g) {
        // We will replace the code by the shape at some point...
        ShapeChar shape = DefaultHieroglyphicFontManager.getInstance().get(mdcProperty.getValue());
        g.draw(shape.getShape());
    }

    @Override
    public Dimension2D getPreferredSize() {
        Rectangle2D textBounds;
        ShapeChar shape = DefaultHieroglyphicFontManager.getInstance().get(mdcProperty.getValue());
        textBounds = shape.getBbox();
        return new DoubleDimensions(textBounds.getWidth(), textBounds.getHeight());
    }

    private MDCDrawingFacade getDrawingFacade() {
        return new MDCDrawingFacade();
    }

    public void setMdc(String mdc) {
        this.mdcProperty.setValue(mdc);
    }

    public String getMdc() {
        return mdcProperty.getValue();
    }

    @Override
    public String toString() {
        return "[mdc " + mdcProperty + "]";
    }
}
