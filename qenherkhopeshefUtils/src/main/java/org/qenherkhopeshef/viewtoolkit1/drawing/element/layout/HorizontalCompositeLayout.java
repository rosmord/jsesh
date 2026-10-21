package org.qenherkhopeshef.viewtoolkit.drawing.element.layout;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewtoolkit.drawing.element.CompositeElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.Margin;

/// Places the children of a composite from left to right.
///
/// The children are stretched to the composite's height.
///
/// @author rosmord
public class HorizontalCompositeLayout implements CompositeLayout {

    @Override
    public void layoutComposite(CompositeElement composite) {
        double x = 0;
        for (GraphicalElement elt : composite) {
            Dimension2D pref = elt.getTotalPreferredSize();
            Margin m = elt.getMargin();
            elt.setOrigin(x + m.getLeft(), m.getTop());
            x += pref.getWidth();
        }
        Dimension2D size = minimalSize(composite);
        final Rectangle2D bounds = composite.getBounds();
        composite.setBounds(bounds.getMinX(), bounds.getMinY(), size.getWidth(), size.getHeight());
    }

    @Override
    public Dimension2D minimalSize(CompositeElement composite) {
        double w = 0;
        double h = 0;
        for (GraphicalElement elt : composite) {
            Dimension2D pref = elt.getTotalPreferredSize();
            w += pref.getWidth();
            h = Math.max(h, pref.getHeight());
        }
        return new DoubleDimensions(w, h);
    }

    @Override
    public void fitChildren(CompositeElement composite, double width, double height) {
        for (GraphicalElement elt : composite) {
            Rectangle2D b = elt.getBounds();
            Margin m = elt.getMargin();
            elt.setBounds(b.getMinX(), b.getMinY(), b.getWidth(), height - m.getTotalMarginHeight());
        }
    }
}
