package jsesh.hieroglyphs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jsesh.bzr.simple.ShapeChar;

/**
 * A font managers that delegates its jobs to a list of other font managers.
 * <p>
 * Uses the <em>composite</em> pattern.
 * 
 * @author rosmord
 * 
 */

public class CompositeHieroglyphicFontManager implements HieroglyphicFontManager {
	List managers;

	SortedSet codes;

	public CompositeHieroglyphicFontManager() {
		managers = new ArrayList();
		codes = null;
	}

	public void addHieroglyphicFontManager(HieroglyphicFontManager manager) {
		managers.add(manager);
	}

	public ShapeChar get(String code) {
		ShapeChar result = null;
		int i = 0;
		while (result == null && i < managers.size()) {
			HieroglyphicFontManager m = (HieroglyphicFontManager) managers
					.get(i);
			result = m.get(code);
			i++;
		}
		return result;
	}

	public Set getCodes() {
		
		if (codes == null || hasNewSigns()) {
			codes = new TreeSet();
			int i = 0;
			while (i < managers.size()) {
				HieroglyphicFontManager m = (HieroglyphicFontManager) managers
						.get(i);
				codes.addAll(m.getCodes());
				i++;
			}
		}
		return codes;
	}

	public boolean hasNewSigns() {
		boolean result = false;
		for (int i = 0; !result && i < managers.size(); i++) {
			result = result
					|| ((HieroglyphicFontManager) managers.get(i))
							.hasNewSigns();
		}
		return result;
	}

}
