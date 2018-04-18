/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.utils;

import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItem;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.SubCadrat;

/**
 * @author S. Rosmorduc
 *  
 */
public class VerticalGrouper {

	/**
	 * Build a cadrat from a list of elements.
	 * If some of them can't be stacked, return null.
	 * @param elts
	 * @return a cadrat or null.
	 */
	public Cadrat buildCadrat(List elts) {
		CadratBuilderAux aux = new CadratBuilderAux();
		for (Iterator i = elts.iterator(); aux.correct && i.hasNext();) {
			ModelElement e = (ModelElement) i.next();
			e.accept(aux);
		}
		if (aux.correct) {
			return aux.cadrat;
		} else
			return null;
	}

	static private class CadratBuilderAux extends ModelElementAdapter {

		Cadrat cadrat;

		boolean correct;

		/**
		 *  
		 */
		public CadratBuilderAux() {
			cadrat = new Cadrat();
			correct = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitCadrat(jsesh.mdc.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			for (int i = 0; i < c.getNumberOfHBoxes(); i++) {
				HBox box = (HBox) c.getHBox(i).deepCopy();
				cadrat.addHBox(box);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
		 */
		public void visitAlphabeticText(AlphabeticText t) {
			HBox box = new HBox();
			BasicItemList l = new BasicItemList();
			l.addBasicItem((BasicItem) t.deepCopy());
			box.addHorizontalListElement(new SubCadrat(l));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitDefault(jsesh.mdc.model.ModelElement)
		 */
		public void visitDefault(ModelElement t) {
			correct = false;
		}
	}

}