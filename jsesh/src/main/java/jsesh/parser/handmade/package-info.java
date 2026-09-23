/**
 * A hand-written recursive descent parser for Manuel de Codage text,
 * {@link jsesh.parser.handmade.MDCHandmadeParser}, which builds the literal
 * AST of {@link jsesh.parser.ast} directly.
 *
 * <p>It reuses the same lexer ({@link jsesh.parser.lex.MDCLex}), and is the
 * parser behind both {@link jsesh.parser.MDCParserAstGenerator} and (through
 * it) {@link jsesh.parser.MDCParserModelGenerator}. It replaced an earlier
 * CUP-generated parser ({@code jsesh/src/jcup/MDCParse.y}); the two were
 * checked to agree on every construct before the CUP grammar was retired.
 */
package jsesh.parser.handmade;
