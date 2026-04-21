/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.editor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import jsesh.defaults.JseshFontKit;
import jsesh.drawingspecifications.JSeshStyle;
import jsesh.swing.utils.GraphicsUtils;

/**
 * A field like editor for Manuel de Codage texts. Displays only one line of
 * text.
 *
 * @author rosmord
 */
@SuppressWarnings("serial")
public class JMDCField extends JMDCEditor {

    private Dimension preferedSize;
    // Drawing margin to move.
    private float drawingHorizontalOrigin = 0;

    private static final int DEFAULT_MARGIN = 5;
    // Should probably change.
    private int margin = DEFAULT_MARGIN;

    /**
     * Name of the action called to validate input (when enter is typed)
     */
    private static final String VALIDATE_INPUT = "VALIDATE_INPUT";

    /**
     * List of action listeners.
     */
    private final ArrayList<ActionListener> actionListeners = new ArrayList<>();

    /**
     * Build a JMDCField, passing all relevant parameters.
     * <p>
     * The width of the field can be chosen, but the height is determined by the
     * style.
     * the style is shared, and can be used by other components.
     * 
     * @param styleReference            a style reference which can be shared
     * @param hieroglyphShapeRepository source for hieroglyph shapes.
     * @param possibilityRepository     source for automated completion.
     */
    public JMDCField(int width, JSeshStyleReference styleReference,
            JseshFontKit fontKit) {
        super(new HieroglyphicTextModel(), styleReference, fontKit);
        // Build an input map using the default MDCEditor inputmap as parent.
        InputMap inputMap = new InputMap();
        inputMap.setParent(getInputMap()); // Normally, the MDCEditor inputMap is already set.
        ActionMap actionMap = new ActionMap();
        actionMap.setParent(getActionMap()); // idem.
        actionMap.put(VALIDATE_INPUT, new ValidateInputAction());
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), VALIDATE_INPUT);
        setActionMap(actionMap);
        setInputMap(WHEN_FOCUSED, inputMap);
    }

    /**
     * Build a JMDCField with a specific style.
     * <p>
     * The width and the height of the field can be chosen, and the actual style
     * will be modified to fit the height.
     * 
     * @param width                     the width of the field, in pixels.
     * @param height                    the height of the field, in pixels.
     * @param style                     a specific style, not shared with other
     *                                  components.
     * @param hieroglyphShapeRepository source for hieroglyph shapes.
     * @param possibilityRepository     source for automated completion.
     */
    public JMDCField(int width, int height, JSeshStyle style, JseshFontKit fontKit) {
        this(width, new JSeshStyleReference(adaptStyleToHeight(style, height, DEFAULT_MARGIN)),
                fontKit);
    }

    /**
     * Create a hieroglyphic field with the given dimensions, in pixels, using
     * reasonnable defaults.
     * 
     * @param width
     * @param height
     */
    public JMDCField(int width, int height) {
        this(width, height, JSeshStyle.DEFAULT, JseshFontKit.embeddedOnlyFontKit());
    }

    /**
     * Compute the actual style modified to fit the height of the field.
     * 
     * @param style  the original style.
     * @param height the target total height of the field, in pixels (including
     *               margins).
     * @return the modified style.
     */
    private final static JSeshStyle adaptStyleToHeight(JSeshStyle style, int height, int margin) {
        int textHeight = height - 2 * margin;
        return style.copy().geometry(g -> g.maxCadratHeight(textHeight)
                .standardSignHeight(textHeight)
                .maxCadratWidth(textHeight * 1.1f)
                .topMargin(margin)
                .leftMargin(0)
                .lineSkip(0)).build();
        // Why was setScale(1.0); called here ?
    }

    public JMDCField() {
        this(320, 50);
    }

    @Override
    public Dimension getPreferredSize() {
        return preferedSize;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return new Dimension(d.width, preferedSize.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.drawBaseComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GraphicsUtils.antialias(g2d);

        Rectangle r = getCursorRectangle();

        double lastX = drawingHorizontalOrigin + getWidth();

        if (r.getMinX() < drawingHorizontalOrigin) {
            drawingHorizontalOrigin = (float) (r.getMinX() - 2f);
        } else if (r.getMaxX() > lastX) {
            drawingHorizontalOrigin = (float) (r.getMaxX() - getWidth());
        }
        g2d.translate(-drawingHorizontalOrigin, 0);
        g2d.scale(getScale(), getScale());
        drawer.setClip(true);
        drawer.drawViewAndCursor(g2d, 
            getRenderContext(),
            buildTechRenderContext(),
            getView(), getMDCCaret());
    }

    public void addActionListener(ActionListener l) {
        actionListeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        actionListeners.remove(l);
    }

    private class ValidateInputAction extends AbstractAction {

        private static final long serialVersionUID = -37706887980500015L;
        private int actionId = 1;

        @Override
        public void actionPerformed(ActionEvent e) {
            actionId++;
            for (int i = 0; i < actionListeners.size(); i++) {
                ActionListener a = actionListeners.get(i);
                a.actionPerformed(new ActionEvent(JMDCField.this, actionId, VALIDATE_INPUT));
            }
        }

    }


}
