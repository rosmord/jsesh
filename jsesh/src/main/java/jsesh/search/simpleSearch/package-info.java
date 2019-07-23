/**
 * Simple searches for sequences of signs.
 *
 * <p>Currently includes two types of searches:</p>
 * <ul>
 *     <li>A basic exact match ({@link jsesh.search.simpleSearch.SignStringSearchQuery})</li>
 *     <li>A more advance wildcard enabled search ({@link jsesh.search.simpleSearch.WildCardQuery})</li>
 * </ul>
 *
 * <p> {@link jsesh.search.simpleSearch.WildCardQuery} allows:</p>
 * <ul>
 *     <li>Wildcards '*'</li>
 *     <li>size limitation</li>
 *     <li>search for variant signs</li>
 * </ul>
 */
package jsesh.search.simpleSearch;
