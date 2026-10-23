/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Draws a view tree as SVG: port of `jsesh.render.draw.ViewDrawer` and
/// `DefaultElementDrawer`.
///
/// Each view is drawn in its parent's system, at its own origin; its scale
/// applies to its subviews. Coordinates are in points.

import { ShadingCode, SymbolCodes } from "../model/constants.js";
import {
  AlphabeticCharacter,
  Cadrat,
  Cartouche,
  Hieroglyph,
  HieroglyphicText,
  HRule,
  ModelElementAdapter,
  PageBreak,
  Philology,
  Superscript,
  TopItem,
  TopItemState,
} from "../model/model.js";
import { drawCartouche } from "./cartouches.js";
import { HieroglyphDrawer, type SignShape } from "./glyphs.js";
import { parsePath, pathBounds, rotation, transformPath } from "./path.js";
import { cssFont, fontForScript, philologyWidth, type FontDescription, type JSeshStyle } from "./style.js";
import { escapeXml, filledPath, num, PathBuilder, strokedPath } from "./svg-writer.js";
import { drawSpecialSymbol } from "./symbols.js";
import { displayString, type TextMeasurer } from "./text.js";
import { Rect, type MDCView } from "./view.js";

export interface DrawContext {
  style: JSeshStyle;
  drawer: HieroglyphDrawer;
  measurer: TextMeasurer;
}

export interface SvgOptions {
  /// Size of a point, in CSS pixels (default 1).
  scale?: number;
  /// Accessible description of the picture (usually the MdC text).
  title?: string;
  /// Prefix for the ids defined in the picture, which must be unique in the
  /// page (default `jsesh`).
  idPrefix?: string;
  /// Fill the background with the style's background colour.
  background?: boolean;
  /// Extra attributes for the `svg` element, already escaped.
  attributes?: string;
}

/// Draws a view tree (usually of a whole text) as an SVG document.
export function drawSvg(view: MDCView, context: DrawContext, options: SvgOptions = {}): string {
  const scale = options.scale ?? 1;
  const width = Math.ceil(view.width) + 2;
  const height = Math.ceil(view.height) + 2;
  const prefix = options.idPrefix ?? "jsesh";
  const drawer = new SvgViewDrawer(context, prefix);
  const content = drawer.draw(view);
  const title = options.title === undefined ? "" : `<title>${escapeXml(options.title)}</title>`;
  const role = options.title === undefined ? ' aria-hidden="true"' : ' role="img"';
  const background = options.background ? `<rect width="100%" height="100%" fill="${context.style.painting.backgroundColor}"/>` : "";
  return (
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${width} ${height}" width="${num(width * scale)}" ` +
    `height="${num(height * scale)}"${role}${options.attributes ? " " + options.attributes : ""}>` +
    title +
    drawer.defs() +
    background +
    content +
    drawer.hatching() +
    "</svg>"
  );
}

/// A transform from the current system to the page: scale, then translation.
interface PageTransform {
  sx: number;
  sy: number;
  tx: number;
  ty: number;
}

class SvgViewDrawer {
  private readonly style: JSeshStyle;
  private readonly drawer: HieroglyphDrawer;
  private readonly measurer: TextMeasurer;
  private drawingState = new TopItemState();
  /// Areas shaded with lines, in page coordinates (drawn at the end).
  private readonly hatched: Rect[] = [];
  /// The shapes used, each defined once: id for each code.
  private readonly shapeIds = new Map<string, string>();
  private shapeDefs = "";

  constructor(
    context: DrawContext,
    private readonly prefix: string,
  ) {
    this.style = context.style;
    this.drawer = context.drawer;
    this.measurer = context.measurer;
  }

  draw(view: MDCView): string {
    return this.drawView(view, 0, { sx: 1, sy: 1, tx: 0, ty: 0 });
  }

  /// A reference to the shape of a sign, defined in `defs` (its `fill`
  /// is inherited).
  useShape(code: string, shape: SignShape, color: string): string {
    let id = this.shapeIds.get(code);
    if (id === undefined) {
      id = `${this.prefix}-s${this.shapeIds.size}`;
      this.shapeIds.set(code, id);
      this.shapeDefs += `<path id="${id}" d="${shape.d}"${shape.evenOdd ? ' fill-rule="evenodd"' : ""}/>`;
    }
    return `<use href="#${id}" fill="${color}"/>`;
  }

  defs(): string {
    let result = this.shapeDefs;
    if (this.hatched.length > 0) result += this.hatchingPattern();
    return result === "" ? "" : `<defs>${result}</defs>`;
  }

