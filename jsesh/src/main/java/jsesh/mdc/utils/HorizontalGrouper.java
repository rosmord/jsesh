/*
 Copyright Serge Rosmorduc
 contributor(s) : Serge J. P. Thomas for the fonts
 serge.rosmorduc@qenherkhopeshef.org

 This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.mdc.utils;

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
 * An helper class for grouping elements horizontally.
 * @author S. Rosmorduc
 */
public class HorizontalGrouper {

	/**
	 * Return a quadrant made of possible InnerGroup elements in elts, where elts
	 * is a list of ModelElements.
	 * <p>
	 * For elements that can be stored into a subcadrat, a subcadrat will be
	 * created.
	 * <p>
	 * Note that embedded innergroups (innergroups inside other innergroups)
	 * won't be retrieved.
	 * <p>
	 * 
	 * @param elts
	 *            a list of model elements.
	 * @return a cadrat, with an horizontal stack of the elements, or null if
	 *         the result would be empty.
	 */

	public Cadrat buildCadrat(List<? extends ModelElement> elts) {
		boolean ok = false;
		Cadrat c = new Cadrat();
		FetcherAux aux = new FetcherAux();

		for (ModelElement e : elts) {
			e.accept(aux);
		}
		if (aux.result.getNumberOfChildren() != 0) {
			ok = true;
			c.addHBox(aux.result);
		}

		if (ok)
			return c;
		else
			return null;
	}

	private static class FetcherAux extends ModelElementDeepAdapter {
		public HBox result;

		FetcherAux() {
			result = new HBox();
		}

		public void visitInnerGroup(InnerGroup g) {
			result.addHorizontalListElement(g);
		}

		/**
		 * Embed complex ligatures in a cadrat
		 */
		public void visitComplexLigature(ComplexLigature ligature) {
			SubCadrat c = new SubCadrat(ligature.buildCadrat());
			result.addHorizontalListElement(c);
		}

		/**
		 * Fetch InnerGroups from a cadrat. If the cadrat is limited to a single
		 * hbox, dig into it a fetch the hbox elements. If it's more complex,
		 * embed the cadrat in a subcadrat (as with other basic item lists).
		 * 
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
		 * 
		 * @param t
		 */
		public void visitBasitItem(BasicItem t) {
			BasicItemList l = new BasicItemList();
			l.addBasicItem(t);
			SubCadrat subc = new SubCadrat(l);
			result.addHorizontalListElement(subc);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementDeepAdapter#visitBasicItemList(jsesh.
		 * mdc.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			SubCadrat subc = new SubCadrat(l);
			result.addHorizontalListElement(subc);
		}
	}

}
