/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Rendering style: port of `jsesh.render.style` (`JSeshStyle` and its parts,
/// `CartoucheSizeHelper`, `DocumentPreferencesStyleConverter`).
///
/// Lengths are in points; A1 is `standardSignHeight` high.

import type { DocumentPreferences } from "../document/preferences.js";
import {
  SymbolCodes,
  type CartouchePart,
  type CartoucheType,
  type ScriptCode,
  type TextDirection,
  type TextOrientation,
} from "../model/constants.js";

const f = Math.fround;

/// Port of `GeometrySpecification` (without the page format).
export interface GeometrySpecification {
  /// Width of thin lines (`{l...}`).
  fineLineWidth: number;
  /// Width of wide lines (`{L...}`).
  wideLineWidth: number;
  /// Space between lines.
  lineSkip: number;
  /// Space between columns.
  columnSkip: number;
  /// Size of a tab unit (`?NN`).
  tabUnitWidth: number;
  cartoucheLineWidth: number;
  cartoucheLoopLength: number;
  /// Margin between a cartouche and its content.
  cartoucheMargin: number;
  /// Depth and length of the bastions of fortified enclosures.
  bastionDepth: number;
  bastionLength: number;
  /// Margin between a hwt and its content.
  hwtSmallMargin: number;
  hwtSquareSize: number;
  serekhDoorSize: number;
  textWidth: number;
  textHeight: number;
  leftMargin: number;
  topMargin: number;
  maxCadratHeight: number;
  maxCadratWidth: number;
  /// A small space, used in various places (between quadrats, for instance).
  smallSkip: number;
  /// Signs higher than this ratio of A1 are "large".
  largeSignSizeRatio: number;
  /// Signs smaller than this ratio of A1 are "small".
  smallSignSizeRatio: number;
  smallBodyScaleLimit: number;
  /// The height of A1, in points.
  standardSignHeight: number;
  /// Height of the baseline of alphabetic text above the quadrats' bottom line.
  alphabeticTextBaseline: number;
  /// The total width (margins included) to which lines of horizontal text are
  /// soft-wrapped, in layout units. 0 (or less) means no wrapping: lines only
  /// end at explicit line breaks.
  wrapWidth: number;
  /// The page (`PageSpecification`). When defined, it determines the wrapping width
  /// and `wrapWidth` is ignored.
  page: PageSpecification;
}

/// Size and margins of a page, in layout units (`PageSpecification`). A width of 0
/// means that there is no page: the text is as large as it needs to be.
export interface PageSpecification {
  pageWidth: number;
  /// 0 if not specified.
  pageHeight: number;
  marginLeft: number;
  marginRight: number;
  marginTop: number;
  marginBottom: number;
}

export const NO_PAGE: Readonly<PageSpecification> = Object.freeze<PageSpecification>({
  pageWidth: 0,
  pageHeight: 0,
  marginLeft: 0,
  marginRight: 0,
  marginTop: 0,
  marginBottom: 0,
});

/// The space between two pages when they are displayed one below the other (`PageSpecification.PAGE_GAP`).
export const PAGE_GAP = 24;

/// Is the text cut into pages (`PageSpecification.isPaginated`)? It is when the page has a height.
export function isPaginated(page: PageSpecification | undefined): boolean {
  return (
    page !== undefined &&
    isPageDefined(page) &&
    f(page.pageHeight - page.marginTop - page.marginBottom) > 0 &&
    page.pageHeight > 0
  );
}

/// The distance between the tops of two consecutive stacked pages (`PageSpecification.pageStride`).
export function pageStride(page: PageSpecification): number {
  return f(page.pageHeight + PAGE_GAP);
}

/// The width available for text, margins excluded (`PageSpecification.usableWidth`).
export function usableWidth(page: PageSpecification): number {
  return f(page.pageWidth - page.marginLeft - page.marginRight);
}

/// Is a page defined (`PageSpecification.isDefined`)?
export function isPageDefined(page: PageSpecification | undefined): boolean {
  return page !== undefined && page.pageWidth > 0 && usableWidth(page) > 0;
}

/// Is soft wrapping of lines active (`GeometrySpecification.isWrapping`)?
export function isWrapping(geometry: GeometrySpecification): boolean {
  return isPageDefined(geometry.page) || geometry.wrapWidth > 0;
}

/// The width available for the content of a wrapped line (`GeometrySpecification.maxLineWidth`).
export function maxLineWidth(geometry: GeometrySpecification): number {
  return isPageDefined(geometry.page) ? usableWidth(geometry.page) : geometry.wrapWidth - 2 * geometry.leftMargin;
}

export const DEFAULT_GEOMETRY: Readonly<GeometrySpecification> = Object.freeze<GeometrySpecification>({
  fineLineWidth: 0.5,
  wideLineWidth: 3,
  lineSkip: 6,
  columnSkip: 10,
  tabUnitWidth: f(18 / 200),
  cartoucheLineWidth: 1,
  cartoucheLoopLength: 10,
  cartoucheMargin: 2,
  bastionDepth: 3,
  bastionLength: 4,
  hwtSmallMargin: 3,
  hwtSquareSize: 10,
  serekhDoorSize: 20,
  textWidth: 538,
  textHeight: 760,
  leftMargin: 6,
  topMargin: 5,
  maxCadratHeight: 18,
  maxCadratWidth: 22,
  smallSkip: 2,
  largeSignSizeRatio: f(0.8),
  smallSignSizeRatio: f(0.4),
  smallBodyScaleLimit: 12,
  standardSignHeight: 18,
  alphabeticTextBaseline: 4,
  wrapWidth: 0,
  page: NO_PAGE,
});

/// Port of `LayoutOptions`.
export interface LayoutOptions {
  textDirection: TextDirection;
  textOrientation: TextOrientation;
  paged: boolean;
  smallSignCentered: boolean;
  /// In horizontal text, scale quadrats down to the maximal quadrat width.
  limitQuadratWidthInLines: boolean;
  /// Lay out every line at the start, whatever the alignment written on its line break. Used
  /// when the lines are wrapped to the width of an editor (not WYSIWYG). The content of a zone,
  /// which has its own width, is not concerned.
  ignoreLineAlignment: boolean;
}

export const DEFAULT_LAYOUT_OPTIONS: Readonly<LayoutOptions> = Object.freeze<LayoutOptions>({
  textDirection: "LEFT_TO_RIGHT",
  textOrientation: "HORIZONTAL",
  paged: false,
  smallSignCentered: false,
  limitQuadratWidthInLines: false,
  ignoreLineAlignment: false,
});

export type ShadingMode = "GRAY_SHADING" | "LINE_HATCHING";

/// Port of `PaintingSpecifications`. Colours are CSS colours.
export interface PaintingSpecifications {
  blackColor: string;
  redColor: string;
  cursorColor: string;
  /// Used for shading and for `\i` signs.
  grayColor: string;
  backgroundColor: string;
  shadingStyle: ShadingMode;
  /// Colours for sign tags (`A1\det`).
  colorMap: Readonly<Record<string, string>>;
}

export const DEFAULT_PAINTING: Readonly<PaintingSpecifications> = Object.freeze<PaintingSpecifications>({
  blackColor: "#000000",
  redColor: "#ff0000",
  cursorColor: "#0000ff",
  grayColor: "rgba(153, 153, 153, 0.3)",
  backgroundColor: "#ffffff",
  shadingStyle: "GRAY_SHADING",
  colorMap: Object.freeze({}),
});

/// A font, described as in CSS.
export interface FontDescription {
  family: string;
  /// Size, in points.
  size: number;
  style?: "normal" | "italic";
  weight?: "normal" | "bold";
}

/// The CSS `font` shorthand for a font.
export function cssFont(font: FontDescription): string {
  return `${font.style ?? "normal"} ${font.weight ?? "normal"} ${font.size}px ${font.family}`;
}

export type YodChoice = "U0313" | "U0486" | "UA7BD" | "PLAIN_J";

/// Port of `FontSpecification`.
export interface FontSpecification {
  /// The transliteration font is a Unicode font (else a MdC ASCII font).
  translitUnicode: boolean;
  yodChoice: YodChoice;
  /// Render `q` as k with a dot below, as Gardiner does.
  gardinerQofUsed: boolean;
  plainFont: FontDescription;
  boldFont: FontDescription;
  italicFont: FontDescription;
  /// Font for small texts (`"sic"` signs, superscripts, unknown codes).
  superScriptFont: FontDescription;
  transliterationFont: FontDescription;
}

const SERIF = "'Times New Roman', Times, serif";

export const DEFAULT_FONTS: Readonly<FontSpecification> = Object.freeze<FontSpecification>({
  translitUnicode: true,
  yodChoice: "UA7BD",
  gardinerQofUsed: true,
  plainFont: { family: SERIF, size: 12 },
  boldFont: { family: SERIF, size: 12, weight: "bold" },
  italicFont: { family: SERIF, size: 12, style: "italic" },
  superScriptFont: { family: SERIF, size: 5, style: "italic" },
  transliterationFont: { family: `CharisSIL, ${SERIF}`, size: 12 },
});

/// The font for a script; scripts without a font of their own use the plain font.
export function fontForScript(fonts: FontSpecification, script: ScriptCode): FontDescription {
  switch (script) {
    case "BOLD":
      return fonts.boldFont;
    case "ITALIC":
      return fonts.italicFont;
    case "TRANSLITERATION":
      return fonts.transliterationFont;
    default:
      return fonts.plainFont;
  }
}

/// Port of `JSeshStyle`.
export interface JSeshStyle {
  geometry: GeometrySpecification;
  painting: PaintingSpecifications;
  fonts: FontSpecification;
  options: LayoutOptions;
}

export const DEFAULT_STYLE: Readonly<JSeshStyle> = Object.freeze<JSeshStyle>({
  geometry: DEFAULT_GEOMETRY,
  painting: DEFAULT_PAINTING,
  fonts: DEFAULT_FONTS,
  options: DEFAULT_LAYOUT_OPTIONS,
});

/// A partial style, to change some values of another one.
export interface StyleChanges {
  geometry?: Partial<GeometrySpecification>;
  painting?: Partial<PaintingSpecifications>;
  fonts?: Partial<FontSpecification>;
  options?: Partial<LayoutOptions>;
}

/// A copy of `style` with some values changed.
export function withStyle(style: JSeshStyle, changes: StyleChanges): JSeshStyle {
  return {
    geometry: { ...style.geometry, ...changes.geometry },
    painting: { ...style.painting, ...changes.painting },
    fonts: { ...style.fonts, ...changes.fonts },
    options: { ...style.options, ...changes.options },
  };
}

function pageOf(preferences: DocumentPreferences): PageSpecification {
  if (!(preferences.pageWidth > 0)) return NO_PAGE;
  return {
    pageWidth: f(preferences.pageWidth),
    pageHeight: f(preferences.pageHeight),
    marginLeft: f(preferences.marginLeft),
    marginRight: f(preferences.marginRight),
    marginTop: f(preferences.marginTop),
    marginBottom: f(preferences.marginBottom),
  };
}

/// Applies document preferences on top of a style
/// (`DocumentPreferencesStyleConverter.applyDocumentPreferences`).
export function applyDocumentPreferences(preferences: DocumentPreferences, original: JSeshStyle): JSeshStyle {
  return withStyle(original, {
    geometry: {
      cartoucheLineWidth: f(preferences.cartoucheLineWidth),
      maxCadratWidth: f(preferences.maxQuadratWidth),
      maxCadratHeight: f(preferences.maxQuadratHeight),
      lineSkip: f(preferences.lineSkip),
      columnSkip: f(preferences.columnSkip),
      standardSignHeight: f(preferences.standardSignHeight),
      smallBodyScaleLimit: f(preferences.smallBodyScaleLimit),
      smallSkip: f(preferences.smallSkip),
      alphabeticTextBaseline: f(preferences.alphabeticTextBaseline),
      page: pageOf(preferences),
      // With a page, the margins around the text are the page's.
      ...(preferences.pageWidth > 0
        ? { leftMargin: f(preferences.marginLeft), topMargin: f(preferences.marginTop) }
        : {}),
    },
    options: {
      smallSignCentered: preferences.smallSignCentered,
      limitQuadratWidthInLines: preferences.limitQuadratWidthInLines,
      textOrientation: preferences.textOrientation,
      textDirection: preferences.textDirection,
    },
    painting: { shadingStyle: preferences.useLinesForShading ? "LINE_HATCHING" : "GRAY_SHADING" },
  });
}

/// Length of a cartouche end along its main axis
/// (`CartoucheSizeHelper.computeCartouchePartLength`).
export function computeCartouchePartLength(style: JSeshStyle, type: CartoucheType, part: CartouchePart): number {
  const g = style.geometry;
  switch (type) {
    case "CARTOUCHE":
      switch (part) {
        case 0:
          return 0;
        case 1:
          return g.cartoucheLoopLength;
        case 2:
          return f(g.cartoucheLoopLength + g.cartoucheLineWidth);
        default:
          throw new Error("bad value for element " + part);
      }
    case "CIRCULAR_ENCLOSURE":
      switch (part) {
        case 0:
          return 0;
        case 1:
        case 2:
          return g.cartoucheLoopLength;
        default:
          throw new Error("bad value for element " + part);
      }
    case "SEREKH":
      switch (part) {
        case 1:
          return f(g.hwtSmallMargin + g.cartoucheLineWidth);
        case 2:
          return f(f(g.hwtSmallMargin + g.cartoucheLineWidth) + g.serekhDoorSize);
        default:
          return 0;
      }
    case "HWT":
      switch (part) {
        case 1:
          return f(g.hwtSmallMargin + g.cartoucheLineWidth);
        case 2:
        case 3:
          return f(f(g.hwtSmallMargin + g.cartoucheLineWidth) + g.hwtSquareSize);
        default:
          return 0;
      }
    case "CASTLE":
      return part !== 0 ? f(f(g.hwtSmallMargin + g.bastionDepth) + g.cartoucheLineWidth) : 0;
  }
}

/// Length along the secondary axis of a cartouche, from its outer side to
/// its content (`CartoucheSizeHelper.computeCartoucheSecondaryLength`).
export function computeCartoucheSecondaryLength(style: JSeshStyle, type: CartoucheType): number {
  const g = style.geometry;
  const result = f(g.cartoucheMargin + g.cartoucheLineWidth);
  return type === "CASTLE" ? f(result + g.bastionDepth) : result;
}

/// Width of a philological bracket (`PhilologyHelper.philologyWidth`).
export function philologyWidth(philologyType: number): number {
  // Closing codes are twice as large as the opening ones.
  if (philologyType >= 100) philologyType = Math.trunc(philologyType / 2);
  switch (philologyType) {
    case SymbolCodes.EDITORADDITION:
    case SymbolCodes.EDITORSUPERFLUOUS:
    case SymbolCodes.ERASEDSIGNS:
    case SymbolCodes.SCRIBEADDITION:
    case SymbolCodes.PREVIOUSLYREADABLE:
    case SymbolCodes.MINORADDITION:
    case SymbolCodes.DUBIOUS:
      return 4;
    default:
      return 0;
  }
}
