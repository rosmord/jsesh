/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdc.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItemList;

/**
 * Expert which can extract innergroups from a list of top level items
 * @author rosmord
 *
 */
public class InnerGroupExtractor {

	private boolean foundOtherElements;
	private List innerGroups;
	/**
	 * Extract Inner groups from a list of TopItems.
	 * @param list
	 */
	public void extract(List list) {
		foundOtherElements= false;
		innerGroups= new ArrayList();
		InnerGroupExtractorAux aux= new InnerGroupExtractorAux();
		Iterator iterator= list.iterator();
		while (iterator.hasNext()) {
			ModelElement elt= (ModelElement) iterator.next();
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
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitInnerGroup(jsesh.mdc.model.InnerGroup)
		 */
		public void visitInnerGroup(InnerGroup g) {
			innerGroups.add(g.deepCopy());
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitDefault(jsesh.mdc.model.ModelElement)
		 */
		public void visitDefault(ModelElement t) {
			super.visitDefault(t);
			foundOtherElements= true;
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitBasicItemList(jsesh.mdc.model.BasicItemList)
		 */
		public void visitBasicItemList(BasicItemList l) {
			super.visitDefault(l);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHBox(jsesh.mdc.model.HBox)
		 */
		public void visitHBox(HBox b) {
			super.visitDefault(b);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitCadrat(jsesh.mdc.model.Cadrat)
		 */
		public void visitCadrat(Cadrat c) {
			super.visitDefault(c);
		}
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitTopItemList(jsesh.mdc.model.TopItemList)
		 */
		public void visitTopItemList(TopItemList t) {
			super.visitDefault(t);
		}
	}

}
