/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.ArrayList;
import java.util.List;


/**
 * BasicItemList : an auxiliary class, used as <em>implementation</em> for all elements which contain basic items.
 * 
 * As such, a BasicItemList does not constitute an element.
 * 
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */

public class BasicItemList
    extends EmbeddedModelElement implements ItemSequence {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6889728940705626342L;

	public BasicItemList() {
	}

	public void addBasicItem(BasicItem basicItem) {
		addChild(basicItem);
	}
	
	public void addBasicItemAt(int id, BasicItem basicItem) {
		addChildAt(id, basicItem);
	}

	@Override
	public TopItem getItemAt(int index) {
		return (TopItem) getChildAt(index);
	}

	/// Only [BasicItem]s (quadrats, alphabetic text, comments) can be found in
	/// such a list: no line or page break, for instance.
	@Override
	public boolean accepts(TopItem item) {
		return item instanceof BasicItem;
	}

	@Override
	public void insertItemsAt(int index, List<? extends TopItem> items) {
		if (!acceptsAll(items)) {
			throw new IllegalArgumentException("Only basic items can be inserted here: " + items);
		}
		int i = index;
		for (TopItem item : items) {
			addChildAt(i++, item);
		}
	}

	@Override
	public List<TopItem> removeItems(int start, int end) {
		List<TopItem> result = new ArrayList<>();
		for (EmbeddedModelElement e : removeChildren(start, end)) {
			result.add((TopItem) e);
		}
		return result;
	}

        @Override
	public void accept(ModelElementVisitor v) {
		v.visitBasicItemList(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
        @Override
	public String toString() {
		return getChildrenAsString();
	}

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
	 */
        @Override
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}
	

	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#deepCopy()
	 */
        @Override
	public BasicItemList deepCopy() {
		BasicItemList r= new BasicItemList();
		copyContentTo(r);
		return r; 
	}
	
	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return new SubCadrat(deepCopy());
	}
	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#buildTopItem()
	 */
        @Override
	public TopItem buildTopItem() {
		Cadrat c= new Cadrat();
		HBox b= new HBox();
		SubCadrat sub= new SubCadrat((BasicItemList) this.deepCopy());
		b.addHorizontalListElement(sub);
		c.addHBox(b);
		return c;
	}
}
