package org.qenherkhopeshef.viewToolKit.drawing.element.layout;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.Margin;

/**
 *
 * @author rosmord
 */
public class VerticalCompositeLayout implements CompositeLayout {

    @Override
    public void layoutComposite(CompositeElement composite) {
        double w = 0;
        double y = 0;
        for (GraphicalElement elt : composite) {
            Dimension2D pref = elt.getTotalPreferredSize();
            if (pref.getWidth() > w) {
                w = pref.getWidth();
            }
        }
        for (GraphicalElement elt : composite) {
            Dimension2D pref = elt.getTotalPreferredSize();
            Margin m = elt.getMargin();
            elt.setOrigin(m.getLeft(), y + m.getTop());
            y += pref.getHeight();
        }
        final Rectangle2D bounds = composite.getBounds();
        composite.setBounds(bounds.getMinX(), bounds.getMinY(), w, y);
    }

    @Override
    public Dimension2D minimalSize(CompositeElement composite) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
