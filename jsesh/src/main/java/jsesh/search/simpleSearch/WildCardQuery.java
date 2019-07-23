package jsesh.search.simpleSearch;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.search.MdCSearchQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple slightly improved search system, with limited wildcard capabilities.
 * <p>The full regexp system is under way.</p>
 */
public class WildCardQuery implements MdCSearchQuery  {

	private List<WildcardToken> patterns;

	/**
	 * Initializes a search from a mdc Text.
	 * <p>The text is relatively standard, except that a number of additional codes have been added :</p>
	 * <ul>
	 *     <li>QUERYSKIP : skips any number of signs</li>
	 *     <li>QUERYSETB and QUERYSETE : enclose a list of possible codes, like [...] in standard Unix regexps.</li>*
	 * </ul>
	 *
	 * The search can also recognise signs variants if needed.
	 * @param mdcCodes
	 */
	public WildCardQuery(String mdcCodes) {
	}

	

	@Override
	public List<MDCPosition> doSearch(TopItemList items) {
		ArrayList<MDCPosition> result = new ArrayList<>();
		OccurrenceStringBuilder builder = new OccurrenceStringBuilder();
		List<HieroglyphOccurrence> l = builder.analyzeQuadrant(items);
		for (int i= 0; i < l.size(); i++) {
			// note : remember that sublist takes a view. No actual copy is done.
			if (matchAt(patterns, l.subList(i, l.size()))) {
				result.add(new MDCPosition(items, l.get(i).getPosition()));
			}
		}
		return result;

	}

	private boolean matchAt(List<WildcardToken> pattern, List<HieroglyphOccurrence> toMatch) {
		boolean result;
		if (pattern.get(0).isRepeat()) {
			result = matchAt(pattern.subList(1, pattern.size()), toMatch)
					|| pattern.get(0).match(toMatch.get(0)) && matchAt( pattern, toMatch.subList(1,toMatch.size()));
		} else {
			result = pattern.get(0).match(toMatch.get(0))
							&& matchAt( pattern.subList(1,pattern.size()), toMatch.subList(1,toMatch.size()));
			;
		}
		return result;
	}


}