  private hatchingPattern(): string {
    // `Shader`: lines of 0.5 pt, going up at 45°, on x + y = 3n (in page
    // coordinates), so 3/√2 apart.
    const period = num(3 / Math.SQRT2);
    return (
      `<pattern id="${this.prefix}-hatching" patternUnits="userSpaceOnUse" width="${period}" height="${period}" ` +
      `patternTransform="rotate(45) translate(-0.25 0)"><rect width="0.5" height="${period}" ` +
      `fill="${this.style.painting.blackColor}"/></pattern>`
    );
  }

  hatching(): string {
    if (this.hatched.length === 0) return "";
    const d = new PathBuilder();
    for (const r of this.hatched) d.rect(r.x, r.y, r.width, r.height);
    return `<path d="${d}" fill="url(#${this.prefix}-hatching)"/>`;
  }

  private drawView(v: MDCView, depth: number, page: PageTransform): string {
    if (depth === 1 && v.model instanceof TopItem) this.drawingState = v.model.state;
    const color = this.drawingState.isRed ? this.style.painting.redColor : this.style.painting.blackColor;
    const element = new ElementDrawer(this, v, color, page);
    let result = element.draw(false);
    // The subviews, in the view's inner system.
    let inner = "";
    const innerPage = { sx: page.sx * v.xScale, sy: page.sy * v.yScale, tx: page.tx, ty: page.ty };
    for (let i = 0; i < v.numberOfSubviews; i++) {
      const b = v.getSubViewBounds(i);
      const subPage = {
        sx: innerPage.sx,
        sy: innerPage.sy,
        tx: innerPage.tx + b.x * innerPage.sx,
        ty: innerPage.ty + b.y * innerPage.sy,
      };
      const sub = this.drawView(v.getSubView(i), depth + 1, subPage);
      if (sub !== "") inner += group(translate(b.x, b.y), sub);
    }
    if (inner !== "") result += group(scale(v.xScale, v.yScale), inner);
    result += element.draw(true);
    return result;
  }

  // Accessors for ElementDrawer.
  get s(): JSeshStyle {
    return this.style;
  }

  get signs(): HieroglyphDrawer {
    return this.drawer;
  }

  get textMeasurer(): TextMeasurer {
    return this.measurer;
  }

  get state(): TopItemState {
    return this.drawingState;
  }

  hatch(r: Rect): void {
    this.hatched.push(r);
  }
}

function translate(x: number, y: number): string {
  return x === 0 && y === 0 ? "" : `translate(${num(x)} ${num(y)})`;
}

function scale(sx: number, sy: number): string {
  if (sx === 1 && sy === 1) return "";
  return sx === sy ? `scale(${num(sx)})` : `scale(${num(sx)} ${num(sy)})`;
}

function group(transform: string, content: string): string {
  return transform === "" ? content : `<g transform="${transform}">${content}</g>`;
}

function text(s: string, x: number, y: number, font: FontDescription, color: string): string {
  if (s.trim() === "") return "";
  return `<text x="${num(x)}" y="${num(y)}" fill="${color}" style="font:${escapeXml(cssFont(font))};white-space:pre">${escapeXml(s)}</text>`;
}

/// Draws the element of one view (port of `DefaultElementDrawer`). Like in
/// Java, an element can draw before its subviews (`postfix` false: shading
/// of signs) and after them.
class ElementDrawer extends ModelElementAdapter {
  private postfix = false;
  private out = "";

  constructor(
    private readonly d: SvgViewDrawer,
    private readonly view: MDCView,
    private color: string,
    private readonly page: PageTransform,
  ) {
    super();
  }

  draw(postfix: boolean): string {
    this.postfix = postfix;
    this.out = "";
    this.view.model.accept(this);
    return this.out;
  }

  /// A character, with its baseline at its font's ascent.
  override visitAlphabeticCharacter(c: AlphabeticCharacter): void {
    if (!this.postfix) return;
    const fonts = this.d.s.fonts;
    const s = displayString(c, fonts);
    const m = this.d.textMeasurer.characterMetrics(fonts, c.script, s);
    this.out += text(s, 0, m.ascent, fontForScript(fonts, c.script), this.color);
  }

  override visitCartouche(c: Cartouche): void {
    if (!this.postfix) return;
    this.out += drawCartouche(c, this.d.s, this.view.width, this.view.height, this.color);
  }

  override visitCadrat(c: Cadrat): void {
    // Shading is drawn after the signs (shadeAfter is true by default).
    if (this.postfix) this.doShade(c.shading);
  }

