/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
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

import jsesh.mdcDisplayer.preferences.PageLayout;
import jsesh.swing.utils.GraphicsUtils;


/**
 * A field like editor for Manuel de Codage texts.
 * Displays only one line of text.
 * 
 * @author rosmord
 *
 */
@SuppressWarnings("serial")
public class JMDCField extends JMDCEditor {
	
	private Dimension preferedSize;
	// Drawing margin to move.
	private float drawingHorizontalOrigin=0;
	
	/**
	 * Name of the action called to validate input (when enter is typed)
	 */
	private static final String VALIDATE_INPUT= "VALIDATE_INPUT";

	/**
	 * List of action listeners.
	 */
	private final ArrayList<ActionListener> actionListeners= new ArrayList<ActionListener>();
	
	/**
	 * Create a hieroglyphic field with the given dimensions, in pixels.
	 * @param width
	 * @param height
	 */
	public JMDCField(int width, int height) {
		setCached(false);
		preferedSize= new Dimension(width,height);
		setDrawingSpecifications(getDrawingSpecifications().copy());
		int textHeight= (int)(height * 0.8);
		//textHeight= height;
		// Compute the space left for margin
		int margin= height - textHeight;
		// Ensure it's even :
		if (margin % 2 != 0) {
			margin++;
			textHeight --;
		}
		setScale(textHeight/getDrawingSpecifications().getMaxCadratHeight());
		
		
		PageLayout pageLayout= getDrawingSpecifications().getPageLayout();
		pageLayout.setTopMargin((int)(margin/2.0/getScale()));
		pageLayout.setLeftMargin(0);
		
		getDrawingSpecifications().setLineSkip(0);
		// Build an input map using the default MDCEditor inputmap as parent.
		InputMap inputMap= new InputMap();
		inputMap.setParent(getInputMap()); // Normally, the MDCEditor inputMap is already set.
		ActionMap actionMap= new ActionMap();
		actionMap.setParent(getActionMap()); // idem.
		actionMap.put(VALIDATE_INPUT, new ValidateInputAction());
		inputMap.put(KeyStroke.getKeyStroke("ENTER"), VALIDATE_INPUT);
		setActionMap(actionMap);
		setInputMap(WHEN_FOCUSED, inputMap);
	}
	
	public JMDCField() {
		this(320,40);
	}
	
	public Dimension getPreferredSize() {
		return preferedSize;
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		Dimension d= super.getMaximumSize();
		return new Dimension(d.width, preferedSize.height);
	}
	
	protected void paintComponent(Graphics g) {
		super.drawBaseComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		GraphicsUtils.antialias(g2d);

		Rectangle r = getPointerRectangle();
		
		double lastX= drawingHorizontalOrigin + getWidth()  ;
	
		if (r.getMinX() < drawingHorizontalOrigin ) {
			drawingHorizontalOrigin= (float) (r.getMinX() - 2f);
			//System.err.println("setting origin to "+ drawingHorizontalOrigin);
		} else
		if (r.getMaxX() > lastX) {
			drawingHorizontalOrigin= (float) (r.getMaxX() - getWidth());
			//System.err.println("setting origin to "+ drawingHorizontalOrigin);
		}
		g2d.translate(-drawingHorizontalOrigin,0);
		g2d.scale(getScale(),getScale());
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
		private int actionId= 1;
		
		public void actionPerformed(ActionEvent e) {
			actionId++;
			for (int i=0; i < actionListeners.size();i++) {
				ActionListener a= actionListeners.get(i);
				a.actionPerformed(new ActionEvent(JMDCField.this, actionId,VALIDATE_INPUT));
			}
		}

	}
	
}
