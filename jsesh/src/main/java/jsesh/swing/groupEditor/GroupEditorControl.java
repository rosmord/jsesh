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
 * Control for a group editor.
 * 
 * @author rosmord
 *  
 */
class GroupEditorControl implements GroupEditorListener {
    private final GroupEditor editor;

    private Point2D oldPoint;

    private int horizontalHandle;

    private int verticalHandle;

    private boolean handleSelected;

    /**
     * @param editor
     */
    public GroupEditorControl(GroupEditor editor) {
        this.editor = editor;
        handleSelected = false;
    }
  
    @Override
    public void mouseClicked(GroupEditorEvent e) {
        // NOTHING
    }

    @Override
    public void mouseEntered(GroupEditorEvent e) {
        // NOTHING
    }

    @Override
    public void mouseExited(GroupEditorEvent e) {
       // NOTHING
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
                if (editor.getMode() == GroupEditor.RESIZE) {
                    Point2D p = e.getPoint();
                    double dx = p.getX() - oldPoint.getX();
                    double dy = p.getY() - oldPoint.getY();
                    editor.resizeTo(dx, dy, horizontalHandle, verticalHandle);
                    oldPoint.setLocation(p.getX(), p.getY());
                } else if (editor.getMode()== GroupEditor.ROTATION){
                    // Rotation :
                    //Graphics2D g= (Graphics2D) editor.getGraphics();
                    Point2D p= e.getPoint();
                    editor.rotate(oldPoint, p);
                    oldPoint.setLocation(p.getX(), p.getY());
                    //g.dispose();
                }
            }
            else {
            	// Move the shape.
                Point2D p = e.getPoint();
                double dx = p.getX() - oldPoint.getX();
                double dy = p.getY() - oldPoint.getY();
                oldPoint.setLocation(p.getX(), p.getY());
                editor.move(dx, dy);
            }

        }
    }

   
    @Override
    public void mouseMoved(GroupEditorEvent e) {
        // NOTHING.
    }

}