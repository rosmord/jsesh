package jsesh.hieroglyphs.fonts;


import jsesh.hieroglyphs.data.HieroglyphCodesSource;
import jsesh.hieroglyphs.data.coremdc.CanonicalCode;
import jsesh.glyphs.shape.ShapeChar;
import org.qenherkhopeshef.observable.ObservableEventListener;


/**
 * HieroglyphShapeRepository associates glyph shapes with codes.
 * 
 *
 * <ul>
 * <li> !! this is probably one of the oldest java files in JSesh. 24 years old today !!
 * 	It has changed a lot since (it's an interface, it uses default methods...)
 * <li> JSesh was not even called JSesh at that time, and was simply an experiment 
 * 	to see if Java 2.0 was fast enough to
 * draw a hieroglyphic text.
 * <li> there are probably some older codes, but they were translated from tksesh and HieroTeX.
 * </ul>
 * 
 * Created: Mon Jun 10 17:59:58 2002
 * @author <a href="mailto:serge.rosmorduc@qenherkhopeshef.org">Serge ROSMORDUC</a>
 */
public interface HieroglyphShapeRepository extends HieroglyphCodesSource {


	/**
	 * Does this font manager know about a certain code.
	 * @param code the code to search
	 * @return if the code is known by the font manager.
	 */
	default boolean hasCode(CanonicalCode code) {
		return get(code) != null;
	}
	
	/**
	 * Returns the ShapeChar for the given Manuel de Codage code, or null if the code is unknown.
	 * @param code : the <em>canonical code</em> for a sign. 
	 * @return the ShapeChar for the given Manuel de Codage code, or null if the code is unknown.
	 */
	ShapeChar get(CanonicalCode code);

	/**
	 * Returns the small body (bolder) shapeChar for a given manuel de codage code, or null if there is none available.
	 * @param code
	 * @return
	 */
	ShapeChar getSmallBody(CanonicalCode code);

	
	/**
	 * Returns true if new signs are known to have been added since the last call to getCodes.
	 * If the manager is immutable or if one wants to keep things simple, returning false is an option.
	 * @return true if new signs are known to have been added since the last call to getCodes.
	 */
	boolean hasNewSigns();

	
	/**
	 * Add an event listener for this repository.
	 * Allows clients to be notified when the repository changes (new signs, etc.)
	 * @param listener
	 */
	default void addListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {}

	/**
	 * Remove an event listener for this repository.
	 * @param listener
	 */
	default void removeListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {}

}
