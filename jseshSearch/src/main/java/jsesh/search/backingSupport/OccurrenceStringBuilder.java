package jsesh.search.backingSupport;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.drawingElements.HieroglyphDrawer;

import java.util.ArrayList;
import java.util.List;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;

/**
 * Extract a normalised list of couples codes/position, usable for a number of searches.
 */
public class OccurrenceStringBuilder extends ModelElementDeepAdapter {

	private List<HieroglyphOccurrence> codes;

	private int position = -1;

	private ManuelDeCodage manuelDeCodage;
	
	public OccurrenceStringBuilder(ManuelDeCodage manuelDeCodage) {		
		this.manuelDeCodage = manuelDeCodage;
	}

	public List<HieroglyphOccurrence> analyzeQuadrat(TopItemList list) {
		this.codes = new ArrayList<>();
		for (int i = 0; i < list.getNumberOfChildren(); i++) {
			this.position = i;
			list.getChildAt(i).accept(this);
		}
		return this.codes;
	}

	@Override
	public void visitHieroglyph(Hieroglyph h) {
		String code = manuelDeCodage
				.getCanonicalCode(h.getCode());
		codes.add(new HieroglyphOccurrence(code, position));
	}
}
