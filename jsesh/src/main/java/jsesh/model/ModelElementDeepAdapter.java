/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.model;

/**
 * A convenience base class form ModelElementVisitors.
 * 
 * <p>Visit methods for non abstract classes call the methods for
 * their abstract parents, which in turn call the methods for their parents,
 * which call visitDefault.
 * <p> visitDefault call <code>accept</code> on
 * all children.</p>
 * 
 * <p> This architecture allows you to customize your visitor at the precision you want.
 * 
 * @author S. Rosmorduc
 *
 */
public class ModelElementDeepAdapter implements ModelElementVisitor {

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementAdapter#visitDefault(jsesh.model.ModelElement)
	 */
	public void visitDefault(ModelElement t) {
		for (int i=0; i < t.getNumberOfChildren(); i++) {
			t.getChildAt(i).accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitAlphabeticText(jsesh.model.AlphabeticText)
	 */
        @Override
	public void visitAlphabeticText(AlphabeticText t) {
		visitBasitItem(t);
		
	}

	/**
	 * @param t
	 */
	public void visitBasitItem(BasicItem t) {
		visitTopItem(t);
	}

	/**
	 * @param t
	 */
	public void visitTopItem(TopItem t) {
		visitDefault(t);
	}

	/* (non-Javadoc)
  	 * @see jsesh.model.ModelElementVisitor#visitBasicItemList(jsesh.model.BasicItemList)
	 */
        @Override
	public void visitBasicItemList(BasicItemList l) {
		visitDefault(l);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitCadrat(jsesh.model.Cadrat)
	 */
        @Override
	public void visitCadrat(Cadrat c) {
		visitBasitItem(c);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitCartouche(jsesh.model.Cartouche)
	 */
        @Override
	public void visitCartouche(Cartouche c) {
		visitInnerGroup(c);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitHBox(jsesh.model.HBox)
	 */
        @Override
	public void visitHBox(HBox b) {
		visitDefault(b);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitHieroglyph(jsesh.model.Hieroglyph)
	 */
        @Override
	public void visitHieroglyph(Hieroglyph h) {
		visitInnerGroup(h);
	}

	/*
	 * 
	 */
	public void visitInnerGroup(InnerGroup g) {
		visitHorizontalListElement(g);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitHRule(jsesh.model.HRule)
	 */
        @Override
	public void visitHRule(HRule h) {
		visitTopItem(h);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitLigature(jsesh.model.Ligature)
	 */
        @Override
	public void visitLigature(Ligature l) {
		visitInnerGroup(l);
	}

        @Override
	public void visitComplexLigature(ComplexLigature ligature) {
		visitHorizontalListElement(ligature);	
	}
	
	public void visitHorizontalListElement(HorizontalListElement elt) {
		visitDefault(elt);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitModifier(jsesh.model.Modifier)
	 */
        @Override
	public void visitModifier(Modifier mod) {
		visitDefault(mod);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitModifierList(jsesh.model.ModifiersList)
	 */
        @Override
	public void visitModifierList(ModifiersList l) {
		visitDefault(l);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitOverwrite(jsesh.model.Overwrite)
	 */
        @Override
	public void visitOverwrite(Overwrite o) {
		visitInnerGroup(o);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitPhilology(jsesh.model.Philology)
	 */
        @Override
	public void visitPhilology(Philology p) {
		visitInnerGroup(p);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitSubCadrat(jsesh.model.SubCadrat)
	 */
        @Override
	public void visitSubCadrat(SubCadrat c) {
		visitInnerGroup(c);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitSuperScript(jsesh.model.Superscript)
	 */
        @Override
	public void visitSuperScript(Superscript s) {
		visitTopItem(s);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitTabStop(jsesh.model.TabStop)
	 */
        @Override
	public void visitTabStop(TabStop t) {
		visitTopItem(t);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitTopItemList(jsesh.model.TopItemList)
	 */
        @Override
	public void visitTopItemList(TopItemList t) {
		visitDefault(t);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitLineBreak(jsesh.model.LineBreak)
	 */
        @Override
	public void visitLineBreak(LineBreak b) {
		visitTopItem(b);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitPageBreak(jsesh.model.PageBreak)
	 */
        @Override
	public void visitPageBreak(PageBreak b) {
		visitTopItem(b);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitAbsoluteGroup(jsesh.model.AbsoluteGroup)
	 */
        @Override
	public void visitAbsoluteGroup(AbsoluteGroup g) {
		visitInnerGroup(g);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElementVisitor#visitZoneStart(jsesh.model.ZoneStart)
	 */
        @Override
	public void visitZoneStart(ZoneStart start) {
		visitTopItem(start);
	}
	
        @Override
	public void visitTabbing(Tabbing tabbing) {
		visitTopItem(tabbing);
	}

        @Override
	public void visitTabbingClear(TabbingClear tabbingClear) {
		visitTopItem(tabbingClear);
	}
}
