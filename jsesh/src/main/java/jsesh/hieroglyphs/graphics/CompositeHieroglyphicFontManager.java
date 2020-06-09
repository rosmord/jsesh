package jsesh.hieroglyphs.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * A font managers that delegates its jobs to a list of other font managers.
 * <p>
 * Uses the <em>composite</em> pattern.
 * 
 * @author rosmord
 * 
 */

public class CompositeHieroglyphicFontManager implements HieroglyphicFontManager {
	List<HieroglyphicFontManager> managers;

	SortedSet<String> codes;

	public CompositeHieroglyphicFontManager() {
		managers = new ArrayList<HieroglyphicFontManager>();
		codes = null;
	}

	public void addHieroglyphicFontManager(HieroglyphicFontManager manager) {
		managers.add(manager);
	}

	public ShapeChar get(String code) {
		ShapeChar result = null;
		int i = 0;
		while (result == null && i < managers.size()) {
			HieroglyphicFontManager m = managers
					.get(i);
			result = m.get(code);
			i++;
		}
		// TODO : suppress this part and create a SANE system for signs of the form "A23H".
		// TODO : differentiate A23h from A23H...
		if (result == null && code.endsWith("H"))
		{
			String otherCode= code.substring(0, code.length() -1) + "h";
			return get(otherCode);
		}
		return result;
	}

	/**
	 * Search for a small body variant...
	 */
	public ShapeChar getSmallBody(String code) {
		ShapeChar result = null;
		int i = 0;
		while (result == null && i < managers.size()) {
			HieroglyphicFontManager m = managers
					.get(i);
			result = m.getSmallBody(code);
			i++;
		}
		// TODO : suppress this part and create a SANE system for signs of the form "A23H".
		// TODO : differentiate A23h from A23H...
		if (result == null && code.endsWith("H"))
		{
			String otherCode= code.substring(0, code.length() -1) + "h";
			return getSmallBody(otherCode);
		}
		return result;
	}
	
        @Override
	public Set<String> getCodes() {
		
		if (codes == null || hasNewSigns()) {
			codes = new TreeSet<>();
			int i = 0;
			while (i < managers.size()) {
				HieroglyphicFontManager m = managers
						.get(i);
				codes.addAll(m.getCodes());
				i++;
			}
		}
		return Collections.unmodifiableSet(codes);
	}

	public boolean hasNewSigns() {
		boolean result = false;
		for (int i = 0; !result && i < managers.size(); i++) {
			result = result
					|| managers.get(i)
							.hasNewSigns();
		}
		return result;
	}

}
