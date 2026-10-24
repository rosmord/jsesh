/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Page-wide settings, and the rendering of MdC to SVG in a browser.

import {
  CanvasTextMeasurer,
  cssFont,
  DEFAULT_STYLE,
  drawLaidOutText,
  layOutText,
  withStyle,
  type GlyphProvider,
  type HieroglyphicText,
  type JSeshStyle,
  type LaidOutText,
  type StyleChanges,
  type TextMeasurer,
} from "@jsesh/core";
import { defaultGlyphDataUrl, loadGlyphProvider } from "@jsesh/glyphs";

export interface JSeshConfiguration {
  /// URL of the glyph data (a folder with `glyph-index.json`, `glyphs/` and
  /// `fonts/`). By default, the `data` folder of `@jsesh/glyphs`.
  glyphsUrl?: string;
  /// A glyph provider, instead of the one loaded from `glyphsUrl`.
  glyphs?: GlyphProvider;
  /// Changes to the default style, for all texts.
  style?: StyleChanges;
}

/// The default style in web pages: JSesh's, with small margins (as JSesh
/// does for pictures embedded in other documents).
const PAGE_STYLE: JSeshStyle = withStyle(DEFAULT_STYLE, { geometry: { leftMargin: 1, topMargin: 1 } });

let configuration: JSeshConfiguration = {};
let measurer: TextMeasurer | undefined;
let fontsReady: Promise<unknown> | undefined;

/// Sets page-wide options. Call it before the first text is displayed.
export function configureJSesh(options: JSeshConfiguration): void {
  configuration = { ...configuration, ...options };
  fontsReady = undefined;
}

/// The URL of the glyph data, absolute, ending with `/`.
export function glyphsUrl(url?: string): string {
  const base = typeof location === "undefined" ? undefined : location.href;
  const result = new URL(url ?? configuration.glyphsUrl ?? defaultGlyphDataUrl(), base).href;
  return result.endsWith("/") ? result : result + "/";
}

/// The glyph provider for the page.
export async function pageGlyphProvider(url?: string): Promise<GlyphProvider> {
  if (url === undefined && configuration.glyphs !== undefined) return configuration.glyphs;
  return loadGlyphProvider(glyphsUrl(url));
}

export function pageStyle(changes?: StyleChanges): JSeshStyle {
  const base = configuration.style === undefined ? PAGE_STYLE : withStyle(PAGE_STYLE, configuration.style);
  return changes === undefined ? base : withStyle(base, changes);
}

/// Declares the transliteration font, and waits until the fonts of the
/// style are loaded (text can't be measured before).
function loadFonts(style: JSeshStyle): Promise<unknown> {
  if (fontsReady === undefined) {
    if (typeof document === "undefined" || document.fonts === undefined) {
      fontsReady = Promise.resolve();
    } else {
      if (typeof FontFace !== "undefined") {
        const face = new FontFace("CharisSIL", `url(${glyphsUrl()}fonts/CharisSIL-R.woff)`);
        document.fonts.add(face);
      }
      const f = style.fonts;
      fontsReady = Promise.all(
        [f.plainFont, f.boldFont, f.italicFont, f.superScriptFont, f.transliterationFont].map((font) =>
          document.fonts.load(cssFont(font), "aꜣ").catch(() => undefined),
        ),
      );
    }
  }
  return fontsReady;
}

export interface BrowserRenderOptions {
  style?: StyleChanges;
  /// URL of the glyph data, for this text only.
  glyphsUrl?: string;
}

/// Loads what is needed (signs, fonts) and lays out a text: MdC (possibly a
/// whole JSesh document), or a model, which the views then refer to.
export async function layOutInPage(
  source: string | HieroglyphicText,
  options: BrowserRenderOptions = {},
): Promise<LaidOutText> {
  const style = pageStyle(options.style);
  const [glyphs] = await Promise.all([pageGlyphProvider(options.glyphsUrl), loadFonts(style)]);
  measurer ??= new CanvasTextMeasurer();
  return layOutText(source, { glyphs, style, measurer });
}

let idCounter = 0;

/// Renders MdC text as an SVG string.
export async function renderMdc(
  mdc: string,
  options: BrowserRenderOptions & { cadratHeight?: number } = {},
): Promise<string> {
  const laidOut = await layOutInPage(mdc, options);
  return drawLaidOutText(laidOut, { cadratHeight: options.cadratHeight, title: mdc, idPrefix: `jsesh${idCounter++}` });
}
