package org.qenherkhopeshef.viewToolKit.drawing.element.layout;

import java.awt.geom.Dimension2D;
import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeElement; 

/**
 * The layout for a given composite.
 * @author rosmord
 */
public interface CompositeLayout {

    /// Compute the minimal size a composite element would have with this layout.
    ///
    /// This must not modify the composite or its children.
    Dimension2D minimalSize(CompositeElement composite);
    
    /**
     * Layout the children of a composite element.
     * @param composite 
     */
    void layoutComposite(CompositeElement composite);

    /// Adapts the children of a composite to the space it has been given.
    ///
    /// Called when the composite bounds are set. Typically, children are
    /// stretched along the cross axis of the layout.
    ///
    /// @param composite the composite, whose size has just been set.
    /// @param width the composite's new width.
    /// @param height the composite's new height.
    void fitChildren(CompositeElement composite, double width, double height);
    
}
