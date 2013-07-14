package org.qenherkhopeshef.viewToolKit.drawing;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import org.qenherkhopeshef.viewToolKit.drawing.event.*;
import org.qenherkhopeshef.viewToolKit.drawing.element.*;

/**
 * A free position drawing, in which element access is speeded by a grid-like
 * indexation.
 *
 * @author rosmord
 */
public class GridDrawing extends AbstractDrawing {

    private double gridWidth;
    private double gridHeight;

    @Override
    protected void eventOccurredInDrawing(DrawingEvent ev) {
        // TODO Auto-generated method stub
    }

    @Override
    public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Rectangle2D getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Rectangle2D getCursorBounds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
