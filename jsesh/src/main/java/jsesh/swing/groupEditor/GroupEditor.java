/*
 * Created on 28 nov. 2004
 *
 * This file is distributed under the GNU Lesser Public Licence.
 * 
 * (c) Serge Rosmorduc.
 */
package jsesh.swing.groupEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Editor for groups and ligatures.
 * <p>
 * when a sign is selected, a red frame is drawn around it. moving this frame
 * will move the sign. The handles in the frame corner, and around the frame,
 * allow resizing. shift and mouse movement allow rotation.
 * <p>
 * The cursor will change accordingly.
 * <p>
 * Ergonomics of the handles :
 * <ul>
 * <li> If we keep the aspect ratio of the glyph (which is currently the case),
 * the handle can't be placed at mouse position. Some extra visual feedback is
 * probably needed.
 * <li> The same goes for rotation.
 * </ul>
 *
 * @author rosmord
 */
public final class GroupEditor extends JPanel {

    /**
     * Adapter from plain mouse events to meaningful ones.
     *
     * @author rosmord
     */
    class LowLevelControl implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            currentTool.mouseClicked(buildEvent(e));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            currentTool.mouseDragged(buildEvent(e));
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            currentTool.mouseEntered(buildEvent(e));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            currentTool.mouseExited(buildEvent(e));
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            currentTool.mouseMoved(buildEvent(e));
        }

        @Override
        public void mousePressed(MouseEvent e) {
            currentTool.mousePressed(buildEvent(e));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            currentTool.mouseReleased(buildEvent(e));
        }
    }


    private GroupEditorDrawingPreferences groupEditorDrawingPreferences = new GroupEditorDrawingPreferences();
    private GroupEditorModel model;
    
    public GroupEditor() {        
        model = new GroupEditorModel();
        model.setListener(() -> {revalidate(); repaint();}); // Modify if needed.
        setBackground(Color.WHITE);
        LowLevelControl control = new LowLevelControl();
        addMouseListener(control);
        addMouseMotionListener(control);
        // TODO : fix the system so that it handle right-to-left signs.
        // Currently, we simply force left-to-right order
        DrawingSpecification specs = groupEditorDrawingPreferences.getDrawingSpecifications().copy();
        specs.setTextDirection(TextDirection.LEFT_TO_RIGHT);
        groupEditorDrawingPreferences.setDrawingSpecifications(specs);
    }

    /**
     * Translates a mouse event into a significant editedGroup editor event.
     *
     * @param e
     * @return the corresponding event.
     */
    private GroupEditorEvent buildEvent(MouseEvent e) {
        GroupEditorEvent result = null;
        Point2D p = getModelPoint(e.getPoint());
        MDCView v = getView();

        // First, test handles for the selected shape.
        if (selected != -1) {
            MDCView subv = v.getSubView(selected);
            // Test the selected sign handles.
            GroupEditorHandle handles[] = groupEditorMode.getHandles(this, subv);
            for (int i = 0; result == null && i < handles.length; i++) {
                if (handles[i].getShape().contains(p)) {
                    result = new GroupEditorEvent(editedGroup, p, selected,
                            handles[i].getHpos(), handles[i].getVpos());
                }
            }
        }

        // Test all shapes.
        // First, we try an exact system, based on actual shapes.
        // However, the click must fall on a "black" line.
        for (int i = 0; (result == null) && i < v.getNumberOfSubviews(); i++) {
            // Test the view itself.
            MDCView subv = v.getSubView(i);
            Hieroglyph h = editedGroup.getHieroglyphAt(i);

            Area s1 = getGlyphArea(subv, h);
            if (s1.contains(p)) {
                result = new GroupEditorEvent(editedGroup, p, i);
            }
        }
        if (result == null) {
            result = new GroupEditorEvent(editedGroup, p, -1);
        }
        return result;
    }

    /**
     * Should probably move somewhere else.
     *
     * @param subv
     * @param h
     * @return
     */
    private Area getGlyphArea(MDCView subv, Hieroglyph h) {
        DrawingSpecification specs = groupEditorDrawingPreferences.getDrawingSpecifications();
        Area area;
        if (h.getType() == SymbolCodes.SMALLTEXT) {
            Dimension2D dims = specs.getSuperScriptDimensions(h.getSmallText());
            double height = h.getRelativeSize() / 100.0 * dims.getHeight();
            double width = h.getRelativeSize() / 100.0 * dims.getWidth();
            area = new Area(new Rectangle2D.Double(subv.getPosition().x, subv.getPosition().y, width, height));
        } else {
            area = specs.getHieroglyphsDrawer().getSignArea(
                    h.getCode(), subv.getPosition().x, subv.getPosition().y,
                    h.getRelativeSize() / 100.0, h.getRelativeSize() / 100.0,
                    h.getAngle(), h.isReversed());
        }
        return area;
    }

    public int getHandleSize() {
        return groupEditorDrawingPreferences.getHandleSize();
    }

    public int getLineWidth() {
        return groupEditorDrawingPreferences.getLineWidth();
    }

    /**
     * Return a point in model coordinates
     *
     * @param p : a point in screen coordinates.
     * @return a point in model coordinates
     */
    private Point2D getModelPoint(Point p) {
        double x = (p.getX() / groupEditorDrawingPreferences.getScale() - groupEditorDrawingPreferences.getSideMargin());
        double y = (p.getY() / groupEditorDrawingPreferences.getScale() - groupEditorDrawingPreferences.getTopMargin());
        return new Point2D.Double(x, y);
    }

    @Override
    public Dimension getPreferredSize() {
        if (model.isEmpty()) {
            return new Dimension(640, 480);
        } else {
            MDCView v = getView();
            int w = (int) ((v.getWidth() + 2 * groupEditorDrawingPreferences.getSideMargin())
                    * groupEditorDrawingPreferences.getScale());
            int h = (int) ((v.getHeight() + 2 * groupEditorDrawingPreferences.getTopMargin())
                    * groupEditorDrawingPreferences.getScale());
            return new Dimension(w, h);
        }
    }

    public double getScale() {
        return groupEditorDrawingPreferences.getScale();
    }

    /**
     * Return the index of the selected element, or -1 if none.
     *
     * @return the index of the selected element, or -1 if none.
     */
    public int getSelected() {
        return selected;
    }

    public double getSideMargin() {
        return groupEditorDrawingPreferences.getSideMargin();
    }

    public double getTopMargin() {
        return groupEditorDrawingPreferences.getTopMargin();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // The view is rebuilt for each redraw, as it's very simple.
        MDCView view = model.getView();
        // Sets the graphics environment
        Graphics2D g2d = (Graphics2D) g.create();
        // We want antialiasing !!!
        jsesh.swing.utils.GraphicsUtils.antialias(g2d);

        g2d.scale(getScale(), getScale());
        g2d.translate(getSideMargin(), getTopMargin());
        double wd = (getLineWidth() / g2d.getTransform().getScaleX());

        if (! model.isEmpty()) {
            // draw the signs
            ViewDrawer drawer = new ViewDrawer();
            drawer.draw(g2d, view, groupEditorDrawingPreferences.getDrawingSpecifications());
            
            // Draw the frame around the selected sign.
            if (selected >= 0) {
                // The view for this sign :
                MDCView v = view.getSubView(selected);
                // For absolute groups, the view placement is ... absolute
                // (This could be a bug source, if we changed the layout for
                // this element)
                Point2D orig = getViewPosition(v);
                double w = v.getWidth();
                double h = v.getHeight();

                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke((float) wd));
                // Draw the frame
                g2d
                        .draw(new Rectangle2D.Double(orig.getX(), orig.getY(),
                                w, h));
                // Draw the handles
                GroupEditorHandle handles[] = groupEditorMode.getHandles(this, v);
                for (int i = 0; i < handles.length; i++) {
                    g2d.draw(handles[i].getShape());
                }
            }
            model.drawControls(g);
        }
        g2d.setColor(Color.RED);
        // Sets the actual width for drawing :
        float f[] = {0.5f, 1};
        g2d.setStroke(new BasicStroke((float) (wd / 2), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_MITER, 2, f, 0));

        g2d.draw(new Line2D.Double(0, 0, 1000, 0));
        g2d.draw(new Line2D.Double(0, 0, 0, 1000));
        g2d.dispose();
    }

    /**
     * @param editedGroup The editedGroup to set.
     */
    public void setEditedGroup(AbsoluteGroup editedGroup) {
        this.editedGroup = editedGroup;
        selected = -1;
        revalidate();
    }

    public void setHandleSize(int handleSize) {
        groupEditorDrawingPreferences.setHandleSize(handleSize);
    }

    public void setLineWidth(int lineWidth) {
        groupEditorDrawingPreferences.setLineWidth(lineWidth);
    }

    public void setScale(double scale) {
        groupEditorDrawingPreferences.setScale(scale);
        repaint();
        revalidate();
    }


  
    /**
     * moves the selected sign.
     *
     * @param dx
     * @param dy
     */
    public void move(double dx, double dy) {
        if (selected != -1) {
            DrawingSpecification specs = groupEditorDrawingPreferences.getDrawingSpecifications();
            MDCView v = getView().getSubView(selected);
            Point2D p = getViewPosition(v);
            double x = p.getX() + dx;
            double y = p.getY() + dy;
            // Convert to integers :
            double unitSize = specs.getHieroglyphsDrawer()
                    .getGroupUnitLength();
            x = x / unitSize;
            y = y / unitSize;
            Hieroglyph h = editedGroup.getHieroglyphAt(selected);
            h.setExplicitPosition((int) x, (int) y, h.getRelativeSize());
            revalidate();
            repaint();
        }
    }

    /**
     * Change the size of the selected glyph.
     * <p>
     * Currently, the aspect ratio doesn't change.
     *
     * @param dx
     * @param dy
     * @param horizontalHandlePosition
     * @param verticalHandlePosition
     */
    public void resizeTo(double dx, double dy, HandleHorizontalPosition horizontalHandlePosition,
            HandleVerticalPosition verticalHandlePosition) {
        // FOR LATER IMPLEMENTATION : If both an hside and vside are
        // selected, the sign aspect ratio will be kept.
        DrawingSpecification specs = groupEditorDrawingPreferences.getDrawingSpecifications();

        if (selected != -1) {
            double scale = 1;
            double x, y;

            MDCView v = getView().getSubView(selected);
            Point2D orig = getViewPosition(v);

            x = orig.getX();
            y = orig.getY();

            if (horizontalHandlePosition != HandleHorizontalPosition.MIDDLE) {
                double newWidth;
                if (horizontalHandlePosition == HandleHorizontalPosition.LEFT) {
                    newWidth = v.getWidth() - dx;
                    x = x + dx;
                } else {
                    newWidth = v.getWidth() + dx;
                }
                if (newWidth > 0) {
                    scale = newWidth / v.getWidth();
                }
            } else {
                double newHeight;
                if (verticalHandlePosition == HandleVerticalPosition.TOP) {
                    newHeight = v.getHeight() - dy;
                    y = y + dy;
                } else {
                    newHeight = v.getHeight() + dy;
                }
                if (newHeight > 0) {
                    scale = newHeight / v.getHeight();
                }
            }

            Hieroglyph h = editedGroup.getHieroglyphAt(selected);
            // Convert to integers :
            // double unitSize = 1000.0 / drawingSpecifications.getBaseLength();
            x = x
                    / specs.getHieroglyphsDrawer()
                            .getGroupUnitLength();
            y = y
                    / specs.getHieroglyphsDrawer()
                            .getGroupUnitLength();

            scale = scale * h.getRelativeSize();
            if (scale < 5) {
                scale = 5;
            }
            h.setExplicitPosition((int) x, (int) y, (int) scale);
            repaint();
            invalidate();
        }

    }

    /**
     * Not too reliable system for sub view positions.
     *
     * @param subview
     * @return the position of subview.
     */
    Point2D getViewPosition(MDCView subview) {
        return new Point2D.Double(subview.getPosition().x, subview
                .getPosition().y);
    }

    public GroupEditorDrawingPreferences getGroupEditorDrawingPreferences() {
        return groupEditorDrawingPreferences;
    }

    /**
     * Rotate the selected sign around its center c.
     * <p>
     * the angle is given by the two vectors c-p1 and c-p2.
     *
     * @param p1
     * @param p2
     */
    public void rotate(Point2D p1, Point2D p2) {
        if (selected != -1) {
            MDCView v = getView().getSubView(selected);
            Point2D orig = getViewPosition(v);
            Point2D center = new Point2D.Double(orig.getX() + v.getWidth() / 2,
                    orig.getY() + v.getHeight() / 2);
            // Compute the vectors.
            Point2D v1 = new Point2D.Double(p1.getX() - center.getX(), p1
                    .getY()
                    - center.getY());
            Point2D v2 = new Point2D.Double(p2.getX() - center.getX(), p2
                    .getY()
                    - center.getY());
            double d1 = v1.distance(0, 0);
            double d2 = v2.distance(0, 0);
            if (d1 == 0 || d2 == 0) {
                return;
            }
            double cos = (v1.getX() * v2.getX() + v1.getY() * v2.getY())
                    / (d1 * d2);
            double sin = (v1.getX() * v2.getY() - v1.getY() * v2.getX())
                    / (d1 * d2);

            double alpha = Math.acos(cos);

            if (sin < 0) {
                alpha = 2 * Math.PI - alpha;
            }
            Hieroglyph h = editedGroup.getHieroglyphAt(selected);
            double angle = (alpha * 180.0 / Math.PI) + h.getAngle();
            h.setAngle((int) angle);
            repaint();
            revalidate();
        }
    }

    public void rotate(int angle) {
        if (selected != -1) {
            Hieroglyph h = editedGroup.getHieroglyphAt(selected);
            h.setAngle((int) angle);
            repaint();
            revalidate();
        }
    }

    public void resetSign() {
        if (selected != -1) {
            Hieroglyph h = editedGroup.getHieroglyphAt(selected);
            h.setExplicitPosition(0, 0, 100);
            repaint();
            revalidate();
        }
    }

    /**
     * select the next glyph in editedGroup.
     */
    public void next() {
        if (editedGroup != null) {
            selected = (selected + 2) % (editedGroup.getNumberOfChildren() + 1) - 1;
            repaint();
        }
    }

    /**
     * Select previous glyph in editedGroup.
     */
    public void previous() {
        if (editedGroup != null) {
            selected = selected - 1;
            if (selected == -2) {
                selected = editedGroup.getNumberOfChildren() - 1;
            }
            repaint();
        }
    }

}
