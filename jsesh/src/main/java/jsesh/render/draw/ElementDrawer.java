package jsesh.render.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import jsesh.render.style.JSeshStyle;
import jsesh.render.style.ShadingMode;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.model.AbsoluteGroup;
import jsesh.model.AlphabeticText;
import jsesh.model.BasicItemList;
import jsesh.model.Cadrat;
import jsesh.model.Cartouche;
import jsesh.model.HBox;
import jsesh.model.HRule;
import jsesh.model.Hieroglyph;
import jsesh.model.Ligature;
import jsesh.model.LineBreak;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementVisitor;
import jsesh.model.Modifier;
import jsesh.model.ModifiersList;
import jsesh.model.Overwrite;
import jsesh.model.PageBreak;
import jsesh.model.Philology;
import jsesh.model.SubCadrat;
import jsesh.model.Superscript;
import jsesh.model.TabStop;
import jsesh.model.Tabbing;
import jsesh.model.TabbingClear;
import jsesh.model.TopItemList;
import jsesh.model.TopItemState;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.view.MDCView;

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

	private JSeshRenderContext renderContext;

	private JSeshTechRenderContext techRenderContext;

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
	 * super.prepareDrawing(jseshStyle) at some time.
	 * 
	 * @param jseshStyle
	 *            TODO
	 */

	public void prepareDrawing(JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
		this.renderContext = renderContext;
		this.techRenderContext = techRenderContext;
	}

	/**
	 * free some ressources used by ElementDrawer. called after the text has
	 * been drawn. Implementations can override this method, but should call
	 * super.cleanup() in this case.
	 * 
	 */
	public void cleanup() {
		this.renderContext = null;
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
		JSeshStyle jseshStyle = renderContext.jseshStyle();
		ModelElement elt = e.getModel();
		this.postfix = postfix;
		if (elt == null)
			return;
		currentView = e;
		currentTextDirection = jseshStyle.options().textDirection();
		currentTextOrientation = jseshStyle.options().textOrientation();
		this.g = g;
		elt.accept(this);
                currentView= null;
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitAlphabeticText(jsesh.model.AlphabeticText)
	 */
	abstract public void visitAlphabeticText(AlphabeticText t);

	/**
	 * @see jsesh.model.ModelElementVisitor#visitBasicItemList(jsesh.model.BasicItemList)
	 */
	public void visitBasicItemList(BasicItemList l) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitCadrat(jsesh.model.Cadrat)
	 */
	public void visitCadrat(Cadrat c) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitCartouche(jsesh.model.Cartouche)
	 */
	public abstract void visitCartouche(Cartouche c);

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHBox(jsesh.model.HBox)
	 */
	public void visitHBox(HBox b) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHieroglyph(jsesh.model.Hieroglyph)
	 */
	abstract public void visitHieroglyph(Hieroglyph h);

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHRule(jsesh.model.HRule)
	 */
	abstract public void visitHRule(HRule h);

	/**
	 * @see jsesh.model.ModelElementVisitor#visitLigature(jsesh.model.Ligature)
	 */
	public void visitLigature(Ligature l) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitLineBreak(jsesh.model.LineBreak)
	 */
	public void visitLineBreak(LineBreak b) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitModifier(jsesh.model.Modifier)
	 */
	public void visitModifier(Modifier mod) {
		// This method intentionnaly left blank.
		// Could be changed if line breaks are supposed to be drawn
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitModifierList(jsesh.model.ModifiersList)
	 */
	public void visitModifierList(ModifiersList l) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitOverwrite(jsesh.model.Overwrite)
	 */
	public void visitOverwrite(Overwrite o) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitPageBreak(jsesh.model.PageBreak)
	 */
	public void visitPageBreak(PageBreak b) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitPhilology(jsesh.model.Philology)
	 */
	abstract public void visitPhilology(Philology p);

	/**
	 * @see jsesh.model.ModelElementVisitor#visitSubCadrat(jsesh.model.SubCadrat)
	 */
	public void visitSubCadrat(SubCadrat c) {
		// This method intentionnaly left blank.
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitSuperScript(jsesh.model.Superscript)
	 */
	abstract public void visitSuperScript(Superscript s);

	/**
	 * @see jsesh.model.ModelElementVisitor#visitTabStop(jsesh.model.TabStop)
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
	 * @see jsesh.model.ModelElementVisitor#visitTopItemList(jsesh.model.TopItemList)
	 */
	public void visitTopItemList(TopItemList t) {
		// This method intentionnaly left blank.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.ModelElementVisitor#visitAbsoluteGroup(jsesh.model
	 * .AbsoluteGroup)
	 */
	public void visitAbsoluteGroup(AbsoluteGroup g) {
		// This method intentionnaly left blank.
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
		JSeshStyle jseshStyle = renderContext.jseshStyle();
		return jseshStyle.options().paged();
	}

	public boolean isShadeAfter() {
		JSeshStyle jseshStyle = renderContext.jseshStyle();
		// shading lines are always drawn after the rest of the cadrat.
		return shadeAfter
				|| jseshStyle.painting().shadingStyle().equals(
						ShadingMode.LINE_HATCHING);
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

	/**
	 * Sets the coordinate system relative to the original device we are drawing to.
	 * <p>This is useful, for instance, to get a regular hatching on the page, or, if one
	 * wants a line to have exactly the same width, current transform notwithstanding.
	 * @param pageCoordinateSystem
	 * @see #getTransformToPageCoordinates(Graphics2D)
	 */
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

	/**
	 * Access to the render context by the subclasses.
	 * @return
	 */
	protected JSeshRenderContext getRenderContext() {
		return renderContext;
	}

	protected JSeshTechRenderContext getTechRenderContext() {
		return techRenderContext;
	}

	/**
	 * Access to the JSeshStyle by the subclasses.
	 * @return
	 */
	protected JSeshStyle getJSeshStyle() {
		return renderContext.jseshStyle();
	}

}
