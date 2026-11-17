/*
 * Created on 30 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 * 
 * NOTE I have noticed a bad behaviour for the focus with 
 * linux, using fvwm as window manager (and focus followmouse), and jdk1.5.0
 * The problem doesn't appear with jdk1.4, nor with gnome and its default WM, 
 * even in focus followmouse mode. So I don't know if this is java 1.5 or 
 * fvwm's fault, I suppose it will eventually be solved.  
 * See jsesh.misc.tests.TestFocus for a simple example. 
 **/

package jsesh.mdcDisplayer.swing.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.draw.SimpleViewBuilder;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.utils.GraphicsUtils;

/**
 * An editor for Manuel de codage text.
 * 
 * If you want to manipulate the text, you may do it through the workflow
 * object.
 * 
 * @author rosmord
 */

public class JMDCEditor extends JPanel implements MDCModelEditionListener {

	/**
	 * Strategy to build a view.
	 *  
	 */
	SimpleViewBuilder builder;

	/**
	 * Debugging of view placement.
	 */
	private boolean debug = false;

	/**
	 * Strategy to draw a view.
	 *  
	 */
	protected ViewDrawer drawer;

	/**
	 * The current view we maintain.
	 * 
	 *  
	 */
	MDCView mdcView;

	/**
	 * Display scale for this window.
	 */
	private double scale;

	/**
	 * Updates the view.
	 *  
	 */
	private MDCViewUpdater viewUpdater;

	/**
	 * Deals with events that occur on this object :
	 */

	MDCEditorEventsListener eventListener;

	/**
	 * Basic Informations about drawing : fonts to use, line width, etc...
	 *  
	 */
	JMDCEditorWorkflow workflow;

	/**
	 * States that the caret has changed since last redraw.
	 * <p>
	 * The purporse of this variable is to ensure that scrolls to the caret
	 * position are only done <em>after</em> view updates.
	 */
	private boolean caretChanged = true;

	public JMDCEditor() {
		this(new HieroglyphicTextModel());
	}

	public JMDCEditor(HieroglyphicTextModel data) {
		setBackground(Color.WHITE);
		drawer = new ViewDrawer();
		drawer.setCached(true);
		setScale(2.0);
		//simpleDrawingSpecification= new SimpleDrawingSpecifications();
		builder = new SimpleViewBuilder();
		mdcView = null;
		workflow = new JMDCEditorWorkflow(data);
		workflow.addMDCModelListener(this);
		//setRequestFocusEnabled(true);
		setFocusable(true);
		viewUpdater = new MDCViewUpdater(this);

		eventListener = new MDCEditorEventsListener(this);

		MDCEditorKeyManager.getInstance().control(this);
	}

