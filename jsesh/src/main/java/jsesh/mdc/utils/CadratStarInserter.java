/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.utils;

import java.util.Collections;
import java.util.List;

import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.HorizontalListElement;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.TopItem;

/**
 * Expert able to build a quadrant from a list of two elements. The result is a
 * new quadrant, in which the second element is inserted in the lower row of the
 * first.
 * 
 * @author S. Rosmorduc
 * 
 */
public class CadratStarInserter {

	/**
	 * build a new quadrant, in which the second element is inserted in the
	 * lower row of the first.
	 * 
	 * @param c1
	 *            the container quadrant.
	 * @param c2
	 *            the quadrant to insert.
	 * @return a quadrant, or null if it couldn't build one.
	 */
	public Cadrat buildCadrat(TopItem c1, TopItem c2) {
		Cadrat result = null;
		// First, build a cadrat from c1 (if c1 is something else, we try to
		// embed it in a cadrat).
		CadratBuilder b = new CadratBuilder();
		result = b.buildCadrat(c1);
		if (result != null) {
			CadratInserterAux inserter = new CadratInserterAux();
			result = inserter.insert(result, c2);
		}
		return result;
	}

	/**
	 * Visitor that build a quadrant from a TopItem, if possible.
	 * <p>
	 * <p>
	 * Note : with the current structure, which is a bit convoluted simply
	 * because of original limitations in the grammar (some of which have been
	 * lifted, there are probably a number of different cases to consider.
	 * 
	 * @author S. Rosmorduc
	 */
	private class CadratBuilder extends ModelElementDeepAdapter {

		Cadrat result;

		/**
		 * @param c1
		 * @return the container cadrat.
		 */
		public Cadrat buildCadrat(TopItem c1) {
			result = null;
			c1.accept(this);
			return result;
		}
		
		public void visitCadrat(Cadrat c) {
			result = (Cadrat) c.deepCopy();
		}
	
		public void visitAlphabeticText(AlphabeticText t) {
			result = new Cadrat();
			HBox box = new HBox();
			box.addHorizontalListElement(new SubCadrat((AlphabeticText) t
					.deepCopy()));
			result.addHBox(box);
		}
	}

	/**
	 * Visitor which adds elements of the second top item inside the new
	 * quadrant.
	 * <p>
	 * If the second element is basically a list of items (i.e. a one level
	 * quadrant), we take its elements, and add them.
	 * <p>
	 * If the second element is more complex, we make a
	 * 
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 * 
	 */
	private class CadratInserterAux extends ModelElementAdapter {

		/**
		 * The result quadrant.
		 */
		Cadrat result;
		boolean noChange = true;

		/**
		 * insert c2 in c.
		 * 
		 * @param c
		 *            the target quadrant (will be modified)
		 * @param c2
		 * @return the corresponding cadrat.
		 */
		public Cadrat insert(Cadrat c, TopItem c2) {
			result = c;
			c2.accept(this);
			if (noChange)
				return null;
			else
				return c;
		}

		@Override
		public void visitCadrat(Cadrat c) {
			if (c.containsOnlyOneSign()) {
				HieroglyphExtractor ex = new HieroglyphExtractor();
				List<Hieroglyph> signList = ex.extractHieroglyphs(Collections
						.singletonList(c));
				if (signList.size() == 1) {
					Hieroglyph s = signList.get(0).deepCopy();
					addToResult(s);
				} else {
					System.err
							.println("problem : I think this contains only one sign "
									+ c);
				}
			} else {
				visitDefault(c);
			}

		}

		@Override
		public void visitDefault(ModelElement t) {
			HorizontalListElement h = t.buildHorizontalListElement();
			if (h != null)
				addToResult(h);
		}

		private void addToResult(HorizontalListElement g) {
			noChange = false;
			result.getHBox(result.getNumberOfHBoxes() - 1)
					.addHorizontalListElement(g);
		}
	}
}
