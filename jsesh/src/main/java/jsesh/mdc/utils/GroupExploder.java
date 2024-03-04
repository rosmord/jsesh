/*
 * Created on 7 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.utils;

import java.util.ArrayList;
import java.util.List;

import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.TopItem;

/**
 * An expert, which breaks Manuel de Codage constructs into smaller elements.
 * 
 * @author S. Rosmorduc
 *  
 */
public class GroupExploder {

	List<TopItem> result = null;

	/**
	 * @param item
	 * @return the list of BasicItems contained in the group. 
	 */
	public List<TopItem> explode(TopItem item) {
		result = new ArrayList<>();
		GroupExploderAux aux = new GroupExploderAux();
		item.accept(aux);
		return result;
	}

	private class GroupExploderAux extends ModelElementDeepAdapter {
		@Override
		public void visitCadrat(Cadrat c) {
			if (c.getNumberOfHBoxes() == 0) {
				// Should not happen !
				result.add(c);
			}
			// If c contains more than one hbox, create a list of cadrats for
			// all these boxes :
			else if (c.getNumberOfHBoxes() > 1) {
				for (int i = 0; i < c.getNumberOfHBoxes(); i++) {
					result.add(c.getChildAt(i).buildTopItem());
				}
			}
			// If c contains only one box, dig into it :
			else {
				HBox hbox = c.getHBox(0);
				hbox.accept(this);
			}
		}

		@Override
		public void visitCartouche(Cartouche c) {
			for (int i = 0; i < c.getBasicItemList().getNumberOfChildren(); i++)
				result.add(c.getBasicItemList().getChildAt(i).buildTopItem());
		}

		@Override
		public void visitHBox(HBox hbox) {
			int nb = hbox.getNumberOfChildren();
			if (nb == 0) {
				// ??? should not happen
			} else if (nb == 1) {
				// If there is only one element, the default would create 
				// an hbox with this very element, which isn't interesting. Hence,
				// we explode this element instead.
				hbox.getChildAt(0).accept(this);
			} else {
				visitDefault(hbox);
			}
		}

		@Override
		public void visitTopItem(TopItem t) {
			result.add(t.deepCopy());
		}
		
		@Override
		public void visitSubCadrat(SubCadrat c) {
			c.getBasicItemList().accept(this);
		}

		/** 
		 * Only hieroglyphs among innergroups can't be broken into smaller parts.
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
		 */
		@Override
		public void visitHieroglyph(Hieroglyph h) {
			result.add(h.buildTopItem());
		}
		
		/* Default behaviour : dig into the element, and add its sub elements as topitems.
		 * (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitDefault(jsesh.mdc.model.ModelElement)
		 */
		@Override
		public void visitDefault(ModelElement t) {
			for (int i=0; i< t.getNumberOfChildren(); i++) {
				TopItem item= t.getChildAt(i).buildTopItem();
				if (item != null)
					result.add(item);
			}
		}
		
		/* (non-Javadoc)
         * @see jsesh.mdc.model.ModelElementDeepAdapter#visitAbsoluteGroup(jsesh.mdc.model.AbsoluteGroup)
         */
		@Override
        public void visitAbsoluteGroup(AbsoluteGroup g) {
            for (int i= 0; i< g.getNumberOfChildren(); i++)
            {
                Hieroglyph h= g.getHieroglyphAt(i).deepCopy();
                h.setExplicitPosition(0,0,100);
                result.add(h.buildTopItem());
            }
        }
	}
}