package jsesh.hieroglyphs.fonts;

import java.util.Set;

import jsesh.hieroglyphs.signshape.ShapeChar;

/**
 * Standard implementation of the HieroglyphShapeRepository interface, without user defined signs.
 */

class StandardFontShapeRepository implements HieroglyphShapeRepository {
    private static final StandardFontShapeRepository INSTANCE = new StandardFontShapeRepository();

    private final CompositeHieroglyphShapeRepository implementation;

    private StandardFontShapeRepository() {
        implementation = new CompositeHieroglyphShapeRepository();
        implementation.addHieroglyphicFontManager(new ResourcesHieroglyphicShapeRepository(Constants.STANDARD_JSESH_FONT_RESOURCE_PATH));
        implementation.addHieroglyphicFontManager(GnutraceHieroglyphShapeRepository.getInstance());
    }

    public static StandardFontShapeRepository getInstance() {
        return INSTANCE;
    }


    @Override
    public Set<String> getCodes() {
        return implementation.getCodes();
    }

    @Override
    public ShapeChar get(String code) {
        return implementation.get(code);
    }

    @Override
    public ShapeChar getSmallBody(String code) {
        return implementation.getSmallBody(code);
    }

    @Override
    public boolean hasNewSigns() {
        return implementation.hasNewSigns();
    }    
}
