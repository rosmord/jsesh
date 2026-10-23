/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// High-level rendering: from MdC text to SVG.

import { readMdcDocument } from "../document/document.js";
import { stripJSeshHeader } from "../document/header.js";
import type { HieroglyphicText } from "../model/model.js";
import { signCodes } from "./codes.js";
import { HieroglyphDrawer, type GlyphProvider } from "./glyphs.js";
import { buildView } from "./layout.js";
import { ApproximateTextMeasurer, CanvasTextMeasurer } from "./measurers.js";
import { applyDocumentPreferences, DEFAULT_STYLE, withStyle, type JSeshStyle, type StyleChanges } from "./style.js";
import { drawSvg, type SvgOptions } from "./svg.js";
import type { TextMeasurer } from "./text.js";
import type { MDCView } from "./view.js";

export interface RenderOptions extends SvgOptions {
  /// Where the signs come from (see `@jsesh/glyphs`).
  glyphs: GlyphProvider;
  /// The style, or changes to the default style. The preferences in the
  /// header of a JSesh document are applied first.
  style?: JSeshStyle | StyleChanges;
  /// Measures alphabetic text; by default, a canvas if there is one, else
  /// an approximation.
  measurer?: TextMeasurer;
  /// Height of a quadrat (that is, of A1), in CSS pixels. It sets `scale`.
  cadratHeight?: number;
}

/// A text laid out, ready to draw.
export interface LaidOutText {
  text: HieroglyphicText;
  style: JSeshStyle;
  view: MDCView;
  drawer: HieroglyphDrawer;
  measurer: TextMeasurer;
}

let defaultMeasurer: TextMeasurer | undefined;

function getDefaultMeasurer(): TextMeasurer {
  if (defaultMeasurer === undefined) {
    try {
      defaultMeasurer = new CanvasTextMeasurer();
    } catch {
      defaultMeasurer = new ApproximateTextMeasurer();
    }
  }
  return defaultMeasurer;
}

function isFullStyle(style: JSeshStyle | StyleChanges): style is JSeshStyle {
  return style.geometry !== undefined && style.painting !== undefined && style.fonts !== undefined && style.options !== undefined;
}

/// Reads a text (MdC, with an optional JSesh header) and computes its style.
function prepare(source: string | HieroglyphicText, options: RenderOptions): { text: HieroglyphicText; style: JSeshStyle } {
  let text: HieroglyphicText;
  let style: JSeshStyle = DEFAULT_STYLE;
  if (typeof source === "string") {
    const document = readMdcDocument(source);
    text = document.text;
    if (Object.keys(stripJSeshHeader(source).properties).length > 0) {
      style = applyDocumentPreferences(document.preferences, style);
    }
  } else {
    text = source;
  }
  if (options.style !== undefined) {
    style = isFullStyle(options.style) ? options.style : withStyle(style, options.style);
  }
  return { text, style };
}

/// Lays out a text. The shapes of rotated signs must be loaded (see
/// [layOutText]).
export function layOutTextSync(source: string | HieroglyphicText, options: RenderOptions): LaidOutText {
  const { text, style } = prepare(source, options);
  return layOutPrepared(text, style, options);
}

/// Loads the shapes of the signs of a text, and lays it out.
export async function layOutText(source: string | HieroglyphicText, options: RenderOptions): Promise<LaidOutText> {
  const { text, style } = prepare(source, options);
  await options.glyphs.loadShapes(signCodes(text, options.glyphs));
  return layOutPrepared(text, style, options);
}

function layOutPrepared(text: HieroglyphicText, style: JSeshStyle, options: RenderOptions): LaidOutText {
  const drawer = new HieroglyphDrawer(options.glyphs);
  const measurer = options.measurer ?? getDefaultMeasurer();
  const view = buildView(text, { style, drawer, measurer });
  return { text, style, view, drawer, measurer };
}

/// Draws a laid out text as SVG.
export function drawLaidOutText(laidOut: LaidOutText, options: SvgOptions & { cadratHeight?: number } = {}): string {
  const scale = options.cadratHeight !== undefined ? options.cadratHeight / laidOut.style.geometry.standardSignHeight : options.scale;
  return drawSvg(
    laidOut.view,
    { style: laidOut.style, drawer: laidOut.drawer, measurer: laidOut.measurer },
    { ...options, scale },
  );
}

/// Renders a text as SVG. `source` is MdC text, possibly a whole JSesh
/// document; its title is the MdC text unless `title` is given.
export async function renderToSvg(source: string | HieroglyphicText, options: RenderOptions): Promise<string> {
  const laidOut = await layOutText(source, options);
  return drawLaidOutText(laidOut, { title: typeof source === "string" ? source : undefined, ...options });
}
