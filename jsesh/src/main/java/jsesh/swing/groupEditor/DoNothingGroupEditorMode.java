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
 * A No-op group editor.
 *
 * @author rosmord
 */
public class DoNothingGroupEditorMode extends GroupEditorMode {

    @Override
    public GroupEditorTool buildTool(GroupEditor editor) {
        return new DoNothingTool();
    }

    @Override
    protected GroupEditorHandle buildHandle(double x, double y, double diameter, HandleHorizontalPosition hpos, HandleVerticalPosition vpos) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public GroupEditorHandle[] getHandles(GroupEditor editor, MDCView v) {
        return new GroupEditorHandle[0];
    }
}
