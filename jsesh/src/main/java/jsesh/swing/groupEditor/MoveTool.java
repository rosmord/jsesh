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

/**
 *
 * @author rosmord
 */
public class MoveTool implements GroupEditorListener {

    private final GroupEditor editor;

    private Point2D oldPoint;

    /**
     * @param editor
     */
    public MoveTool(GroupEditor editor) {
        this.editor = editor;
    }

    @Override
    public void mousePressed(GroupEditorEvent e) {
        editor.setSelected(e.getElementIndex());
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
            // Move the shape.
            Point2D p = e.getPoint();
            double dx = p.getX() - oldPoint.getX();
            double dy = p.getY() - oldPoint.getY();
            oldPoint.setLocation(p.getX(), p.getY());
            editor.move(dx, dy);
        }
    }

}
