/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
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
		/*
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
		*/
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