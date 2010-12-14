package jsesh.mdc.model;

/**
 * An ItemAdapter is a ModelElementVisitor designed to work on Items (Basic and Top).
 * That is, all other elements are ignored. This adapter can be useful to guarantee that 
 * no (Basic|Top)Item is forgotten.
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
abstract public class ItemAdapter implements ModelElementVisitor {

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitBasicItemList(jsesh.mdc.model.BasicItemList)
	 */
	final public void visitBasicItemList(BasicItemList l) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHBox(jsesh.mdc.model.HBox)
	 */
	final public void visitHBox(HBox b) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
	 */
	final 	public void visitHieroglyph(Hieroglyph h) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitLigature(jsesh.mdc.model.Ligature)
	 */
	final 	public void visitLigature(Ligature l) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitModifier(jsesh.mdc.model.Modifier)
	 */
	final 	public void visitModifier(Modifier mod) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitModifierList(jsesh.mdc.model.ModifiersList)
	 */
	final 	public void visitModifierList(ModifiersList l) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitOverwrite(jsesh.mdc.model.Overwrite)
	 */
	final 	public void visitOverwrite(Overwrite o) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
	 */
	final 	public void visitPhilology(Philology p) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitSubCadrat(jsesh.mdc.model.SubCadrat)
	 */
	final 	public void visitSubCadrat(SubCadrat c) {}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitTopItemList(jsesh.mdc.model.TopItemList)
	 */
	final 	public void visitTopItemList(TopItemList t) {}


}
