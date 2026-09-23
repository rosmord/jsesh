/**
 * A hand-written recursive descent parser for Manuel de Codage text,
 * {@link jsesh.parser.handmade.MDCHandmadeParser}, which builds the literal
 * AST of {@link jsesh.parser.ast} directly.
 *
 * <p>It is meant to be equivalent to {@link jsesh.parser.MDCParserAstGenerator}
 * (the CUP-generated parser from {@code jsesh/src/jcup/MDCParse.y}), and
 * reuses the same lexer ({@link jsesh.parser.lex.MDCLex}). This package is
 * temporary: it will move once the equivalence has been checked.
 */
package jsesh.parser.handmade;
