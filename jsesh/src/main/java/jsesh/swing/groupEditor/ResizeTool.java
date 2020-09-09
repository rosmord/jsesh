/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.swing.groupEditor;

import java.awt.geom.Point2D;
import java.util.Optional;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.swing.utils.GeometryHelper;

/**
 * Tool for resizing element. We apply an affine transformation on the sign.
 * <ul>
 * <li> the transformation itself depends on the selected handles ;
 * <li> it has an invariant point ;
 * <li> it will transform : the sign scale, and the sign original position.
 * </ul>
 *
 * @author rosmord
 */
public class ResizeTool implements GroupEditorListener {

    private final GroupEditor editor;

    /**
     * The center for the transform.
     */
    private Point2D centerPoint;

    /**
     * The original position of the handle.
     */
    private Point2D originalHandlePosition;

    /**
     * The original glyph position...
     */
    private Point2D originalGlyphPosition;

    private HandleHorizontalPosition horizontalHandle;

    private HandleVerticalPosition verticalHandle;

    private boolean handleSelected;

    /**
     * Original scale of the sign.
     */
    private int originalScale;

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
        if (e.getElementIndex() != -1 && e.isOnHandle()) {
            horizontalHandle = e.getHorizontalHandlePosition();
            verticalHandle = e.getVerticalHandlePosition();
            originalHandlePosition = new Point2D.Double(e.getPoint().getX(), e.getPoint()
                    .getY());
            centerPoint = computeCenter();
            originalGlyphPosition = getGlyphPosition(editor.getSelected());
            originalScale = editor.getSelectedSign().getRelativeSize();
        }
    }

    @Override
    public void mouseReleased(GroupEditorEvent e) {
        centerPoint = null;
        handleSelected = false;
    }

    @Override
    public void mouseDragged(GroupEditorEvent e) {
        if (editor.getSelected() != -1 && centerPoint != null) {
            if (handleSelected) {
                computePositionAndScale(e.getPoint())
                        .ifPresent(newPosition -> editor.setPositionAndSize(newPosition));
            }
        }
    }

//    /**
//     * Change the size of the selected glyph.
//     * <p>
//     * Currently, the aspect ratio doesn't change.
//     *
//     * @param dx
//     * @param dy
//     * @param horizontalHandlePosition
//     * @param verticalHandlePosition
//     */
//    public void resizeTo(double dx, double dy, HandleHorizontalPosition horizontalHandlePosition,
//            HandleVerticalPosition verticalHandlePosition) {
//        // FOR LATER IMPLEMENTATION : If both an hside and vside are
//        // selected, the sign aspect ratio will be kept.
//        DrawingSpecification specs = groupEditorDrawingPreferences.getDrawingSpecifications();
//
//        if (selected != -1) {
//            double scale = 1;
//            double x, y;
//
//            MDCView v = getView().getSubView(selected);
//            Point2D orig = getViewPosition(v);
//
//            x = orig.getX();
//            y = orig.getY();
//
//            if (horizontalHandlePosition != HandleHorizontalPosition.MIDDLE) {
//                double newWidth;
//                if (horizontalHandlePosition == HandleHorizontalPosition.LEFT) {
//                    newWidth = v.getWidth() - dx;
//                    x = x + dx;
//                } else {
//                    newWidth = v.getWidth() + dx;
//                }
//                if (newWidth > 0) {
//                    scale = newWidth / v.getWidth();
//                }
//            } else {
//                double newHeight;
//                if (verticalHandlePosition == HandleVerticalPosition.TOP) {
//                    newHeight = v.getHeight() - dy;
//                    y = y + dy;
//                } else {
//                    newHeight = v.getHeight() + dy;
//                }
//                if (newHeight > 0) {
//                    scale = newHeight / v.getHeight();
//                }
//            }
//
//            Hieroglyph h = group.getHieroglyphAt(selected);
//            // Convert to integers :
//            // double unitSize = 1000.0 / drawingSpecifications.getBaseLength();
//            x = x
//                    / specs.getHieroglyphsDrawer()
//                            .getGroupUnitLength();
//            y = y
//                    / specs.getHieroglyphsDrawer()
//                            .getGroupUnitLength();
//
//            scale = scale * h.getRelativeSize();
//            if (scale < 5) {
//                scale = 5;
//            }
//
//        }
//    }
    /**
     * Compute the center of the current transform.
     *
     * @return
     */
    private Point2D computeCenter() {
        // As a first version, scale around the center in all cases...
        MDCView v = editor.getSelectedView();
        Point2D viewPosition = editor.getViewPosition(v);
        double cx = viewPosition.getX() + v.getWidth() / 2.0;
        double cy = viewPosition.getY() + v.getHeight() / 2.0;
        return new Point2D.Double(cx, cy);
    }

    private Optional<PositionAndScale> computePositionAndScale(Point2D point) {
        // First, compute scale...
        double ratio = GeometryHelper.lengthRatio(centerPoint, point, originalHandlePosition);
        if (Double.isFinite(ratio) && ratio > 0.0) {
            int scale = (int) (ratio * 100.0);
            // Now, position...
            Point2D newPos = GeometryHelper.homotheticImage(centerPoint, ratio, originalGlyphPosition);
            // Transform this position in picture space into a position in group space...
            double unitLength = editor.getGroupEditorDrawingPreferences().getDrawingSpecifications().getHieroglyphsDrawer()
                    .getGroupUnitLength();
            return Optional.of(
                    new PositionAndScale((int) (newPos.getX() / unitLength), (int) (newPos.getY() / unitLength), scale)
            );
        } else {
            return Optional.empty();
        }
    }

    private Point2D getGlyphPosition(int selected) {
        return GeometryHelper.clonePoint(editor.getSelectedView().getPosition());        
    }

}
