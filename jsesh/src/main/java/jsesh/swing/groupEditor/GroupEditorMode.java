/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import java.awt.geom.Point2D;
import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * The mode (MOVE, RESIZE, ROTATE, mainly) for the group editor.
 * <p>Modes are primarily stateless. Thus, they may be shared and reused.
 * The {@link  GroupEditorListener} they create are used to keep the states of
 * ongoing manipulations.
 * @author rosmord
 */
public abstract class GroupEditorMode {

    /**
     * Build a suitable tool for this mode.
     *
     * @param editor
     * @return
     */
    public abstract GroupEditorListener buildTool(GroupEditor editor);

    /**
     * Create an handle (if relevant for the current mode).
     * 
     * @param x
     * @param y
     * @param diameter
     * @param hpos
     * @param vpos
     * @return a handle
     * @throws UnsupportedOperationException if the current mode doesn't provide handles.
     */
    protected abstract GroupEditorHandle buildHandle(
            double x, double y,
            double diameter, 
            HandleHorizontalPosition hpos,
            HandleVerticalPosition vpos);
    
    /**
     * Build the list of handles for a sign in this mode.
     *
     * @param v the view.
     * @return the handles.
     */
    public GroupEditorHandle[] getHandles(GroupEditor editor, MDCView v) {
        GroupEditorDrawingPreferences prefs = editor.getGroupEditorDrawingPreferences();
        GroupEditorHandle result[] = new GroupEditorHandle[8];
        Point2D orig = editor.getViewPosition(v);
        double w = v.getWidth();
        double h = v.getHeight();
        double radius = (prefs.getHandleSize() / (2 * prefs.getScale()));
        double diameter = 2 * radius;
        int i = 0;

        result[i++] = buildHandle(orig.getX() - radius, orig.getY() - radius,
                diameter, HandleHorizontalPosition.LEFT, HandleVerticalPosition.TOP);
        result[i++] = buildHandle(orig.getX() - radius + w / 2, orig.getY()
                - radius, diameter, HandleHorizontalPosition.MIDDLE,
                HandleVerticalPosition.TOP);
        result[i++] = buildHandle(orig.getX() - radius + w, orig.getY()
                - radius, diameter, HandleHorizontalPosition.RIGHT,
                HandleVerticalPosition.TOP);
        result[i++] = buildHandle(orig.getX() - radius, orig.getY() + h / 2
                - radius, diameter, HandleHorizontalPosition.LEFT,
                HandleVerticalPosition.MIDDLE);
        result[i++] = buildHandle(orig.getX() - radius + w, orig.getY() + h / 2
                - radius, diameter, HandleHorizontalPosition.RIGHT,
                HandleVerticalPosition.MIDDLE);
        result[i++] = buildHandle(orig.getX() - radius, orig.getY() + h
                - radius, diameter, HandleHorizontalPosition.LEFT,
                HandleVerticalPosition.BOTTOM);
        result[i++] = buildHandle(orig.getX() - radius + w / 2, orig.getY() + h
                - radius, diameter, HandleHorizontalPosition.MIDDLE,
                HandleVerticalPosition.BOTTOM);
        result[i++] = buildHandle(orig.getX() - radius + w, orig.getY() + h
                - radius, diameter, HandleHorizontalPosition.RIGHT,
                HandleVerticalPosition.BOTTOM);
        return result;
    }
}
