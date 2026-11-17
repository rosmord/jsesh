/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file ExportData.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphicExport;

import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.swing.editor.HieroglyphicTextModel;

/**
 * Data needed for text exportation.
 * TODO : suppress caret provider from ExportData, and replace it with a Range class.
 * @author rosmord
 */
public class ExportData {
    private DrawingSpecifications drawingSpecifications;
    private CaretProvider caretProvider;
    private HieroglyphicTextModel data;
    private double scale;
    
    
    /**
     * @param drawingSpecifications
     * @param caretProvider
     * @param data
     */
    public ExportData(DrawingSpecifications drawingSpecifications,
            CaretProvider caretProvider, HieroglyphicTextModel data, double scale) {
        super();
        this.drawingSpecifications = drawingSpecifications;
        this.caretProvider = caretProvider;
        this.data = data;
        this.scale= scale;
    }
    
    
    public CaretProvider getCaretProvider() {
        return caretProvider;
    }
    public void setCaretProvider(CaretProvider caretProvider) {
        this.caretProvider = caretProvider;
    }
    public HieroglyphicTextModel getData() {
        return data;
    }
    public void setData(HieroglyphicTextModel data) {
        this.data = data;
    }
    public DrawingSpecifications getDrawingSpecifications() {
        return drawingSpecifications;
    }
    public void setDrawingSpecifications(
            DrawingSpecifications drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications;
    }
    /**
     * @return Returns the scale.
     */
    public double getScale() {
        return scale;
    }
    
    /**
     * @param scale The scale to set.
     */
    public void setScale(double scale) {
        this.scale = scale;
    }
}
