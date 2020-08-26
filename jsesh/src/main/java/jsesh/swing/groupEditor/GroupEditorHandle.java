/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import java.awt.Shape;

/**
 * A handle (square or round) for grabbing an element.
 * @author rosmord
 */
public class GroupEditorHandle {
    
    private final Shape shape;
    private final HandleVerticalPosition vpos;
    private final HandleHorizontalPosition hpos;

    /**
     * @param shape
     * @param vpos
     * @param hpos
     */
    public GroupEditorHandle(Shape shape, 
            HandleHorizontalPosition hpos,
            HandleVerticalPosition vpos) {
        this.shape = shape;
        this.vpos = vpos;
        this.hpos = hpos;
    }  

    public Shape getShape() {
        return shape;
    }

    public HandleVerticalPosition getVpos() {
        return vpos;
    }

    public HandleHorizontalPosition getHpos() {
        return hpos;
    }
    
    
}
