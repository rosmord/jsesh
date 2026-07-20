package jsesh.search.backingSupport;

import java.util.ArrayList;
import java.util.List;

import jsesh.model.Hieroglyph;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.TopItemList;
import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.ManuelDeCodage;

/**
 * Extract a normalised list of couples codes/position, usable for a number of searches.
 */
public class OccurrenceStringBuilder extends ModelElementDeepAdapter {

	private List<HieroglyphOccurrence> codes;

	private int position = -1;

	private ManuelDeCodage manuelDeCodage;
	
	public OccurrenceStringBuilder() {		
		// Can be a singleton. There is only ONE manuel de codage (it holds only standard codes),
		// and it is costly to build.
		this.manuelDeCodage = ManuelDeCodage.getInstance();
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
		CanonicalCode code = manuelDeCodage
				.getCanonicalCode(h.getCode());
		codes.add(new HieroglyphOccurrence(code.code(), position));
	}
}
