/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.viewToolKit.drawing.element.layout;

import java.awt.geom.Dimension2D;
import org.qenherkhopeshef.viewToolKit.drawing.element.CompositeElement; 

/**
 * The layout for a given composite.
 * @author rosmord
 */
public interface CompositeLayout {

    /**
     * Compute the minimal size a composite element would have with this layout.
     * @param composite
     * @return 
     */
    
    Dimension2D minimalSize(CompositeElement composite);
    
    /**
     * Layout the children of a composite element.
     * @param composite 
     */
    void layoutComposite(CompositeElement composite);
    
}
