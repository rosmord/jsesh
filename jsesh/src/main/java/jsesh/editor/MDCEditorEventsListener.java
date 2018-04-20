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
/*
 * Created on 1 nov. 2004
 */
package jsesh.editor;

import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * Manages events that occur in a JMDCEditor. Keyboard events are managed by a
 * MDCEditorKeyManager. Was originally an inner class; moved to its own file to
 * increase readability.
 * 
 * @author S. Rosmorduc
 *  
 */

final class MDCEditorEventsListener extends MouseInputAdapter implements
		FocusListener {
	private final JMDCEditor editor;

	private boolean dragging;

	/**
	 * @param editor
	 */
	MDCEditorEventsListener(JMDCEditor editor) {
		this.editor = editor;
		dragging = false;
		editor.addMouseListener(this);
		editor.addMouseMotionListener(this);
		editor.addFocusListener(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 3 && e.getButton() == MouseEvent.BUTTON1) {
			this.editor.moveCursorToMouse(e.getPoint());
			this.getWorkflow().selectCurrentLine();
			editor.requestFocusInWindow();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.MouseInputAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (!dragging) {
			editor.getWorkflow().setMarkToCursor();
		}
		Point p = (Point) e.getPoint();
		this.editor.moveCursorToMouse(p);
		editor.requestFocusInWindow();
		dragging = true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.MouseInputAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		int action = 0;
		if (dragging) {
			dragging = false;
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			action = 1;
			if ((e.isShiftDown()))
				action = 2;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			action = 2;
		}
		if (action == 1) {
			Point p = (Point) e.getPoint();
			this.editor.moveCursorToMouse(p);
			getWorkflow().clearMark();
			editor.requestFocusInWindow();
		} else if (action == 2) {
			Point p = (Point) e.getPoint();
			this.editor.moveMarkToMouse(p);
			editor.requestFocusInWindow();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.MouseInputAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		//dragging = false;
	}

	public void focusGained(FocusEvent e) {
		getWorkflow().focusGained();
	}

	public void focusLost(FocusEvent e) {
		getWorkflow().focusLost();
	}

	private JMDCEditorWorkflow getWorkflow() {
		return editor.workflow;
	}

}