	public void addCodeChangeListener(MDCModelEditionListener l) {
		workflow.addMDCModelListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
	 */
	public void caretChanged(MDCCaret caret) {
		caretChanged = true;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#codeChanged(java.lang.StringBuffer)
	 */
	public void codeChanged(StringBuffer code) {
		// NO-OP.
	}

	public void deleteCodeChangeListener(MDCModelEditionListener l) {
		workflow.deleteCodeChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#focusGained(java.lang.StringBuffer)
	 */
	public void focusGained(StringBuffer code) {
		// NO-OP.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#focusLost(
	 */
	public void focusLost() {
		// NO-OP.
	}

	/**
	 * Returns the code buffer associated with this editor.
	 * 
	 * @return the code buffer associated with this editor.
	 */
	public String getCodeBuffer() {
		return workflow.getCurrentCode().toString();
	}

	/**
	 * @return
	 *  
	 */
	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return workflow.getHieroglyphicTextModel();
	}

	/**
	 * returns the cursor associated with this object, or null if none. The
	 * simple displayer has no cursor. Only its editor subclass has.
	 */
	protected MDCCaret getMDCCaret() {
		return workflow.getCaret();
	}

	public Dimension getPreferredSize() {
		if (getHieroglyphicTextModel() == null)
			return new Dimension(600, 600);
		else {
			MDCView v = getView();
			if (v.getWidth() == 0 || v.getHeight() == 0) {
				v.setWidth(14);
				v.setHeight(14);
			}
			return new Dimension((int) (scale * v.getWidth()), (int) (scale * v
					.getHeight()));
		}
	}

	/**
	 * @return
	 *  
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Returns a MDCView of the current hieroglyphicTextModel.
	 * 
	 * @return
	 */

	public MDCView getView() {
		if (mdcView == null) {
			mdcView = builder.buildView(getHieroglyphicTextModel().getModel());
			revalidate();
			if (debug)
				System.out.println(mdcView);
		}
		return mdcView;
	}

	/**
	 * Return the workflow, which allows manipulation of the underlaying
	 * hieroglyphicTextModel.
	 * 
	 * @return Returns the workflow.
	 */
	public JMDCEditorWorkflow getWorkflow() {
		return workflow;
	}

	/**
	 * @param e
	 */
	protected void moveCursorToMouse(Point p) {

		Point clickPoint = (Point) p.clone();
		// the display scale is none of the drawer's business (currently, that
		// is.)
		clickPoint.x = (int) (clickPoint.x / getScale());
		clickPoint.y = (int) (clickPoint.y / getScale());
		//drawer.getPositionForPoint(getView(), clickPoint);
		MDCPosition pos= drawer.getPositionForPoint(getView(), clickPoint,
				builder.getDrawingSpecifications());
		if (pos != null)
			workflow.setCursor(pos);
	}

	/**
	 * @param e
	 */
	protected void moveMarkToMouse(Point p) {
		Point clickPoint = (Point) p.clone();
		// the display scale is none of the drawer's business (currently, that
		// is.)
		clickPoint.x = (int) (clickPoint.x / getScale());
		clickPoint.y = (int) (clickPoint.y / getScale());
		//drawer.getPositionForPoint(getView(), clickPoint);
		MDCPosition pos= drawer.getPositionForPoint(getView(), clickPoint,
				builder.getDrawingSpecifications());
		workflow.setMark(pos);
	}

	public void paintComponent(Graphics g) {
		//boolean canDraw= true;
		
		//if (canDraw) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			GraphicsUtils.antialias(g2d);
			g2d.scale(scale, scale);

			drawer.setClip(true);
			drawer.DrawViewAndCursor(g2d, getView(), getMDCCaret(), builder
					.getDrawingSpecifications());
		//}
		if (caretChanged) {
			// Disarm caret change updates.
			caretChanged = false;
			// Show the cursor.
			Rectangle r= getPointerRectangle();
			if (! g.getClipBounds().contains(r)) {
				//canDraw= false;
				scrollRectToVisible(r);
				repaint();
			}
		} 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
	public void print(Graphics g) {
		drawer.setClip(false);
		drawer.draw((Graphics2D) g, getView(), builder
				.getDrawingSpecifications());
	}

	/**
	 * @param b
	 *  
	 */
	public void setDebug(boolean b) {
		debug = b;
	}

	/**
	 * @param d
	 *  
	 */
	public void setScale(double d) {
		scale = d;
		if (drawer.isCached())
			drawer.flushCache();
		repaint();
		revalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#textChanged()
	 */
	public void textChanged() {
		mdcView = null;
		//repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#textEdited(jsesh.mdc.model.operations.ModelOperation)
	 */
	public void textEdited(ModelOperation op) {
		op.accept(viewUpdater);
		// TODO : only call revalidate if the dimensions have changed.
		revalidate();
		repaint();
	}

	/**
	 * @return the current insertion position.
	 */
	public int getInsertPositiont() {
		return workflow.getCaret().getInsert().getIndex();
	}

	/**
	 * Returns a rectangle describing the current cursor position.
	 * 
	 * @return
	 */
	Rectangle getPointerRectangle() {
		Rectangle2D r1 = drawer.getRectangleAroundPosition(getView(), workflow
				.getCaret().getInsert().getPosition(), builder
				.getDrawingSpecifications());
		int w = (int) (r1.getWidth() * getScale());
		int h = (int) (r1.getHeight() * getScale());
		if (w < 2)
			w = 2;
		if (h < 2)
			h = 2;
		Rectangle r = new Rectangle((int) (r1.getX() * getScale()), (int) (r1
				.getY() * getScale()), w, h);
		return r;
	}

}