/// Reading Manuel de Codage text into the document model.
///
/// This package bridges {@link jsesh.parser} and {@link jsesh.model}: it
/// interprets the literal AST built by {@link jsesh.parser.MDCParserAstGenerator}
/// into a {@link jsesh.model.TopItemList}. It is the only place which depends on
/// both, so that the parser stays independent of the model.
///
/// Most of the time, you will use {@link jsesh.mdcreader.MDCParserModelGenerator}:
///
/// ```java
/// MDCParserModelGenerator m = new MDCParserModelGenerator();
/// Reader r = new StringReader("p*t:pt");
/// TopItemList l = m.parse(r);
/// ```
package jsesh.mdcreader;
