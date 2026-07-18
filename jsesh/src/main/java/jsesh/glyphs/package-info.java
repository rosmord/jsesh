/**
 * Deals with hieroglyphic signs in general: drawing and values.
 *
 * <p>The data can be drawn from various sources: internal data, external
 * files, database...
 *
 * <p>The system should work with no configuration, but should allow alternate
 * sources to be used. The whole thing should be independent from the JSesh
 * main application.
 *
 * <p>The solution is to provide a default implementation which allows for some
 * tailoring — see {@link jsesh.glyphs.data.HieroglyphDatabaseFactory} and
 * {@code jsesh.defaults.HieroglyphResourcesBuilder}. However, if one wants
 * full freedom on the subject, all classes that use hieroglyphs should be able
 * to use the source of their choice.
 */
package jsesh.glyphs;
