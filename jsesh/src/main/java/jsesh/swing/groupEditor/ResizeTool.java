/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.swing.groupEditor;

import java.awt.geom.Point2D;

/**
 *
 * @author rosmord
 */
public class ResizeTool implements GroupEditorListener {

    private final GroupEditor editor;

    private Point2D oldPoint;

    private HandleHorizontalPosition horizontalHandle;

    private HandleVerticalPosition verticalHandle;

    private boolean handleSelected;

    /**
     * @param editor
     */
    public ResizeTool(GroupEditor editor) {
        this.editor = editor;
        handleSelected = false;
    }

    @Override
    public void mousePressed(GroupEditorEvent e) {
        editor.setSelected(e.getElementIndex());
        handleSelected = e.isOnHandle();
        if (e.isOnHandle()) {
            horizontalHandle = e.getHorizontalHandlePosition();
            verticalHandle = e.getVerticalHandlePosition();
        }
        if (e.getElementIndex() != -1) {
            oldPoint = new Point2D.Double(e.getPoint().getX(), e.getPoint()
                    .getY());
        }
    }

    @Override
    public void mouseReleased(GroupEditorEvent e) {
        oldPoint = null;
    }

    @Override
    public void mouseDragged(GroupEditorEvent e) {
        if (editor.getSelected() != -1 && oldPoint != null) {
            if (handleSelected) {
                Point2D p = e.getPoint();
                double dx = p.getX() - oldPoint.getX();
                double dy = p.getY() - oldPoint.getY();
                editor.resizeTo(dx, dy, horizontalHandle, verticalHandle);
                oldPoint.setLocation(p.getX(), p.getY());
            }
        }
    }
}
