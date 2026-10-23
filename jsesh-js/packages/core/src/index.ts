/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Public API of `@jsesh/core`.

export { parseMdc } from "./mdcreader/builder.js";
export { toMdc, type MdcWriterOptions } from "./writer/writer.js";
export {
  readMdcDocument,
  writeMdcDocument,
  type MdcDocument,
  type ReadDocumentOptions,
} from "./document/document.js";
export {
  DEFAULT_DOCUMENT_PREFERENCES,
  documentPreferencesFromMap,
  documentPreferencesToMap,
  type DocumentPreferences,
} from "./document/preferences.js";
export { stripJSeshHeader, writeJSeshHeader } from "./document/header.js";

// Lower level: the literal AST and the parser.
export { MdcParser, MdcSyntaxError, parseMdcAst, type MdcParserOptions } from "./parser/parser.js";
export type * as Ast from "./parser/ast.js";

export * from "./model/model.js";
export * from "./model/constants.js";

// Rendering: layout and drawing.
export { MDCView, Rect, type Point, type ViewSide } from "./render/view.js";
export { buildView, type LayoutContext } from "./render/layout.js";
export {
  HieroglyphDrawer,
  scaleZone,
  type ExplicitPosition,
  type GlyphProvider,
  type HorizontalGravity,
  type LigatureZone,
  type SignShape,
  type VerticalGravity,
} from "./render/glyphs.js";
export {
  displayString,
  isRightToLeft,
  TextRun,
  transliterationString,
  type CharacterMetrics,
  type RunMeasure,
  type SmallTextDimensions,
  type TextMeasurer,
} from "./render/text.js";
export * from "./render/style.js";
export { signCodes } from "./render/codes.js";
export { drawSvg, type DrawContext, type SvgOptions } from "./render/svg.js";
export { ApproximateTextMeasurer, CanvasTextMeasurer } from "./render/measurers.js";
export {
  drawLaidOutText,
  layOutText,
  layOutTextSync,
  renderToSvg,
  type LaidOutText,
  type RenderOptions,
} from "./render/render.js";
