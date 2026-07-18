/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.model.tools;

import java.util.ArrayList;
import java.util.List;

import jsesh.model.BasicItemList;
import jsesh.model.Cadrat;
import jsesh.model.HBox;
import jsesh.model.InnerGroup;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.TopItemList;

/**
 * Expert which can extract innergroups from a list of top level items
 * @author rosmord
 *
 */
public class InnerGroupExtractor {

	private boolean foundOtherElements;
	private List<InnerGroup> innerGroups;
	/**
	 * Extract Inner groups from a list of TopItems.
	 * @param list
	 */
	public void extract(List<? extends ModelElement> list) {
		foundOtherElements= false;
		innerGroups= new ArrayList<>();
		InnerGroupExtractorAux aux= new InnerGroupExtractorAux();
		for (ModelElement elt: list) {
			elt.accept(aux);
		}		
	}

	/**
	 * @return
	 */
	public boolean foundOtherElements() {
		return foundOtherElements;
	}

	/**
	 * @return
	 */
	public List getInnerGroups() {
		return innerGroups;
	}
	
	private class InnerGroupExtractorAux extends ModelElementDeepAdapter {
		
		/* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitInnerGroup(jsesh.model.InnerGroup)
		 */
		@Override
		public void visitInnerGroup(InnerGroup g) {
			innerGroups.add(g.deepCopy());
		}
		
		/* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitDefault(jsesh.model.ModelElement)
		 */
		public void visitDefault(ModelElement t) {
			super.visitDefault(t);
			foundOtherElements= true;
		}
		
		/* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitBasicItemList(jsesh.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			super.visitDefault(l);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitHBox(jsesh.model.HBox)
		 */
		public void visitHBox(HBox b) {
			super.visitDefault(b);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitCadrat(jsesh.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			super.visitDefault(c);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitTopItemList(jsesh.model.TopItemList)
		 */
		public void visitTopItemList(TopItemList t) {
			super.visitDefault(t);
		}
	}

}
