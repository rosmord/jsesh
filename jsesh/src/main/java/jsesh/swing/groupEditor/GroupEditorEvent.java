/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 *
 * Created on 30 nov. 2004
 */
package jsesh.swing.groupEditor;

import java.awt.geom.Point2D;

import jsesh.mdc.model.AbsoluteGroup;

/**
 * Represent a mouse action over a group editor.
 *
 * @author rosmord
 */
public class GroupEditorEvent {

    private final Point2D point;
    private final AbsoluteGroup group;
    private final int elementIndex;
    private final boolean onHandle;
    private HandleHorizontalPosition horizontalHandlePosition;
    private HandleVerticalPosition verticalHandlePosition;

    /**
     * Create an event for a mouse click or down on an absolute group.
     *
     * @param group : the group being edited
     * @param point
     * @param elementIndex : the element in the group
     */
    public GroupEditorEvent(AbsoluteGroup group, Point2D point, int elementIndex) {
        super();
        this.group = group;
        this.point = point;
        this.elementIndex = elementIndex;
        this.onHandle = false;
    }


    /**
     * Get the mouse event point, in model coordinates.
     *
     * @return the mouse event point, in model coordinates
     */
    public Point2D getPoint() {
        return point;
    }

    public HandleHorizontalPosition getHorizontalHandlePosition() {
        return horizontalHandlePosition;
    }

    public HandleVerticalPosition getVerticalHandlePosition() {
        return verticalHandlePosition;
    }

   
    

    /**
     * Event for mouse events on an handle.
     *
     * @param group
     * @param point
     * @param elementIndex
     * @param handleHorizontalPosition
     * @param handleVerticalPosition
     */
    public GroupEditorEvent(AbsoluteGroup group, Point2D point, int elementIndex,
            HandleHorizontalPosition handleHorizontalPosition,
            HandleVerticalPosition handleVerticalPosition) {
        this.group = group;
        this.point = point;
        this.elementIndex = elementIndex;
        this.onHandle = true;
        this.horizontalHandlePosition = handleHorizontalPosition;
        this.verticalHandlePosition = handleVerticalPosition;
    }

    /**
     * Return the element on which this event occurred, or -1 if none.
     *
     * @return the index of the element on which this event occurred, or -1 if
     * none
     */
    public int getElementIndex() {
        return elementIndex;
    }

    public AbsoluteGroup getGroup() {
        return group;
    }

    public boolean isOnHandle() {
        return onHandle;
    }
}
