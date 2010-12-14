/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model.utilities;

import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.TopItem;

/**
 * Expert able to build a cadrat from a list of two elements.
 * The result is a new cadrat, in which the second element is inserted in the lower row of the first. 
 * @author S. Rosmorduc
 *
 */
public class CadratStarInserter {

	/**
	 * build a new cadrat, in which the second element is inserted in the lower row of the first. 
	 * @param c1 the container cadrat.
	 * @param c2 the cadrat to insert.
	 * @return a cadrat, or null if it couldn't build one.
	 */
	public Cadrat buildCadrat(TopItem c1, TopItem c2) {
		Cadrat result= null;
		// First, build a cadrat from c1 (if c1 is something else, we try to embed it in a cadrat).
		CadratBuilder b= new CadratBuilder();
		result= b.buildCadrat(c1);
		if (result != null) {
			CadratInserterAux inserter= new CadratInserterAux();
			result= inserter.insert(result, c2);
		}
		return result;
	}
	
	/**
	 * Visitor that build a cadrat from a TopItem, if possible.
	 * (actually, it's only possible if the topitem is AlphabeticText or Cadrat.) 
	 * @author S. Rosmorduc
	 *
	 */
	private class CadratBuilder extends ModelElementDeepAdapter {

		Cadrat result;
		
		/**
		 * @param c1
		 * @return the container cadrat.
		 */
		public Cadrat buildCadrat(TopItem c1) {
			result= null;
			c1.accept(this);
			return result;
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementAdapter#visitCadrat(jsesh.mdc.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			result= (Cadrat) c.deepCopy();
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementAdapter#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
		 */
		public void visitAlphabeticText(AlphabeticText t) {
			result= new Cadrat();
			HBox box= new HBox();
			box.addHorizontalListElement(new SubCadrat((AlphabeticText)t.deepCopy()));
			result.addHBox(box);
		}
	}
	
	private class CadratInserterAux extends ModelElementDeepAdapter {

		Cadrat result;
		InnerGroup group;
		
		/**
		 * insert c2 in c.
		 * @param c
		 * @param c2
		 * @return the corresponding cadrat.
		 */
		public Cadrat insert(Cadrat c, TopItem c2) {
			group= null;
			c2.accept(this);
			if (group!= null) {
				c.getHBox(c.getNumberOfHBoxes() - 1).addHorizontalListElement(group);
				result= c;
			} else {
				result= null;
			}
			return result;
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitInnerGroup(jsesh.mdc.model.InnerGroup)
		 */
		public void visitInnerGroup(InnerGroup g) {
			this.group= (InnerGroup) g.deepCopy();
		}
		
	}
}
