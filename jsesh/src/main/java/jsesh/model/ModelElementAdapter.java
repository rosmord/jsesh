package jsesh.model;

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
	 * @see jsesh.model.ModelElementVisitor#visitAlphabeticText(jsesh.model.AlphabeticText)
	 */
	public void visitAlphabeticText(AlphabeticText t) {
		visitDefault(t);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitBasicItemList(jsesh.model.BasicItemList)
	 */
	public void visitBasicItemList(BasicItemList l) {
		visitDefault(l);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitCadrat(jsesh.model.Cadrat)
	 */
	public void visitCadrat(Cadrat c) {
		visitDefault(c);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitCartouche(jsesh.model.Cartouche)
	 */
	public void visitCartouche(Cartouche c) {
		visitDefault(c);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHBox(jsesh.model.HBox)
	 */
	public void visitHBox(HBox b) {
		visitDefault(b);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHieroglyph(jsesh.model.Hieroglyph)
	 */
	public void visitHieroglyph(Hieroglyph h) {
		visitDefault(h);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHRule(jsesh.model.HRule)
	 */
	public void visitHRule(HRule h) {
		visitDefault(h);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitLigature(jsesh.model.Ligature)
	 */
	public void visitLigature(Ligature l) {
		visitDefault(l);
	}

	public void visitComplexLigature(ComplexLigature ligature) {
		visitDefault(ligature);	
	}
	/**
	 * @see jsesh.model.ModelElementVisitor#visitLineBreak(jsesh.model.LineBreak)
	 */
	public void visitLineBreak(LineBreak b) {
		visitDefault(b);
	}

	public void visitPageBreak(PageBreak b) {
		visitDefault(b);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitModifier(jsesh.model.Modifier)
	 */
	public void visitModifier(Modifier mod) {
		visitDefault(mod);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitModifierList(jsesh.model.ModifiersList)
	 */
	public void visitModifierList(ModifiersList l) {
		visitDefault(l);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitOverwrite(jsesh.model.Overwrite)
	 */
	public void visitOverwrite(Overwrite o) {
		visitDefault(o);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitPhilology(jsesh.model.Philology)
	 */
	public void visitPhilology(Philology p) {
		visitDefault(p);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitSubCadrat(jsesh.model.SubCadrat)
	 */
	public void visitSubCadrat(SubCadrat c) {
		visitDefault(c);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitSuperScript(jsesh.model.Superscript)
	 */
	public void visitSuperScript(Superscript s) {
		visitDefault(s);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitTabStop(jsesh.model.TabStop)
	 */
	public void visitTabStop(TabStop t) {
		visitDefault(t);
	}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitTopItemList(jsesh.model.TopItemList)
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
	 * @see jsesh.model.ModelElementVisitor#visitZoneStart(jsesh.model.ZoneStart)
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
