/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.model.tools;

import java.util.Iterator;
import java.util.List;

import jsesh.model.BasicItem;
import jsesh.model.BasicItemList;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.TopItem;

/**
 * This expert is able to extract a list of basicitems from a larger selection.
 * 
 * @author S. Rosmorduc
 *  
 */
public class BasicItemListGrouper {

	private BasicItemList l = null;

	/**
	 * @param elts
	 *            a list of TopItem elements
	 * @return the list of BasicItems contained in elts.
	 */
	public BasicItemList extractBasicItemList(List elts) {
		l = new BasicItemList();
		if (elts != null) {
			BasicItemListGrouperAux aux = new BasicItemListGrouperAux();
			for (Iterator i = elts.iterator(); i.hasNext();) {
				TopItem top = (TopItem) i.next();
				top.accept(aux);
			}
		}
		BasicItemList result = l;
		l = null;
		return result;
	}

	// Just to avoid a cast. Do we need it ?
	private class BasicItemListGrouperAux extends ModelElementDeepAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.model.ModelElementDeepAdapter#visitBasitItem(jsesh.model.BasicItem)
		 */
		public void visitBasitItem(BasicItem t) {
			l.addBasicItem((BasicItem) t.deepCopy());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.model.ModelElementDeepAdapter#visitDefault(jsesh.model.ModelElement)
		 */
		public void visitDefault(ModelElement t) {
			// NO-OP
		}
	}

}