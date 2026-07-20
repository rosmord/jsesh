package jsesh.search.ui;

import jsesh.ui.editor.JSeshStyleReference;
import jsesh.defaults.HieroglyphResources;
import jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.glyphs.signdata.HieroglyphDatabase;

/**
 * Resources used by the search UI.
 * 
 * <p> Should typically be created once (spring-like singleton).
 * Basically, the same resources as "normal" jSesh components, but the font is enhanced with wildcards symbols.
 */
public class SearchUIResources {
    private JSeshStyleReference styleReference;
    private HieroglyphResources hieroglyphResources;

    public SearchUIResources(JSeshStyleReference styleReference, HieroglyphResources originalHieroglyphResources) {
        this.styleReference = styleReference;
        this.hieroglyphResources = buildComposite(originalHieroglyphResources);
    }

    private HieroglyphResources buildComposite(HieroglyphResources originalResources) {
		CompositeHieroglyphShapeRepository compositeFont= new CompositeHieroglyphShapeRepository();
        compositeFont.addRepository(WildcardFont.getInstance());
        compositeFont.addRepository(originalResources.hieroglyphShapeRepository());
        
		return new HieroglyphResources(
                compositeFont,
                originalResources.database(),
                originalResources.possibilityRepository());
	}

	public HieroglyphDatabase database() {
        return hieroglyphResources.database();
    }

    public JSeshStyleReference styleReference() {
        return styleReference;
    }

    public HieroglyphResources hieroglyphResources() {
        return hieroglyphResources;
    }

}
