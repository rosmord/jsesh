/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdc.utils;

import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.ComplexLigature;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItemList;

/**
 * Expert which extracts a complex ligature from an element.
 * 
 * @author rosmord
 * 
 */
public class ComplexLigatureExtractor {
	private ComplexLigature complexLigature;

	private boolean foundOtherElements;

	private boolean foundTooManyLigatures;

	public void extract(ModelElement elt) {
		complexLigature = null;
		foundOtherElements = false;
		foundTooManyLigatures = false;
		ComplexLigatureExtractorAux aux= new ComplexLigatureExtractorAux();
		elt.accept(aux);
		if (foundTooManyLigatures)
			complexLigature= null;
	}

	/**
	 * @return Returns the complexLigature.
	 */
	public ComplexLigature getComplexLigature() {
		return complexLigature;
	}

	/**
	 * Returns true if elements other than a complex ligature and the necessary
	 * support elements (cadrats and the like) have been found.
	 * 
	 * @return
	 */
	public boolean foundOtherElements() {
		return foundOtherElements;
	}

	private class ComplexLigatureExtractorAux extends ModelElementDeepAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitComplexLigature(jsesh.mdc.model.ComplexLigature)
		 */
		public void visitComplexLigature(ComplexLigature ligature) {
			if (complexLigature != null)
				foundTooManyLigatures = true;
			else 
				complexLigature = (ComplexLigature) ligature.deepCopy();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitDefault(jsesh.mdc.model.ModelElement)
		 */
		public void visitDefault(ModelElement t) {
			// All elements which are not listed are not legal :
			foundOtherElements = true;
			super.visitDefault(t);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitBasicItemList(jsesh.mdc.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			super.visitDefault(l);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHBox(jsesh.mdc.model.HBox)
		 */
		public void visitHBox(HBox b) {
			super.visitDefault(b);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitCadrat(jsesh.mdc.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			super.visitDefault(c);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
		 */
		public void visitTopItemList(TopItemList t) {
			super.visitDefault(t);
		}

	}
}
