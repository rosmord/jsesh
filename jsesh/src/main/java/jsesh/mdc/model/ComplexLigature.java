package jsesh.mdc.model;

import jsesh.mdc.interfaces.ComplexLigatureInterface;

public class ComplexLigature extends HorizontalListElement implements ComplexLigatureInterface {
	
	/*
	 * Note about the internal organisation of the ligature :
	 * all elements are stored in the "children" field. The problem is that a ligature may contain 2 or 3 elements.
	 * If hasBeforeGroup is true, beforeGroup is stored in position 0, the main sign in position 1, and afterGroup in position 2.
	 * else, afterGroup is stored in position 1. 
	 */
	
	/**
	 * Does the ligature contain a "G1" element ?
	 */
	private boolean hasBeforeGroup;
	
	/**
	 * Does the ligature contain a "G2" element ?
	 */
	
	private boolean hasAfterGroup;
	
	/*
	 * Create a complex ligature.
	 * <p> An entire group of signs is inserted in an empty space left by a hieroglyph.
	 * These ligature only require some information about the hieroglyph.
	 * @param hieroglyph : the hieroglyph to which 	a group is ligatured
	 * @param cadrat a group to insert
	 * @param position should the group go in front (BEFORE) or after (AFTER) the hieroglyph.
	 */

	
	/**
	 * Create a complex ligature.
	 * <p> An entire group of signs is inserted in an empty space left by a hieroglyph.
	 * These ligature only require some information about the hieroglyph.
	 * @param g1 : the group to be inserted in the first space in the hieroglyph. May be null
	 * @param h : the hieroglyph
	 * @param g2 : the group to be inserted in the second space in the hieroglyph. May be null.
	 */
	
	public ComplexLigature(InnerGroup g1, Hieroglyph h, InnerGroup g2) {
		hasBeforeGroup= (g1 != null);
		hasAfterGroup= (g2 != null);
		if (hasBeforeGroup)
			addChild(g1);
		addChild(h);
		if (hasAfterGroup)
			addChild(g2);
	}

	public void accept(ModelElementVisitor v) {
		v.visitComplexLigature(this);
	}
	
	/*
	 * A value used in comparisons. 
	 * <p> Simply : 2 if there is a "before group", 1 if there is an after group.
	 */
	private int computeLigatureRank() {
		int result= 0;
		if (hasBeforeGroup)
			result+= 2;
		if (hasAfterGroup)
			result+= 1;
		return result;
	}
	
	public int compareToAux(ModelElement e) {
		ComplexLigature lig2= (ComplexLigature) e;
		
		int result= computeLigatureRank() - lig2.computeLigatureRank();
		if (result == 0) {
			result= compareContents(e);
		}
		return result;
	}
	
	public ModelElement deepCopy() {
		InnerGroup copy1= null, copy2= null;
		if (hasBeforeGroup)
			copy1= (InnerGroup) getBeforeGroup().deepCopy();
		if (hasAfterGroup)
			copy2= (InnerGroup) getAfterGroup().deepCopy();
		return new ComplexLigature(copy1, (Hieroglyph)getHieroglyph().deepCopy(), copy2);
		
	}

	public Hieroglyph getHieroglyph() {
		int hindex= 0;
		if (hasBeforeGroup)
			hindex= 1;
		return (Hieroglyph) getChildAt(hindex);
	}

	/**
	 * Return the group of signs to be placed in the first ligature zone for the hieroglyph.
	 * @return an InnerGroup, or null
	 */
	public InnerGroup getBeforeGroup() {
		if (hasBeforeGroup)
			return (InnerGroup) getChildAt(0);
		else
			return null;
	}

	/**
	 * Return the group of signs to be placed in the second ligature zone for the hieroglyph.
	 * @return an InnerGroup, or null
	 */
	
	public InnerGroup getAfterGroup() {
		if (hasAfterGroup) {
			if (hasBeforeGroup)
				return (InnerGroup) getChildAt(2);
			else
				return (InnerGroup) getChildAt(1);
		} else
			return null;
	}
}
