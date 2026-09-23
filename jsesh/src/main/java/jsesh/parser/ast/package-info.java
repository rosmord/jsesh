/**
 * A literal Abstract Syntax Tree for Manuel de Codage text.
 *
 * <p>Unlike {@link jsesh.model}, which {@link jsesh.parser.AstModelBuilder} builds
 * from this AST and which already applies some interpretation (e.g. red/shaded
 * state is folded into each item instead of kept as toggle markers, some constructs
 * such as cadrat and zone options are dropped altogether), this package keeps a
 * faithful, uninterpreted record of what the parser actually read: every construct
 * recognized by the grammar becomes a node, in the order it was parsed.
 *
 * <p>Use {@link jsesh.parser.MDCParserAstGenerator} to build this tree from MDC text:
 *
 * <pre>
 * MDCParserAstGenerator g = new MDCParserAstGenerator();
 * AstDocument doc = g.parse("p*t:pt");
 * </pre>
 *
 * <p>{@link jsesh.parser.ast.AstNode#accept(jsesh.parser.ast.AstVisitor)} and
 * {@link jsesh.parser.ast.AstVisitor} follow the same visitor pattern as
 * {@link jsesh.model.ModelElementVisitor}; {@link jsesh.parser.ast.AstVisitorAdapter}
 * is a no-op base to override selectively.
 *
 * <p>{@code AstNode} is sealed, so a consumer can instead {@code switch} over
 * node types directly, with the compiler checking coverage:
 *
 * <pre>
 * String describe(AstNode node) {
 *     return switch (node) {
 *         case AstHieroglyph h -&gt; "sign " + h.code();
 *         case AstCadrat c -&gt; "cadrat";
 *         default -&gt; "other";
 *     };
 * }
 * </pre>
 *
 * <p>Most node types are records, with plain (no "get" prefix) accessors,
 * e.g. {@code hieroglyph.code()}. The exceptions are the handful of nodes the
 * grammar builds incrementally, of unbounded arity, via left recursion
 * ({@link jsesh.parser.ast.AstTopItemList}, {@link jsesh.parser.ast.AstBasicItemList},
 * {@link jsesh.parser.ast.AstHBox}, {@link jsesh.parser.ast.AstCadrat},
 * {@link jsesh.parser.ast.AstLigature}, {@link jsesh.parser.ast.AstAbsoluteGroup},
 * {@link jsesh.parser.ast.AstModifierList}, {@link jsesh.parser.ast.AstOptionList}):
 * those stay plain, mutable-during-parsing classes, since a record's fields
 * are fixed at construction.
 */
package jsesh.parser.ast;
