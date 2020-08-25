/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import java.awt.Graphics;

/**
 * Base interface for tools.
 * 
 * @author rosmord
 */
public interface GroupEditorTool extends GroupEditorListener {
    /**
     * Draws the information about the current tool state.
     * @param g 
     */
    default void drawControls(Graphics g) {} 
}
