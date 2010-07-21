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
package jsesh.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import jsesh.editor.actions.EditorShadeAction;
import jsesh.editor.caret.MDCCaret;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.ListOfTopItems;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.PageLayout;
import jsesh.mdcDisplayer.swing.shadingMenuBuilder.ShadingMenuBuilder;

import org.qenherkhopeshef.graphics.utils.GraphicsUtils;

/**
 * An editor for Manuel de codage text. If you want to manipulate the text, you
 * may do it through the workflow object.
 * 
 * The names for the available actions are defined as constants in  {@link MDCEditorKeyManager}
 * @author rosmord
 */
public class JMDCEditor extends JPanel {

	/**
     *
     */
	private static final long serialVersionUID = -5312716856062578743L;
	private static final int BOTTOM_MARGIN = 5;
	private JMDCModelEditionListener mdcModelEditionListener;
	/**
	 * Strategy to build a view.
	 * 
	 */
	//SimpleViewBuilder builder;
	/**
	 * Debugging of view placement.
	 */
	private boolean debug = false;
	
	/**
	 * Strategy to draw a view.
	 * (we have just decided to build the view builder on demand,
	 * and not to keep it. But the drawer contains some information, 
	 * and in particular a cache of views). 
	 */
	protected ViewDrawer drawer;
	
	/**
	 * The current view we maintain.
	 */
	MDCView documentView;
	
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
	 * The object responsible for building transferable for the clipboard.
	 */
	MDCModelTransferableBroker mdcModelTransferableBroker = new SimpleMDCModelTransferableBroker();
	/**
	 * States that the caret has changed since last redraw.
	 * <p>
	 * The purpose of this variable is to ensure that scrolls to the caret
	 * position are only done <em>after</em> view updates.
	 */
	private boolean caretChanged = true;
	
	private boolean editable = true;
	
	// FIXME : choose a reasonable method to share drawing specifications.
	private DrawingSpecification drawingSpecifications = MDCEditorKit
			.getBasicMDCEditorKit().getDrawingSpecifications();
	
	private boolean drawLimits= false;

	public JMDCEditor() {
		this(new HieroglyphicTextModel());
	}

	public JMDCEditor(HieroglyphicTextModel data) {
		setBackground(Color.WHITE);
		drawer = new ViewDrawer();
		drawer.setCached(true);
		setScale(2.0);
		// simpleDrawingSpecification= new SimpleDrawingSpecifications();
		// The use of an interface for the view builder may have been 
		// some kind of over-engineering.
		//builder = new SimpleViewBuilder();
		documentView = null;
		workflow = new JMDCEditorWorkflow(data);

		mdcModelEditionListener = new JMDCModelEditionListener();
		workflow.addMDCModelListener(mdcModelEditionListener);
		// setRequestFocusEnabled(true);
		setFocusable(true);
		viewUpdater = new MDCViewUpdater(this);

		eventListener = new MDCEditorEventsListener(this);

		new MDCEditorKeyManager(this);
	}

	public void setHieroglyphiTextModel(
			HieroglyphicTextModel hieroglyphicTextModel) {
		workflow.setHieroglyphicTextModel(hieroglyphicTextModel);
		workflow.setCursor(hieroglyphicTextModel.buildFirstPosition());
		workflow
				.setCursor(new MDCPosition(hieroglyphicTextModel.getModel(), 0));
		invalidateView();
	}

	public void addCodeChangeListener(MDCModelEditionListener l) {
		workflow.addMDCModelListener(l);
	}

	public void deleteCodeChangeListener(MDCModelEditionListener l) {
		workflow.deleteCodeChangeListener(l);
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
	 * @return the model.
	 * 
	 */
	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return workflow.getHieroglyphicTextModel();
	}

	/**
	 * returns the cursor associated with this object, or null if none. The
	 * simple displayer has no cursor. Only its editor subclass has.
	 * 
	 * @return the caret.
	 */
	protected MDCCaret getMDCCaret() {
		return workflow.getCaret();
	}