  override visitHieroglyph(h: Hieroglyph): void {
    const style = this.d.s;
    if (!this.postfix) {
      if (h.modifiers.hasInteger("shading")) {
        this.doShade(parseShading(String(h.modifiers.getIntegerWithDefault("shading", 0))));
      }
      return;
    }
    for (const m of h.modifiers.modifiers) {
      const tagColor = style.painting.colorMap[m.name];
      if (tagColor !== undefined) this.color = tagColor;
    }
    if (h.modifiers.getBoolean("red")) this.color = style.painting.redColor;
    if (h.modifiers.getBoolean("i")) this.color = style.painting.grayColor;
    switch (h.type) {
      case SymbolCodes.SMALLTEXT: {
        const smallText = h.getSmallText();
        const r = this.d.textMeasurer.smallTextDimensions(style.fonts, smallText);
        this.out += text(smallText, 0, r.height, style.fonts.superScriptFont, this.color);
        break;
      }
      case SymbolCodes.REDPOINT:
        this.out += this.sign(h.code, 0, false, this.d.signs.scaleFromFontToStyle(style), style.painting.redColor);
        break;
      case SymbolCodes.HALFSPACE:
      case SymbolCodes.FULLSPACE:
        break;
      case SymbolCodes.FULLSHADE:
      case SymbolCodes.VERTICALSHADE:
      case SymbolCodes.HORIZONTALSHADE:
      case SymbolCodes.QUATERSHADE:
        this.shadeArea([new Rect(0, 0, this.view.width, this.view.height)]);
        break;
      default: {
        const special = h.type >= SymbolCodes.BEGINERASE && h.type <= SymbolCodes.ENDDUBIOUS;
        const reversed = h.isReversed !== (style.options.textDirection === "RIGHT_TO_LEFT");
        const baseSignScale = special ? 1 : this.d.signs.scaleFromFontToStyle(style);
        this.out += this.sign(h.code, h.angle, reversed, baseSignScale, this.color);
      }
    }
  }

  /// A sign (`DefaultElementDrawer.drawSign`, `HieroglyphDrawer.draw`).
  private sign(code: string, angle: number, reversed: boolean, baseSignScale: number, color: string): string {
    const v = this.view;
    const style = this.d.s;
    const signs = this.d.signs;
    let result: string;
    if (signs.isKnown(code)) {
      if (HieroglyphDrawer.isSpecial(code)) {
        result = drawSpecialSymbol(code, angle, v.width, v.height, v.xScale, v.yScale, color);
      } else {
        const shape = signs.getShape(code);
        if (shape === undefined) {
          // Not loaded (yet), or a shading code: nothing to draw.
          result = "";
        } else {
          let path = this.d.useShape(signs.provider.canonicalCode(code), shape, color);
          if (angle !== 0) {
            const b = pathBounds(transformPath(parsePath(shape.d), rotation(angle)));
            path = group(`${translate(-b.minX, -b.minY)} rotate(${num(angle)})`.trim(), path);
          }
          result = group(scale(baseSignScale * v.xScale, baseSignScale * v.yScale), path);
        }
      }
    } else {
      // Unknown sign: its code, in red.
      const r = this.d.textMeasurer.smallTextDimensions(style.fonts, code);
      result = text(code, 0, r.height, style.fonts.superScriptFont, style.painting.redColor);
    }
    if (reversed && result !== "") {
      // The right side is at the view's width.
      result = group(`matrix(-1 0 0 1 ${num(v.width)} 0)`, result);
    }
    return result;
  }

  override visitHRule(h: HRule): void {
    if (!this.postfix) return;
    const g = this.d.s.geometry;
    const d = new PathBuilder().line(h.startPos * g.tabUnitWidth, 0, h.endPos * g.tabUnitWidth, 0);
    this.out += strokedPath(d, this.color, h.type === "L" ? g.wideLineWidth : g.fineLineWidth);
  }

  /// A page break which doesn't start a new page is a line across the page.
  override visitPageBreak(_b: PageBreak): void {
    if (!this.postfix || this.d.s.options.paged) return;
    this.out += strokedPath(new PathBuilder().line(-10000, 0, 10000, 0), this.color, this.d.s.geometry.fineLineWidth);
  }

  /// Philological brackets around a group (tksesh's reading of `[[...]]`).
  override visitPhilology(p: Philology): void {
    if (!this.postfix) return;
    const w = philologyWidth(p.type);
    this.out += this.bracket(p.type * 2 + 1, this.view.width - w);
    this.out += this.bracket(p.type * 2, 0);
  }

