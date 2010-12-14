package jsesh.mdcDisplayer.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.Ligature;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementVisitor;
import jsesh.mdc.model.Modifier;
import jsesh.mdc.model.ModifiersList;
import jsesh.mdc.model.Overwrite;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TabStop;
import jsesh.mdc.model.Tabbing;
import jsesh.mdc.model.TabbingClear;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.TopItemState;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.ShadingStyle;

/**
 * This file is free Software 
 * under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * 
 * (c) Serge Rosmorduc
 * @author Serge Rosmorduc
 *
 */

/**
 * Object responsible for drawing the basic elements from a text.
 * 
 * It doesn't need to deal at all with sub-elements.
 * 
 * Note that it's called twice for each element, once with postfix= true, the
 * other with posfix=false. Normally, you should draw in only one of these two
 * cases. So the actual drawing methods might look like this one :
 * 
 * <pre>
 * public void visitAlphabeticText(AlphabeticText t) {
 * 	if (!postfix)
 * 		return;
 * 	if (t.getScriptCode() != ScriptCodes.COMMENT) {
 * 		g.setFont(getDrawingSpecifications().getFont(t.getScriptCode()));
 * 		g.drawString(t.getText().toString(), 0, g.getFontMetrics().getAscent());
 * 	}
 * }
 * </pre>
 * 
 */

public abstract class ElementDrawer implements ModelElementVisitor {

	/**
	 * True if the drawer is being called after the sub elements have been
	 * drawn, and false if it's being called before.
	 * 
	 * <p>
	 * For each element, the method drawElement will be called twice, once with
	 * posfix= true, the other with postfix= false. Normally, drawing should
	 * take place only in one of the two cases.
	 */
	protected boolean postfix;

	// only the ViewDrawer can write here
	protected DrawingSpecification drawingSpecifications;

	// /**
	// * True if pagination is on, in which case no line will be drawn for new
	// * pages.
	// */
	// private boolean paged = false;

	private boolean shadeAfter = true;

	private TopItemState drawingState;

	/**
	 * The Graphic context we draw on.
	 */
	protected Graphics2D g;

	/**
	 * The view we are currently drawing.
	 */

	protected MDCView currentView;

	protected TextOrientation currentTextOrientation;

	protected TextDirection currentTextDirection;

	private PageCoordinateSystem pageCoordinateSystem = new PageCoordinateSystem();

	/**
	 * Initialize the drawer before drawing the text. This method is called once
	 * before drawing a whole MDC text. implementations of ElementDrawer can
	 * override this method ; if so, they should call
	 * super.prepareDrawing(drawingSpecifications) at some time.
	 * 
	 * @param drawingSpecifications
	 *            TODO
	 */

	public void prepareDrawing(DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
	}

	/**
	 * free some ressources used by ElementDrawer. called after the text has
	 * been drawn. Implementations can override this method, but should call
	 * super.cleanup() in this case.
	 * 
	 */
	public void cleanup() {
		this.drawingSpecifications = null;
	}

	/**
	 * Draws the element part of a view on the Graphics element specified
	 * before.
	 * 
	 * The graphic context is garanteed to be restored to its former value after
	 * a call to drawElement. So, in most cases, it's safe if the actual drawing
	 * methods modify g ; they don't need to restore it.
	 * 
	 * @param e
	 *            : the view to draw.
	 * @param g
	 *            : the graphics to draw to.
	 * @param postfix
	 *            : this boolean indicates if the method is being called after
	 *            the subelements have been drawn.
	 * @see ElementDrawer#postfix
	 */