	public Dimension getPreferredSize() {
		if (getHieroglyphicTextModel() == null) {
			return new Dimension(600, 600);
		} else {
			MDCView v = getView();
			if (v.getWidth() == 0 || v.getHeight() == 0) {
				v.setWidth(14);
				v.setHeight(14);
			}
			return new Dimension((int) (scale * v.getWidth()), BOTTOM_MARGIN
					+ (int) (scale * v.getHeight()));
		}
	}

	/**
	 * @return the scale
	 * 
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Returns a MDCView of the current hieroglyphicTextModel. Build it if
	 * necessary.
	 * 
	 * @return the view for the model.
	 */
	public MDCView getView() {
		if (documentView == null) {
			documentView = new SimpleViewBuilder().buildView(getHieroglyphicTextModel().getModel(),
					getDrawingSpecifications());
			revalidate();
			if (debug) {
				System.out.println(documentView);
			}
		}
		return documentView;
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
	 * Moves cursor to screen position p.
	 * 
	 * @param p
	 *            a point in screen position.
	 */
	protected void moveCursorToMouse(Point p) {

		Point clickPoint = (Point) p.clone();
		// the display scale is none of the drawer's business (currently, that
		// is.)
		clickPoint.x = (int) (clickPoint.x / getScale());
		clickPoint.y = (int) (clickPoint.y / getScale());
		// drawer.getPositionForPoint(getView(), clickPoint);
		MDCPosition pos = drawer.getPositionForPoint(getView(), clickPoint,
				getDrawingSpecifications());
		if (pos != null) {
			workflow.setCursor(pos);
		}
	}

	/**
	 * Moves mouse to screen position p.
	 * 
	 * @param p
	 */
	protected void moveMarkToMouse(Point p) {
		Point clickPoint = (Point) p.clone();
		// the display scale is none of the drawer's business (currently, that
		// is.)
		clickPoint.x = (int) (clickPoint.x / getScale());
		clickPoint.y = (int) (clickPoint.y / getScale());
		// drawer.getPositionForPoint(getView(), clickPoint);
		MDCPosition pos = drawer.getPositionForPoint(getView(), clickPoint,
				getDrawingSpecifications());
		workflow.setMark(pos);
	}

	/**
	 * Preliminary etap for drawing this component.
	 * 
	 * @param g
	 */
	protected void drawBaseComponent(Graphics g) {
		super.paintComponent(g);
		Insets insets = getInsets();
		g.clipRect(insets.left, insets.top, getWidth() - insets.left
				- insets.right, getHeight() - insets.top - insets.bottom);
	}

	protected void paintComponent(Graphics g) {
		drawBaseComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		GraphicsUtils.antialias(g2d);
		g2d.scale(scale, scale);

		// Either there are no page format specification (in which case there is only
		// one infinitie page). 
		PageLayout pageLayout = getDrawingSpecifications().getPageLayout();
		if (drawLimits && pageLayout.hasPageFormat()) {
			// IMPROVE THIS...
			g2d.setColor(Color.RED);
			g2d.draw(pageLayout.getDrawingRectangle());
		}

		
		drawer.setClip(true);
		drawer.drawViewAndCursor(g2d, getView(), getMDCCaret(),
				getDrawingSpecifications());

		drawer.drawCursor(g2d, getView(), getMDCCaret(), getDrawingSpecifications());
		if (caretChanged) {
			// Disarm caret change updates.
			caretChanged = false;
			// Show the cursor.
			Rectangle r = getPointerRectangle();
			if (!g.getClipBounds().contains(r)) {
				// canDraw= false;
				// Let's get some space around the cursor :
				r.height += 4;
				r.width += 4;
				r.x -= 2;
				r.y -= 2;
				SwingUtilities.invokeLater(new VisibilityScroller(r));
			}
		}

	}

	/*
	 * Auxiliary class, used to redraw the window when the cursor is out of the
	 * visible frame.
	 */
	private class VisibilityScroller implements Runnable {

		Rectangle r;

		public VisibilityScroller(Rectangle r) {
			this.r = r;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			scrollRectToVisible(r);
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
	public void print(Graphics g) {
		drawer.setClip(false);
		drawer.draw((Graphics2D) g, getView(), getDrawingSpecifications());
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
		if (drawer.isCached()) {
			drawer.flushCache();
		}
		getDrawingSpecifications().setGraphicDeviceScale(scale);
		repaint();
		revalidate();
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
	 * The rectangle coordinates are given in screen coordinates, not model
	 * coordinates.
	 * 
	 * @return a rectangle describing the current cursor position.
	 */
	Rectangle getPointerRectangle() {
		Rectangle2D r1 = drawer.getRectangleAroundPosition(getView(), workflow
				.getCaret().getInsert().getPosition(),
				getDrawingSpecifications());
		int w = (int) (r1.getWidth() * getScale());
		int h = (int) (r1.getHeight() * getScale());
		if (w < 2) {
			w = 2;
		}
		if (h < 2) {
			h = 2;
		}
		Rectangle r = new Rectangle((int) (r1.getX() * getScale()), (int) (r1
				.getY() * getScale()), w, h);
		return r;
	}

	/**
	 * Chooses between lines and columns for main text. Should change
	 * orientation in sub zones when a zone system is created.
	 * 
	 * @param orientation
	 */
	public void setTextOrientation(TextOrientation orientation) {
		getDrawingSpecifications().setTextOrientation(orientation);
		invalidateView();
	}

	/**
	 * Choose between right-to-left and left-to-right text direction.
	 * 
	 */
	public void setTextDirection(TextDirection direction) {
		getDrawingSpecifications().setTextDirection(direction);
		invalidateView();
	}

	public TextOrientation getTextOrientation() {
		return getDrawingSpecifications().getTextOrientation();
	}

	/**
	 * Choose between right-to-left and left-to-right text direction.
	 * 
	 */
	public TextDirection getTextDirection() {
		return getDrawingSpecifications().getTextDirection();
	}

	/**
	 * Returns a copy of the specifications attached to the current window.
	 */
	public DrawingSpecification getDrawingSpecifications() {
		DrawingSpecification result = drawingSpecifications;
		return result;
	}

	/**
	 * @param drawingSpecifications
	 *            The drawingSpecifications to set.
	 */
	public void setDrawingSpecifications(
			DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
		drawingSpecifications.setGraphicDeviceScale(scale);
		//TODO : remove me after...
		PageLayout p= drawingSpecifications.getPageLayout();
		p.setPageFormat(new PageFormat());
		drawingSpecifications.setPageLayout(p);

		invalidateView();
	}

	public void invalidateView() {
		documentView = null;
		if (drawer.isCached()) {
			drawer.flushCache();
		}
		repaint();
	}

	public char getCurrentSeparator() {
		return getWorkflow().getCurrentSeparator();
	}

	private class JMDCModelEditionListener implements MDCModelEditionListener {

		private static final String CLASS_FULL_NAME = "jsesh.editor.JMDCEditor";

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.editor.MDCModelEditionListener#textEdited
		 * (jsesh.mdc.model.operations.ModelOperation)
		 */

		public void textEdited(ModelOperation op) {
			op.accept(viewUpdater);
			Logger.getLogger(CLASS_FULL_NAME).fine("Text edited");
			caretChanged = true;
			// FIXME : only call revalidate if the dimensions have changed.
			revalidate();
			repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.editor.MDCModelEditionListener#textChanged()
		 */
		public void textChanged() {
			Logger.getLogger(CLASS_FULL_NAME).fine("Text changed");
			documentView = null;
			// repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh
		 * .mdcDisplayer.draw.MDCCaret)
		 */
		public void caretChanged(MDCCaret caret) {
			Logger.getLogger(CLASS_FULL_NAME).fine("Caret changed");
			caretChanged = true;
			repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.editor.MDCModelEditionListener#codeChanged
		 * (java.lang.StringBuffer)
		 */
		public void codeChanged(StringBuffer code) {
			// NO-OP.
		}

		public void separatorChanged() {
			// NO-OP
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.editor.MDCModelEditionListener#focusGained
		 * (java.lang.StringBuffer)
		 */
		public void focusGained(StringBuffer code) {
			// NO-OP.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.editor.MDCModelEditionListener#focusLost(
		 */
		public void focusLost() {
			// NO-OP.
		}
	}

	/**
	 * Paste the content of the clipboard into this editor, at the insert
	 * position.
	 * 
	 * <p>
	 * Currently supports cut and paste from other JSesh editors, and plain text
	 * from other editors. It might be interesting to propose some kind of
	 * switch to paste plain text as Manuel de Codage code later.
	 * 
	 * <p>
	 * Other project : we should also support html in a basic way.
	 */
	public void paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		try {
			System.out.println("getting content");
			Transferable t = clipboard.getContents(this);
			if (t != null) {
				System.out.println("asking for list of top items");
				if (t
						.isDataFlavorSupported(JSeshPasteFlavors.ListOfTopItemsFlavor)) {
					ListOfTopItems l = (ListOfTopItems) t
							.getTransferData(JSeshPasteFlavors.ListOfTopItemsFlavor);
					System.err.println("data got");
					workflow.insertElements(l);
				} else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String string = (String) t
							.getTransferData(DataFlavor.stringFlavor);
					workflow.insertElement(new AlphabeticText('l', string));
				}
			}
		} catch (IllegalStateException exception) {
			exception.printStackTrace();
		} catch (UnsupportedFlavorException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		}

	}

	/**
	 * Copy the selected area in this editor into the clipboard.
	 */
	public void copy() {
		TopItemList top = getWorkflow().getSelectionAsTopItemList();
		MDCModelTransferable transferable = mdcModelTransferableBroker
				.buildTransferable(top);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				transferable, null);
	}

	public void copy(DataFlavor[] dataFlavors) {
		TopItemList top = getWorkflow().getSelectionAsTopItemList();
		MDCModelTransferable transferable = mdcModelTransferableBroker
				.buildTransferable(top, dataFlavors);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				transferable, null);

	}

	/**
	 * Cut the selected area.
	 */
	public void cut() {
		copy();
		getWorkflow().removeSelectedText();
	}

	/**
	 * Sets a class which will be used to build model transferable for cut and
	 * paste.
	 * 
	 * @param mdcModelTransferableBroker
	 *            The mdcModelTransferableBroker to set.
	 */
	public void setMdcModelTransferableBroker(
			MDCModelTransferableBroker mdcModelTransferableBroker) {
		this.mdcModelTransferableBroker = mdcModelTransferableBroker;
	}

	public void clearText() {
		try {
			getWorkflow().setMDCCode("");
		} catch (MDCSyntaxError e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the content of this element, giving it a text in manuel de codage
	 * format.
	 * 
	 * @param mdcText
	 * @throws a
	 *             RuntimeException encapsulating a MDCSyntaxError
	 */
	public void setMDCText(String mdcText) {
		try {
			getWorkflow().setMDCCode(mdcText);
		} catch (MDCSyntaxError e) {
			throw new RuntimeException(e);
		}
	}

	public String getMDCText() {
		return getWorkflow().getMDCCode();
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable
	 *            the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * Does the component keep a cache of rendered groups, sacrifying memory for
	 * speed ?
	 * 
	 * @return
	 */
	public boolean isCached() {
		return drawer.isCached();
	}

	/**
	 * Chose if the component will sacrifice memory for speed.
	 * 
	 * @param c
	 */
	public void setCached(boolean c) {
		drawer.setCached(c);
	}

	public void showShadingPopup() {
		ShadingMenuBuilder menuBuilder = new ShadingMenuBuilder() {
			protected Action buildAction(int shadingCode, String mdcLabel) {
				return new EditorShadeAction(JMDCEditor.this, shadingCode,
						mdcLabel);
			}
		};
		JPopupMenu shadingPopup = menuBuilder.buildPopup();
		Rectangle r = getPointerRectangle();

		shadingPopup.show(this, (int) r.getCenterX(), (int) r.getCenterY());

	}

	/**
	 * Has the current document some unsaved changes ?
	 * @return
	 */
	public boolean mustSave() {
		return workflow.mustSave();
	}


}