/**
 * Marker interfaces shared by the two representations of a parsed MDC text:
 * the interpreted document model ({@code jsesh.model}) and the literal parse
 * AST ({@code jsesh.parser.ast}).
 *
 * <p>Each interface here (e.g. {@code CadratInterface}, {@code HBoxInterface})
 * is implemented both by a {@code jsesh.model} class and by the corresponding
 * {@code jsesh.parser.ast} node, letting a handful of structures (mainly the
 * hieroglyph position/word-ending machinery) stay shared between the two.
 */
package jsesh.model.api;
