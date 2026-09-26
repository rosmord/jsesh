/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.model.tools;

import java.util.List;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.BasicItemList;
import jsesh.model.Cadrat;
import jsesh.model.HBox;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.SubCadrat;
import jsesh.model.TopItem;

/**
 * @author S. Rosmorduc
 *  
 */
public class VerticalGrouper {

	/**
	 * Build a cadrat from a list of elements.
	 * If some of them can't be stacked, return null.
	 * @param elements the elements to combine in a cadrant.
	 * @return a cadrat or null.
	 */
	public Cadrat buildCadrat(List<TopItem> elements) {
		CadratBuilderAux aux = new CadratBuilderAux();
		int i = 0;
		while (aux.correct && i < elements.size()) {
			if (elements.get(i) instanceof AlphabeticCharacter) {
				// A run of text is stacked as a whole, not letter by letter.
				int end = AlphabeticRuns.runEnd(elements, i, false);
				aux.addText(elements.subList(i, end));
				i = end;
			} else {
				elements.get(i).accept(aux);
				i++;
			}
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
		 * @see jsesh.model.ModelElementAdapter#visitCadrat(jsesh.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			for (int i = 0; i < c.getNumberOfHBoxes(); i++) {
				HBox box = c.getHBox(i).deepCopy();
				cadrat.addHBox(box);
			}
		}

		/// Adds a run of text as one line of the cadrat.
		void addText(List<TopItem> characters) {
			HBox box = new HBox();
			BasicItemList l = new BasicItemList();
			for (TopItem c : characters) {
				l.addBasicItem((AlphabeticCharacter) c.deepCopy());
			}
			box.addHorizontalListElement(new SubCadrat(l));
			cadrat.addHBox(box);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.model.ModelElementAdapter#visitDefault(jsesh.model.ModelElement)
		 */
		public void visitDefault(ModelElement t) {
			correct = false;
		}
	}

}