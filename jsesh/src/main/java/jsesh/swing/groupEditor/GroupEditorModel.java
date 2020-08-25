
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Model for the group editor.
 *
 * @author rosmord
 */
public class GroupEditorModel {

    private GroupEditorModelListener listener = () -> {
    };

    /**
     * The editedGroup we are editing.
     */
    private AbsoluteGroup editedGroup;

    /**
     * The current tool working on this group.
     */
    private GroupEditorTool currentTool = new DoNothingTool();

    private DrawingSpecification drawingSpecification;

    // the selected sign.
    private int selected = -1;

    /**
     * Gets the signs geometry, before a manipulation.
     *
     * @return
     */
    public List<SignGeometry> getGeometries() {
        ArrayList<SignGeometry> res = new ArrayList<>();
        for (int i= 0; i < editedGroup.getNumberOfChildren(); i++) {
            res.add(new SignGeometry(editedGroup.getHieroglyphAt(i)));
        }
        return res;
    }

    /**
     * Fix the signs geometries to a new value.
     *
     * @param newGeometries
     */
    public void setGeometries(List<SignGeometry> newGeometries) {
        for (int i= 0; i < editedGroup.getNumberOfChildren(); i++) {
            newGeometries.get(i).applyTo(editedGroup.getHieroglyphAt(i));
        }
    }

    public void setListener(GroupEditorModelListener listener) {
        this.listener = listener;
    }

    public boolean isEmpty() {
        return editedGroup == null;
    }

    public void setEditedGroup(AbsoluteGroup editedGroup) {
        this.editedGroup = editedGroup;
        notifyListener();
    }

    public MDCView getView() {
        MDCView view;
        if (editedGroup != null) {
            SimpleViewBuilder builder = new SimpleViewBuilder();
            view = builder.buildView(editedGroup, drawingSpecification);
        } else {
            view = new MDCView(null);
        }
        return view;
    }

    private void notifyListener() {
        this.listener.groupModified();
    }

    /**
     * Asks the current tool to draw his own specific information on a graphics.
     *
     * @param g
     */
    public void drawControls(Graphics2D g) {
        if (currentTool != null) {
            currentTool.drawControls(g);
        }
    }

    public void setSignGeometry(int signIndex, SignGeometry geometry) {
        if (signIndex != -1) {
            geometry.applyTo(editedGroup.getHieroglyphAt(signIndex));
            notifyListener();
        }
    }
    
    public SignGeometry getSignGeometry(int signIndex) {
        if (signIndex != -1) {
            return new SignGeometry(editedGroup.getHieroglyphAt(signIndex));
        } else
           throw new IllegalArgumentException("index out of range for sign "+ signIndex);
    }
}
