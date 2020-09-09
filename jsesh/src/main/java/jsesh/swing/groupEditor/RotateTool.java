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
import java.util.Optional;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.swing.utils.GeometryHelper;

/**
 *
 * @author rosmord
 */
public class RotateTool implements GroupEditorListener {

    private final GroupEditor editor;

    private Point2D center;

    private Point2D originalPoint;

    private int originalAngle;

    /**
     * @param editor
     */
    public RotateTool(GroupEditor editor) {
        this.editor = editor;
    }

    @Override
    public void mousePressed(GroupEditorEvent e) {
        editor.setSelected(e.getElementIndex());
        if (e.getElementIndex() != -1 && e.isOnHandle()) {
            MDCView v = editor.getSelectedView();
            originalAngle = editor.getSelectedSign().getAngle();
            Point2D orig = editor.getViewPosition(v);
            center = new Point2D.Double(orig.getX() + v.getWidth() / 2,
                    orig.getY() + v.getHeight() / 2);

            originalPoint = new Point2D.Double(e.getPoint().getX(), e.getPoint()
                    .getY());

        }
    }

    @Override
    public void mouseReleased(GroupEditorEvent e) {
        originalPoint = null;
        center = null;
    }

    @Override
    public void mouseDragged(GroupEditorEvent e) {
        if (editor.getSelected() != -1 && originalPoint != null) {
            Point2D p = e.getPoint();
            computeAngle(p).ifPresent(angle -> {
                editor.rotate(angle);
            }
            );
        }
    }

    /**
     * Compute the correct angle corresponding to a control in position "p". If
     * position "p" happens to be the origin, no angle can be computed. Hence
     * the "Optional" result here.
     *
     * Note that the angle computed is the sum of the angle between the control
     * points and the original angle for the sign.
     *
     * @param p
     * @return
     */
    private Optional<Integer> computeAngle(Point2D p) {
        return GeometryHelper.computeAngle(center, originalPoint, p)
                .map(a -> a + originalAngle)
                .map(GeometryHelper::normalizeAngle);
    }

}
