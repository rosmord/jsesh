/// Code for reading texts written in the Manuel de Codage format.
///
/// This package builds a literal, uninterpreted record of what was parsed:
/// {@link jsesh.parser.MDCParserAstGenerator} returns an {@link jsesh.parser.ast.AstDocument},
/// walkable with {@link jsesh.parser.ast.AstVisitor}. See the package
/// {@link jsesh.parser.ast} for details.
///
/// It does not depend on {@link jsesh.model}. To get an in-memory document
/// model instead, which is what you will want most of the time, use
/// {@link jsesh.mdcreader.MDCParserModelGenerator}, which interprets this AST.
///
/// Both are backed by {@link jsesh.parser.MDCParser}, a
/// hand-written recursive descent parser for the Manuel de Codage grammar.
package jsesh.parser;
