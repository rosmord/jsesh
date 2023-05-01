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

import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

import jsesh.mdcDisplayer.preferences.PageLayout;
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
    
    // Should probably change.
    private int margin = 5;

    /**
     * Name of the action called to validate input (when enter is typed)
     */
    private static final String VALIDATE_INPUT = "VALIDATE_INPUT";

    /**
     * List of action listeners.
     */
    private final ArrayList<ActionListener> actionListeners = new ArrayList<>();

    /**
     * Create a hieroglyphic field with the given dimensions, in pixels.
     *
     * @param width
     * @param height
     */
    public JMDCField(int width, int height) {
        setCached(false);
        preferedSize = new Dimension(width, height);
        DrawingSpecification specs = getDrawingSpecifications().copy();
        int textHeight = height - 2 * margin;       
        
        specs.setMaxCadratHeight(textHeight);
        // Perhaps not the best system...
        specs.setStandardSignHeight(textHeight);
        specs.setMaxCadratWidth(textHeight*1.1f);
        
        PageLayout pageLayout = specs.getPageLayout();
        pageLayout.setTopMargin(margin);
        pageLayout.setLeftMargin(0);

        specs.setLineSkip(0);
        setScale(1.0);
        
        setDrawingSpecifications(specs); 
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

        Rectangle r = getPointerRectangle();

        double lastX = drawingHorizontalOrigin + getWidth();

        if (r.getMinX() < drawingHorizontalOrigin) {
            drawingHorizontalOrigin = (float) (r.getMinX() - 2f);
        } else if (r.getMaxX() > lastX) {
            drawingHorizontalOrigin = (float) (r.getMaxX() - getWidth());
        }
        g2d.translate(-drawingHorizontalOrigin, 0);
        g2d.scale(getScale(), getScale());
        drawer.setClip(true);
        drawer.drawViewAndCursor(g2d, getView(), getMDCCaret(), getDrawingSpecifications());
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

	public void setFontManager(HieroglyphicFontManager fontManager) {
		// TODO Auto-generated method stub
		throw new RuntimeException("WRITE ME!!!!");
	}

}
