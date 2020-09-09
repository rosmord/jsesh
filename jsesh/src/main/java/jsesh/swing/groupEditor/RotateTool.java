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
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 *
 * @author rosmord
 */
public class RotateTool implements GroupEditorListener {

    private final GroupEditor editor;

    private Point2D center;

    private Point2D originalPoint;

    private int originalAngle;

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
        if (e.getElementIndex() != -1 && e.isOnHandle()) {
            horizontalHandle = e.getHorizontalHandlePosition();
            verticalHandle = e.getVerticalHandlePosition();

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
            if (handleSelected) {
                Point2D p = e.getPoint();
                computeAngle(p).ifPresent(angle -> {
                    editor.rotate(angle);
                }
                );
            }
        }
    }

    /**
     * Compute the correct angle corresponding to a control in position "p". If
     * position "p" happens to be the origin, no angle can be computed. Hence
     * the "Optional" result here.
     *
     * @param p
     * @return
     */
    private Optional<Integer> computeAngle(Point2D p) {
        // Compute the vectors from origin to control points.
        Point2D v1 = new Point2D.Double(
                originalPoint.getX() - center.getX(),
                originalPoint.getY() - center.getY());
        Point2D v2 = new Point2D.Double(
                p.getX() - center.getX(),
                p.getY() - center.getY());
        // Normalize them and compute cos and sin.
        double d1 = v1.distance(0, 0);
        double d2 = v2.distance(0, 0);
        if (d1 >= 0 && d2 >= 0) {
            double cos = ensureLimits((v1.getX() * v2.getX() + v1.getY() * v2.getY())
                    / (d1 * d2));
            double sin = ensureLimits((v1.getX() * v2.getY() - v1.getY() * v2.getX())
                    / (d1 * d2));
            // Use cos and sin to compute the angle (in radian)
            double alpha = Math.acos(cos); // angle in radian.

            if (sin < 0.0) {
                alpha = 2 * Math.PI - alpha;
            }
            int angle = (int) ((alpha * 180.0 / Math.PI) + originalAngle);
            if (angle < 0) {
                angle += 360;
            } else if (angle >= 360) {
                angle -= 360;
            }
            return Optional.of(angle);
        } else {
            return Optional.empty();
        }

    }

    /**
     * Ensure the argument is between -1 and 1. Cos and sin are supposed to be
     * between -1 and 1, but small approximations might lead to values out of
     * range.
     *
     * @param d
     * @return d or -1 or 1, if d is out of range.
     */
    private static double ensureLimits(double d) {
        if (d < -1.0) {
            return -1.0;
        } else if (d > 1.0) {
            return 1.0;
        } else {
            return d;
        }
    }
}
