package jsesh.hieroglyphs.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jsesh.hieroglyphs.data.coremdc.CanonicalCode;
import jsesh.glyphs.shape.ShapeChar;
import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventSupport;

/**
 * A font managers that delegates its jobs to a list of other font managers.
 * <p>
 * Uses the <em>composite</em> pattern.
 * 
 * @author rosmord
 * 
 */

public class CompositeHieroglyphShapeRepository implements HieroglyphShapeRepository {
	List<HieroglyphShapeRepository> managers;

	SortedSet<String> codes;

	private final ObservableEventSupport<HieroglyphShapeRepositoryChangedEvent> eventSupport = new ObservableEventSupport<>();
	private final ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> childListener = e -> eventSupport.fireEvent(e);

	public CompositeHieroglyphShapeRepository() {
		managers = new ArrayList<HieroglyphShapeRepository>();
		codes = null;
	}

	/**
	 * Add another repository.
	 * <p> The repositories will be searched in the order they were added.
	 * @param manager
	 */
	public void addRepository(HieroglyphShapeRepository manager) {
		managers.add(manager);
		manager.addListener(childListener);
		eventSupport.fireEvent(new HieroglyphShapeRepositoryChangedEvent());
	}

	@Override
	public void addListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {
		eventSupport.addListener(listener);
	}

	@Override
	public void removeListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {
		eventSupport.removeListener(listener);
	}

	@Override
	public ShapeChar get(CanonicalCode code) {
		ShapeChar result = null;
		int i = 0;
		while (result == null && i < managers.size()) {
			HieroglyphShapeRepository m = managers
					.get(i);
			result = m.get(code);
			i++;
		}		
		return result;
	}

	/**
	 * Search for a small body variant...
	 */
	@Override
	public ShapeChar getSmallBody(CanonicalCode code) {
		ShapeChar result = null;
		int i = 0;
		while (result == null && i < managers.size()) {
			HieroglyphShapeRepository m = managers
					.get(i);
			result = m.getSmallBody(code);
			i++;
		}		
		return result;
	}

	@Override
	public Set<String> getCodes() {

		if (codes == null || hasNewSigns()) {
			codes = new TreeSet<>();
			int i = 0;
			while (i < managers.size()) {
				HieroglyphShapeRepository m = managers
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
