package jsesh.defaults;

import jsesh.editor.PossibilityRepository;
import jsesh.glyphs.data.HieroglyphDatabase;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;

/**
 * A repository giving coordinated access to hieroglyphic fonts resources, including automated completion.
 * @param hieroglyphShapeRepository the repository of hieroglyphic shapes (for drawing).
 * @param database the hieroglyphic database with access to sign information (values, descriptions, variants).
 * @param possibilityRepository the repository of possibilities for automated completion.
 */
public record HieroglyphResources(
    HieroglyphShapeRepository hieroglyphShapeRepository,
    HieroglyphDatabase database,
    PossibilityRepository possibilityRepository
) {

}
