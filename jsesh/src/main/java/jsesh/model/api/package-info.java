/**
 * Marker interfaces used to build a model from a MDC text.
 *
 * <p>This package contains a number of interfaces, which are used as
 * <em>markers</em> in combination with the {@code MDCBuilder} interface
 * and the parser, to generate a model from a MDC text.
 *
 * <p>In practice, you write a class that implements {@code MDCBuilder}, and
 * generally creates objects from classes that implement the various
 * interfaces. Have a look at {@code jsesh.parser.ast.AstBuilder} and
 * {@code PrintMDCBuilder} for examples.
 */
package jsesh.model.api;
