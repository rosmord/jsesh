
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 *
 * @author rosmord
 */
public class GroupEditorDrawingPreferences {

    /**
     * Frame line width.
     */
    private int lineWidth = 2;  
    private double scale = 10;
    private double topMargin = 5;
    private double sideMargin = 5;
   
    /**
     * The drawing specifications. TODO find a good policy for drawing
     * specifications !!!!
     */
    private DrawingSpecification drawingSpecifications = MDCEditorKit.getBasicMDCEditorKit().getDrawingSpecifications();
    
    /**
     * Handle sizes
     */
    private int handleSize = 10;

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(double topMargin) {
        this.topMargin = topMargin;
    }

    public double getSideMargin() {
        return sideMargin;
    }

    public void setSideMargin(double sideMargin) {
        this.sideMargin = sideMargin;
    }

    public DrawingSpecification getDrawingSpecifications() {
        return drawingSpecifications;
    }

    public void setDrawingSpecifications(DrawingSpecification drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications;
    }

    public int getHandleSize() {
        return handleSize;
    }

    public void setHandleSize(int handleSize) {
        this.handleSize = handleSize;
    }
    
    
}
