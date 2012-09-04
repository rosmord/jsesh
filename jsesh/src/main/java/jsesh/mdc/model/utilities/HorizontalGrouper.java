/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model.utilities;

import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.BasicItem;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.ComplexLigature;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.SubCadrat;

/**
 * @author S. Rosmorduc
 *
 */
public class HorizontalGrouper {

	/**
	 * Return a cadrat made of possible InnerGroup elements in elts, where elts is a list of ModelElements.
	 * <p> For elements that can be stored into a subcadrat, a subcadrat will be created.
	 * <p> Note that embedded innergroups (innergroups inside other innergroups) won't be retrieved.<p>
	 * @param elts a list of model elements.
	 * @return a cadrat, with an horizontal stack of the elements, or null if the result would be empty.
	 */

	public Cadrat buildCadrat(List<? extends ModelElement> elts) {
		FetcherAux aux= new FetcherAux();
                for (ModelElement e: elts) {
			e.accept(aux);
		}
		if (aux.result.getNumberOfChildren() == 0) {
			return null;
		} else {
			Cadrat c= new Cadrat();
			c.addHBox(aux.result);
			return c;
		}
	}
	
	private static class FetcherAux extends ModelElementDeepAdapter {
		public HBox result;

		FetcherAux() {
			result= new HBox();
		}
		
		public void visitInnerGroup(InnerGroup g) {
			result.addHorizontalListElement(g);
		}
		
		/**
		 * Embed complex ligatures in a cadrat
		 */
		public void visitComplexLigature(ComplexLigature ligature) {
			SubCadrat c= new SubCadrat(ligature.buildCadrat());
			result.addHorizontalListElement(c);
		}
		/**
		 * Fetch InnerGroups from a cadrat. If the cadrat is limited to a single 
		 * hbox, dig into it a fetch the hbox elements.
		 * If it's more complex, embed the cadrat in a subcadrat (as with other basic item lists).
		 * @param c
		 */
		
		public void visitCadrat(Cadrat c) {
			if (c.getNumberOfHBoxes() == 1) {
				visitDefault(c);
			} else {
				visitBasitItem(c);
			}
		}
		
		/**
		 * Embed simple basicItem in a SubCadrat.
		 * @param t
		 */
		public void visitBasitItem(BasicItem t) {
			BasicItemList l= new BasicItemList();
			l.addBasicItem(t);
			SubCadrat subc= new SubCadrat(l);
			result.addHorizontalListElement(subc);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitBasicItemList(jsesh.mdc.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			SubCadrat subc= new SubCadrat(l);
			result.addHorizontalListElement(subc);
		}
	}

}
