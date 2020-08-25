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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author rosmord
 */
public class ResizeMode extends GroupEditorMode{

    @Override
    public GroupEditorTool buildTool(GroupEditor editor) {
        return new ResizeTool(editor);
    }

   @Override
    protected GroupEditorHandle buildHandle(double x, double y, double diameter, 
            HandleHorizontalPosition hpos,
            HandleVerticalPosition vpos) {       
        return new GroupEditorHandle(new Rectangle2D.Double(x, y, diameter, diameter), hpos, vpos);
    }
    
}
