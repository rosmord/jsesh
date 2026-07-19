/**
 * Coordinated assembly of the hieroglyphic font resources JSesh needs to draw
 * and complete signs.
 *
 * <h2>Layering</h2>
 * <pre>
 *   PredefinedFonts  ──▶  HieroglyphResourcesBuilder  ──▶  HieroglyphResources
 *     (leaf fonts)           (composition, pure)             (immutable result)
 *                                    ▲
 *                                    │ DirectoryHolder (plain, observable)
 *                                    │
 *              UserFontDirectoryManager  ← app-scoped, the ONLY class here
 *                                          that touches java.util.prefs
 * </pre>
 *
 * <ul>
 * <li>{@link jsesh.defaults.PredefinedFonts} builds individual, self-contained
 *     fonts (standard JSesh font, GnuTrace font).
 * <li>{@link jsesh.defaults.HieroglyphResourcesBuilder} composes fonts (order
 *     matters: first added is searched first) plus the sign database and the
 *     completion possibilities. It depends only on a plain
 *     {@link jsesh.utils.io.DirectoryHolder} for user fonts, so it stays pure and
 *     unit-testable with no persistence involved.
 * <li>{@link jsesh.defaults.HieroglyphResources} is the immutable result handed
 *     to the rest of the application.
 * <li>{@link jsesh.defaults.UserFontDirectoryManager} is the app-scoped adapter
 *     that persists the user-font folder to preferences and vends the shared
 *     {@code DirectoryHolder} feeding the builder. It is also the write side:
 *     new user signs are written through it because writing "only needs to know
 *     the folder". Reader (the repository) and writer (this manager) never
 *     reference each other — they rendezvous at the {@code DirectoryHolder}.
 *     It offers that write side as
 *     {@link jsesh.glyphs.signsource.UserSignWriter}, so the sign importer down
 *     in {@code jsesh.glyphs} depends on the one-method contract rather than on
 *     this package.
 * </ul>
 *
 * <p>A library embedder that does not want JSesh's preference tree can ignore
 * {@code UserFontDirectoryManager} entirely and call
 * {@link jsesh.defaults.HieroglyphResourcesBuilder#buildFull(jsesh.utils.io.DirectoryHolder, jsesh.glossary.Glossary)} directly with a bare
 * with a bare {@link jsesh.utils.io.DirectoryHolder} and a bare {@link jsesh.glossary.Glossary}.
 */
package jsesh.defaults;
