/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * Default mode for the group editor : moves elements.
 * @author rosmord
 */
public class MoveMode extends GroupEditorMode {

    @Override
    public GroupEditorHandle[] getHandles(GroupEditor editor, MDCView v) {
        return new GroupEditorHandle[0];
    }

    @Override
    public GroupEditorTool buildTool(GroupEditor editor) {
        return new MoveTool(editor);
    }

    @Override
    protected GroupEditorHandle buildHandle(double x, double y, double diameter, HandleHorizontalPosition hpos, HandleVerticalPosition vpos) {
        throw new UnsupportedOperationException("No handle for this mode");
    }
    
}
