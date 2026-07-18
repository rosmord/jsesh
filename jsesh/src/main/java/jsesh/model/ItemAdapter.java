package jsesh.model;

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
	 * @see jsesh.model.ModelElementVisitor#visitBasicItemList(jsesh.model.BasicItemList)
	 */
	final public void visitBasicItemList(BasicItemList l) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHBox(jsesh.model.HBox)
	 */
	final public void visitHBox(HBox b) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitHieroglyph(jsesh.model.Hieroglyph)
	 */
	final 	public void visitHieroglyph(Hieroglyph h) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitLigature(jsesh.model.Ligature)
	 */
	final 	public void visitLigature(Ligature l) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitModifier(jsesh.model.Modifier)
	 */
	final 	public void visitModifier(Modifier mod) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitModifierList(jsesh.model.ModifiersList)
	 */
	final 	public void visitModifierList(ModifiersList l) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitOverwrite(jsesh.model.Overwrite)
	 */
	final 	public void visitOverwrite(Overwrite o) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitPhilology(jsesh.model.Philology)
	 */
	final 	public void visitPhilology(Philology p) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitSubCadrat(jsesh.model.SubCadrat)
	 */
	final 	public void visitSubCadrat(SubCadrat c) {}

	/**
	 * @see jsesh.model.ModelElementVisitor#visitTopItemList(jsesh.model.TopItemList)
	 */
	final 	public void visitTopItemList(TopItemList t) {}


}
