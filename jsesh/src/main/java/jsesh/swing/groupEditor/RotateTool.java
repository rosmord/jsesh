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
 * Rotation tool.
 * <p>One grabs an handle. Rotation is then performed around the center of the glyph.
 * <p>TODO : rotation around the <em>opposite</em> handle.
 * <p>During the rotation, the rotation handle is drawn in red;
 * the handles themselves are not drawn at that time. 
 * 
 * @author rosmord
 */
public class RotateTool implements GroupEditorTool {

    private final GroupEditor editor;

    private Point2D rotationCenter;
    
    private Point2D rotationStart;
    
    private SignGeometry startSignGeometry;
    
    private Point2D oldPoint;

    private HandleHorizontalPosition horizontalHandle;

    private HandleVerticalPosition verticalHandle;

    private boolean handleSelected;

    /**
     * @param editor
     */
    public RotateTool(GroupEditor editor) {
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
                editor.rotate(oldPoint, p);
                oldPoint.setLocation(p.getX(), p.getY());
            }
        }
    }
}
