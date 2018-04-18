/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdc.utils;

import java.util.List;

import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.SubCadrat;

/**
 * This Expert is able to build an InnerGroup from a list of top-level elements
 * (discarding unsuitable ones).
 * <p>
 * If the list is composed of exactly one element which contains only one
 * InnerGroup, then the systems stops right away.
 * 
 * @author rosmord
 * 
 */
public class InnerGroupBuilder {

	InnerGroup innerGroup;

	/**
	 * @param list
	 * @return
	 */
	public void buildHorizontalElement(List list) {
		innerGroup = null;

		// First, try the case where the list contains an innergroup and nothing else (except the cadrat, etc... needed for this group).
		if (list.size() == 1) {
			InnerGroupExtractor extractor = new InnerGroupExtractor();
			extractor.extract(list);
			if (!extractor.foundOtherElements()
					&& extractor.getInnerGroups().size() == 1) {
				innerGroup = (InnerGroup) extractor.getInnerGroups().get(0);
			}
		}
		// General case : for now, we extract Basic Items and place them in a subcadrat.
		if (innerGroup== null) {
			BasicItemListGrouper basicItemListGrouper= new BasicItemListGrouper();
			BasicItemList basicItemList= basicItemListGrouper.extractBasicItemList(list);
			if (basicItemList.getNumberOfChildren() != 0)
				innerGroup= new SubCadrat(basicItemList);
		}
	}

	/**
	 * @return
	 */
	public InnerGroup getGroup() {
		return innerGroup;
	}

}
