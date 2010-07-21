package jsesh.mdc.model;

/**
 * Simple implementation for ModelElementVisitor.
 * All method call visitDefault, which is empty.
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class ModelElementAdapter implements ModelElementVisitor {

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
	 */
	public void visitAlphabeticText(AlphabeticText t) {
		visitDefault(t);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitBasicItemList(jsesh.mdc.model.BasicItemList)
	 */
	public void visitBasicItemList(BasicItemList l) {
		visitDefault(l);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitCadrat(jsesh.mdc.model.Cadrat)
	 */
	public void visitCadrat(Cadrat c) {
		visitDefault(c);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitCartouche(jsesh.mdc.model.Cartouche)
	 */
	public void visitCartouche(Cartouche c) {
		visitDefault(c);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
	 */
	public void visitHBox(HBox b) {
		visitDefault(b);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
	 */
	public void visitHieroglyph(Hieroglyph h) {
		visitDefault(h);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
	 */
	public void visitHRule(HRule h) {
		visitDefault(h);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitLigature(jsesh.mdc.model.Ligature)
	 */
	public void visitLigature(Ligature l) {
		visitDefault(l);
	}

	public void visitComplexLigature(ComplexLigature ligature) {
		visitDefault(ligature);	
	}
	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitLineBreak(jsesh.mdc.model.LineBreak)
	 */
	public void visitLineBreak(LineBreak b) {
		visitDefault(b);
	}

	public void visitPageBreak(PageBreak b) {
		visitDefault(b);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
	 */
	public void visitModifier(Modifier mod) {
		visitDefault(mod);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
	 */
	public void visitModifierList(ModifiersList l) {
		visitDefault(l);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
	 */
	public void visitOverwrite(Overwrite o) {
		visitDefault(o);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
	 */
	public void visitPhilology(Philology p) {
		visitDefault(p);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
	 */
	public void visitSubCadrat(SubCadrat c) {
		visitDefault(c);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
	 */
	public void visitSuperScript(Superscript s) {
		visitDefault(s);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitTabStop(jsesh.mdc.model.TabStop)
	 */
	public void visitTabStop(TabStop t) {
		visitDefault(t);
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitTopItemList(jsesh.mdc.model.TopItemList)
	 */
	public void visitTopItemList(TopItemList t) {
		visitDefault(t);
	}

	public void visitAbsoluteGroup(AbsoluteGroup g) {
		visitDefault(g);
	}

	public void visitDefault(ModelElement t) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElementVisitor#visitZoneStart(jsesh.mdc.model.ZoneStart)
	 */
	public void visitZoneStart(ZoneStart start) {
		visitDefault(start);
	}
	
	public void visitTabbing(Tabbing tabbing) {
		visitDefault(tabbing);		
	}
	
	public void visitTabbingClear(TabbingClear tabbingClear) {
		visitDefault(tabbingClear);
	}
	

}
