package jsesh.search.backingSupport;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItemList;

import java.util.ArrayList;
import java.util.List;
import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;

/**
 * Extract a normalised list of couples codes/position, usable for a number of searches.
 */
public class OccurrenceStringBuilder extends ModelElementDeepAdapter {

	private List<HieroglyphOccurrence> codes;

	private int position = -1;
	

	public List<HieroglyphOccurrence> analyzeQuadrant(TopItemList list) {
		this.codes = new ArrayList<>();
		for (int i = 0; i < list.getNumberOfChildren(); i++) {
			this.position = i;
			list.getChildAt(i).accept(this);
		}
		return this.codes;
	}

	@Override
	public void visitHieroglyph(Hieroglyph h) {
		String code = HieroglyphDatabaseRepository.getHieroglyphDatabase()
				.getCanonicalCode(h.getCode());
		codes.add(new HieroglyphOccurrence(code, position));
	}
}