	public void drawElement(MDCView e, Graphics2D g, boolean postfix) {
		ModelElement elt = e.getModel();
		this.postfix = postfix;
		if (elt == null)
			return;
		currentView = e;
		currentTextDirection = drawingSpecifications.getTextDirection();
		currentTextOrientation = drawingSpecifications.getTextOrientation();
		this.g = g;
		elt.accept(this);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
	 */
	abstract public void visitAlphabeticText(AlphabeticText t);

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitBasicItemList(jsesh.mdc.model.BasicItemList)
	 */
	public void visitBasicItemList(BasicItemList l) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitCadrat(jsesh.mdc.model.Cadrat)
	 */
	public void visitCadrat(Cadrat c) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitCartouche(jsesh.mdc.model.Cartouche)
	 */
	public abstract void visitCartouche(Cartouche c);

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
	 */
	public void visitHBox(HBox b) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
	 */
	abstract public void visitHieroglyph(Hieroglyph h);

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
	 */
	abstract public void visitHRule(HRule h);

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitLigature(jsesh.mdc.model.Ligature)
	 */
	public void visitLigature(Ligature l) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitLineBreak(jsesh.mdc.model.LineBreak)
	 */
	public void visitLineBreak(LineBreak b) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
	 */
	public void visitModifier(Modifier mod) {
		// This method intentionnaly left blank.
		// Could be changed if line breaks are supposed to be drawn
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
	 */
	public void visitModifierList(ModifiersList l) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
	 */
	public void visitOverwrite(Overwrite o) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitPageBreak(jsesh.mdc.model.PageBreak)
	 */
	public void visitPageBreak(PageBreak b) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
	 */
	abstract public void visitPhilology(Philology p);

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
	 */
	public void visitSubCadrat(SubCadrat c) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
	 */
	abstract public void visitSuperScript(Superscript s);

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitTabStop(jsesh.mdc.model.TabStop)
	 */
	public void visitTabStop(TabStop t) {
		// This method intentionnaly left blank.
	}
	
	public void visitTabbing(Tabbing tabbing) {
		// This method intentionnaly left blank.
	}
	
	public void visitTabbingClear(TabbingClear tabbingClear) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitTopItemList(jsesh.mdc.model.TopItemList)
	 */
	public void visitTopItemList(TopItemList t) {
		// This method intentionnaly left blank.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.model.ModelElementVisitor#visitAbsoluteGroup(jsesh.mdc.model
	 * .AbsoluteGroup)
	 */
	public void visitAbsoluteGroup(AbsoluteGroup g) {
		// This method intentionnaly left blank.
	}

	/**
	 * The ViewDrawer is responsible for setting the drawing specifications
	 * before drawing. Thus, drawing specifications can be shared, and
	 * ElementDrawers can be shared too.
	 * 
	 * @param specifications
	 */

	void setDrawingSpecifications(DrawingSpecification specifications) {
		drawingSpecifications = specifications;
	}

	/**
	 * @return the current drawing state.
	 */
	public TopItemState getDrawingState() {
		return drawingState;
	}

	/**
	 * Sets the current state for drawing (in red, shade ?).
	 * 
	 * @param state
	 */
	public void setDrawingState(TopItemState state) {
		drawingState = state;
	}

	public boolean isPaged() {
		return drawingSpecifications.isPaged();
	}

	public boolean isShadeAfter() {
		// shading lines are always drawn after the rest of the cadrat.
		return shadeAfter
				|| drawingSpecifications.getShadingStyle().equals(
						ShadingStyle.LINE_HATCHING);
	}

	/**
	 * If ShadeAfter is true, the shading should be drawn <em>after</em> the
	 * shaded elements. For systems which support transparency, shadeAfter at
	 * <code>true</code> might be a good choice. Older systems (in the present
	 * case, wmf), should use shadeAfter= false.
	 * 
	 * @param shadeAfter
	 */
	public void setShadeAfter(boolean shadeAfter) {
		this.shadeAfter = shadeAfter;
	}

	public void setPageCoordinateSystem(
			PageCoordinateSystem pageCoordinateSystem) {
		this.pageCoordinateSystem = pageCoordinateSystem;
	}

	/**
	 * Compute a transform from the current local coordinates to the global page
	 * coordinates. If we have a shape in a local (translated, reduced, etc...)
	 * system, we might want to compute it in the original page coordinates (for
	 * instance if we need to layout things on a fixed grid in page space).
	 * 
	 * @return
	 */
	public AffineTransform getTransformToPageCoordinates(Graphics2D g) {
		return pageCoordinateSystem.getTransformToPageCoordinates(g);
	}

	public PageCoordinateSystem getPageCoordinateSystem() {
		return pageCoordinateSystem;
	}

}