  private bracket(code: number, x: number): string {
    const s = BRACKETS[code] ?? "??";
    const width = philologyWidth(code);
    const height = this.view.height;
    // The bracket, stretched to the size of the group.
    const font = this.d.s.fonts.plainFont;
    return (
      `<text x="0" y="0" fill="${this.color}" textLength="${num(width)}" lengthAdjust="spacingAndGlyphs" ` +
      `transform="translate(${num(x)} ${num(height * 0.8)}) scale(1 ${num(height / font.size)})" ` +
      `style="font:${escapeXml(cssFont(font))}">${escapeXml(s)}</text>`
    );
  }

  override visitSuperscript(s: Superscript): void {
    if (!this.postfix) return;
    const style = this.d.s;
    const dims = this.d.textMeasurer.smallTextDimensions(style.fonts, s.text);
    const ascent = dims.ascent ?? dims.height * 0.8;
    this.out += text(s.text, 0, ascent, style.fonts.superScriptFont, this.color);
    const d = new PathBuilder().line(dims.width / 2, dims.height + style.geometry.smallSkip, dims.width / 2, this.view.internalHeight);
    this.out += strokedPath(d, this.color, style.geometry.fineLineWidth);
  }

  override visitHieroglyphicText(_t: HieroglyphicText): void {}

  /// Shades the quarters of the view (`DefaultElementDrawer.doShade`).
  private doShade(localShading: number): void {
    const v = this.view;
    const shaded = this.d.state.isShaded;
    if (shaded) localShading = ShadingCode.FULL;
    if (localShading === ShadingCode.NONE) return;
    const leftToRight = v.parent !== null && v.parent.direction === "LEFT_TO_RIGHT";
    const [topStart, topEnd, bottomStart, bottomEnd] = leftToRight
      ? [ShadingCode.TOP_START, ShadingCode.TOP_END, ShadingCode.BOTTOM_START, ShadingCode.BOTTOM_END]
      : [ShadingCode.TOP_END, ShadingCode.TOP_START, ShadingCode.BOTTOM_END, ShadingCode.BOTTOM_START];
    const w = v.width / 2;
    const h = v.height / 2;
    const areas: Rect[] = [];
    if (localShading & topStart) areas.push(new Rect(0, 0, w, h));
    if (localShading & topEnd) areas.push(new Rect(w, 0, w, h));
    if (localShading & bottomStart) areas.push(new Rect(0, h, w, h));
    if (localShading & bottomEnd) areas.push(new Rect(w, h, w, h));
    // Shade the space between this view and the next one.
    const next = v.next;
    if (next !== null && shaded && this.d.s.options.textOrientation === "HORIZONTAL" && v.nextIsHorizontallyAdjacent()) {
      const outerWidth = next.position.x - v.position.x - v.width;
      areas.push(new Rect(v.width, 0, outerWidth / v.xScale, v.height));
    }
    this.shadeArea(areas);
  }

  private shadeArea(areas: Rect[]): void {
    const painting = this.d.s.painting;
    if (painting.shadingStyle === "GRAY_SHADING") {
      const d = new PathBuilder();
      for (const r of areas) d.rect(r.x, r.y, r.width, r.height);
      this.out += filledPath(d, painting.grayColor);
    } else {
      // Lines in page coordinates, so that they join from one sign to the next.
      const p = this.page;
      for (const r of areas) this.d.hatch(new Rect(p.tx + r.x * p.sx, p.ty + r.y * p.sy, r.width * p.sx, r.height * p.sy));
    }
  }
}

/// `LexicalSymbolsUtils.getStringForPhilology`.
const BRACKETS: Record<number, string> = {
  [SymbolCodes.BEGINERASE]: "[",
  [SymbolCodes.ENDERASE]: "]",
  [SymbolCodes.BEGINEDITORADDITION]: "<",
  [SymbolCodes.ENDEDITORADDITION]: ">",
  [SymbolCodes.BEGINEDITORSUPERFLUOUS]: "{",
  [SymbolCodes.ENDEDITORSUPERFLUOUS]: "}",
  [SymbolCodes.BEGINPREVIOUSLYREADABLE]: "[|",
  [SymbolCodes.ENDPREVIOUSLYREADABLE]: "|]",
  [SymbolCodes.BEGINSCRIBEADDITION]: "'",
  [SymbolCodes.ENDSCRIBEADDITION]: "'",
};

/// `ShadingCode.parse`: the quarters named by the digits 1 to 4.
function parseShading(s: string): number {
  let result = 0;
  for (const c of s) {
    if (c === "1") result |= ShadingCode.TOP_START;
    else if (c === "2") result |= ShadingCode.TOP_END;
    else if (c === "3") result |= ShadingCode.BOTTOM_START;
    else if (c === "4") result |= ShadingCode.BOTTOM_END;
  }
  return result;
}
