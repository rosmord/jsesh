package jsesh.search.backingSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.model.Hieroglyph;
import jsesh.model.MDCPosition;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.HieroglyphicText;
import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.ManuelDeCodage;

/// Extract a normalised list of couples codes/position, usable for a number of searches.
///
/// Embedded signs get their declared Gardiner equivalent (see
/// [HieroglyphOccurrence#getGardinerEquivalent()]).
public class OccurrenceStringBuilder extends ModelElementDeepAdapter {

	private List<HieroglyphOccurrence> codes;

	private MDCPosition position;

	private final ManuelDeCodage manuelDeCodage;

	/// Embedded sign code → Gardiner equivalent.
	private final Map<String, String> gardinerEquivalents;

	/// A builder for texts without embedded signs.
	public OccurrenceStringBuilder() {
		this(new EmbeddedSignList());
	}

	/// @param embeddedSigns the signs embedded in the document of the text.
	public OccurrenceStringBuilder(EmbeddedSignList embeddedSigns) {
		// Can be a singleton. There is only ONE manuel de codage (it holds only standard codes),
		// and it is costly to build.
		this.manuelDeCodage = ManuelDeCodage.getInstance();
		this.gardinerEquivalents = embeddedSigns.getGardinerEquivalents();
	}

	public List<HieroglyphOccurrence> analyzeQuadrat(HieroglyphicText list) {
		this.codes = new ArrayList<>();
		for (int i = 0; i < list.getNumberOfChildren(); i++) {
			this.position = new MDCPosition(i);
			list.getChildAt(i).accept(this);
		}
		return this.codes;
	}

	@Override
	public void visitHieroglyph(Hieroglyph h) {
		CanonicalCode code = manuelDeCodage.getCanonicalCode(h.getCode());
		String equivalent = gardinerEquivalents.get(h.getCode());
		String canonicalEquivalent = equivalent == null ? null : manuelDeCodage.getCanonicalCode(equivalent).code();
		codes.add(new HieroglyphOccurrence(code.code(), canonicalEquivalent, position));
	}
}
