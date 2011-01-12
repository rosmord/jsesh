package jsesh.mdc.model;


/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public interface ModelElementVisitor {
	void visitAlphabeticText(AlphabeticText t);
	void visitBasicItemList(BasicItemList l);
	void visitCadrat(Cadrat c);
	void visitCartouche(Cartouche c);
	void visitHBox(HBox b);
	void visitHieroglyph(Hieroglyph h);
	void visitHRule(HRule h);
	void visitLigature(Ligature l);
	void visitModifier(Modifier mod);
	void visitModifierList(ModifiersList l);
	void visitOverwrite(Overwrite o);
	void visitPhilology(Philology p);
	void visitSubCadrat(SubCadrat c);
	void visitSuperScript(Superscript s);
	void visitTabStop(TabStop t);
	void visitTopItemList(TopItemList t);
	void visitLineBreak(LineBreak b);
	void visitPageBreak(PageBreak b);
	void visitAbsoluteGroup(AbsoluteGroup g);
	void visitZoneStart(ZoneStart start);
	void visitComplexLigature(ComplexLigature ligature);
	void visitTabbing(Tabbing tabbing);
	void visitTabbingClear(TabbingClear tabbingClear);
}
